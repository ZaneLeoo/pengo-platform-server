#!/usr/bin/env bash
set -Eeuo pipefail

# 项目管理系统服务器端发布工具。
# 推荐复制到部署根目录（默认 /www/wwwroot/pm/pmctl.sh）后执行。
# 真实密码只从部署根目录的 .env 读取，不写入脚本或命令输出。
ROOT="${PM_ROOT:-$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)}"
COMPOSE_FILE="$ROOT/compose.yml"
ENV_FILE="$ROOT/.env"
BACKUP_DIR="$ROOT/backups"

compose() {
  # 所有 Compose 命令都显式指定配置和密钥文件，避免从错误目录启动项目。
  docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" "$@"
}

require_runtime() {
  [[ -f "$COMPOSE_FILE" ]] || { echo "缺少 $COMPOSE_FILE" >&2; exit 1; }
  [[ -f "$ENV_FILE" ]] || { echo "缺少 $ENV_FILE" >&2; exit 1; }
}

load_env() {
  set -a
  # shellcheck disable=SC1090
  . "$ENV_FILE"
  set +a
}

health() {
  # 经 Nginx 访问后端，能够同时验证前端容器、容器网络和后端健康接口。
  curl -fsS http://127.0.0.1:9000/dev-api/health
  echo
}

wait_for_health() {
  # 容器刚重建时 Nginx/后端可能有数秒连接重置；统一在这里做有限重试。
  local attempts="${1:-36}"
  local output=""
  for _ in $(seq 1 "$attempts"); do
    if output="$(health 2>/dev/null)"; then
      printf '%s\n' "$output"
      return 0
    fi
    sleep 5
  done
  return 1
}

backup_database() {
  # 每次发布前生成事务一致的压缩备份；备份仅 root 可读。
  require_runtime
  load_env
  mkdir -p "$BACKUP_DIR"
  local file="$BACKUP_DIR/pm-$(date +%Y%m%d-%H%M%S).sql.gz"
  compose exec -T mysql \
    mysqldump -upm_app -p"$PM_DB_PASSWORD" --single-transaction --routines --triggers --no-tablespaces pm \
    | gzip -9 > "$file"
  chmod 600 "$file"
  echo "$file"
}

apply_migrations() {
  # 只执行 pm_schema_history 中尚未成功登记的 V*.sql，执行成功后写入版本记录。
  load_env
  local release_dir="$1"
  local migration_dir="$release_dir/database/migrations"
  [[ -d "$migration_dir" ]] || return 0

  local file base version checksum applied started elapsed
  while IFS= read -r file; do
    base="$(basename "$file")"
    version="${base%%__*}"
    version="${version#V}"
    # 查询命令显式断开 while 的标准输入，避免 mysql 吞掉后续迁移文件名。
    applied="$(compose exec -T mysql mysql -upm_app -p"$PM_DB_PASSWORD" -Nse \
      "SELECT COUNT(*) FROM pm.pm_schema_history WHERE version='$version' AND success=1" \
      </dev/null)"
    [[ "$applied" == "1" ]] && continue

    echo "执行数据库迁移：$base"
    started="$(date +%s%3N)"
    compose exec -T mysql mysql -upm_app -p"$PM_DB_PASSWORD" pm < "$file"
    elapsed=$(( $(date +%s%3N) - started ))
    checksum="$(sha256sum "$file" | awk '{print $1}')"
    compose exec -T mysql mysql -upm_app -p"$PM_DB_PASSWORD" -e \
      "INSERT INTO pm.pm_schema_history(version,description,type,script,checksum,installed_by,execution_time_ms,success) VALUES ('$version','$base','MIGRATION','$base','$checksum','pmctl',$elapsed,1)" \
      </dev/null
  done < <(find "$migration_dir" -maxdepth 1 -type f -name 'V*.sql' | sort)
}

deploy_release() {
  # 发布顺序：校验包结构 -> 解压 -> 备份 -> 迁移 -> 切换软链接 -> 重建 -> 健康检查。
  require_runtime
  local archive="${1:-}"
  [[ -f "$archive" ]] || { echo "发布包不存在：$archive" >&2; exit 1; }

  local release
  release="$(tar -tzf "$archive" | awk -F/ 'NR==1 {print $1}')"
  [[ "$release" == pm-release-* ]] || { echo "发布包顶层目录不合法：$release" >&2; exit 1; }

  mkdir -p "$ROOT/releases"
  if [[ ! -d "$ROOT/releases/$release" ]]; then
    tar -xzf "$archive" -C "$ROOT/releases"
  fi
  local release_dir="$ROOT/releases/$release"
  [[ -f "$release_dir/backend/ruoyi-admin.jar" ]] || { echo "发布包缺少后端 JAR" >&2; exit 1; }
  [[ -f "$release_dir/frontend/index.html" ]] || { echo "发布包缺少前端 dist" >&2; exit 1; }

  # 先捕获备份结果再输出，确保 mysqldump 失败会立即终止发布，不能带病迁移。
  local backup_file
  backup_file="$(backup_database)"
  echo "数据库备份：$backup_file"
  apply_migrations "$release_dir"

  local previous=""
  [[ -L "$ROOT/current" ]] && previous="$(readlink "$ROOT/current")"
  ln -sfn "releases/$release" "$ROOT/current"
  cp -f "$release_dir/deploy/templates/application-prod.yml" "$ROOT/config/application-prod.yml"
  cp -f "$release_dir/deploy/docker/compose.yml" "$ROOT/compose.yml"
  cp -f "$release_dir/deploy/docker/nginx.conf" "$ROOT/nginx/default.conf"

  # 补齐系统内置头像；仅安装缺失文件，不覆盖用户后续上传的头像。
  install -d -m 0755 "$ROOT/data/uploads/avatar"
  local avatar
  for avatar in default-admin.png default-male.png default-female.png; do
    if [[ -f "$release_dir/deploy/assets/$avatar" && \
          ! -f "$ROOT/data/uploads/avatar/$avatar" ]]; then
      install -m 0644 "$release_dir/deploy/assets/$avatar" \
        "$ROOT/data/uploads/avatar/$avatar"
    fi
  done

  compose config --quiet
  compose up -d
  # current 是软链接；强制重建应用容器，确保 bind mount 和 JAR 都指向新版本。
  compose up -d --no-deps --force-recreate backend web

  if ! wait_for_health 36 >/dev/null; then
    # 自动回退代码软链接；数据库迁移不会自动反向执行，必须由迁移说明单独处理。
    echo "新版本健康检查失败，恢复代码软链接：$previous" >&2
    [[ -n "$previous" ]] && ln -sfn "$previous" "$ROOT/current"
    compose up -d backend web
    exit 1
  fi

  echo "部署成功：$release"
  wait_for_health 12
}

rollback_release() {
  # 仅回滚前后端代码。数据库是否需要回滚由对应迁移的人工说明决定。
  require_runtime
  local release="${1:-}"
  [[ -d "$ROOT/releases/$release" ]] || { echo "版本不存在：$release" >&2; exit 1; }
  ln -sfn "releases/$release" "$ROOT/current"
  compose up -d backend web
  health
  echo "注意：代码已回滚，数据库迁移不会自动反向回滚。"
}

case "${1:-}" in
  deploy) deploy_release "${2:-}" ;;
  rollback) rollback_release "${2:-}" ;;
  backup) backup_database ;;
  health) health ;;
  status) require_runtime; compose ps ;;
  logs) require_runtime; compose logs --tail="${2:-200}" backend ;;
  restart) require_runtime; compose restart backend web ;;
  *)
    echo "用法：$0 {deploy <release.tar.gz>|rollback <release-id>|backup|health|status|logs [行数]|restart}"
    exit 2
    ;;
esac
