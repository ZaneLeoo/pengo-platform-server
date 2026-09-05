#!/usr/bin/env bash
set -Eeuo pipefail

APP_USER="${PM_APP_USER:-pm}"
APP_GROUP="${PM_APP_GROUP:-pm}"
APP_HOME="${PM_APP_HOME:-/opt/pm}"
RELEASES_DIR="$APP_HOME/releases"
CURRENT_LINK="$APP_HOME/current"
BIN_DIR="$APP_HOME/bin"
SHARE_DIR="$APP_HOME/share"
CONFIG_DIR="${PM_CONFIG_DIR:-/etc/pm}"
ENV_FILE="$CONFIG_DIR/pm.env"
WEB_ROOT="${PM_WEB_ROOT:-/var/www/pm}"
UPLOAD_DIR="${PM_UPLOAD_PATH:-/data/pm/uploads}"
LOG_DIR="${PM_LOG_DIR:-/var/log/pm}"
BACKUP_DIR="${PM_BACKUP_DIR:-/var/backups/pm}"
SERVICE_NAME="${PM_SERVICE_NAME:-pm-backend}"
SCRIPT_PATH="$(readlink -f "$0")"
SCRIPT_DIR="$(dirname "$SCRIPT_PATH")"

log() { printf '\033[1;34m[pm-deploy]\033[0m %s\n' "$*"; }
warn() { printf '\033[1;33m[pm-deploy]\033[0m %s\n' "$*" >&2; }
fail() { printf '\033[1;31m[pm-deploy]\033[0m %s\n' "$*" >&2; exit 1; }

require_root() {
    [[ "${EUID:-$(id -u)}" -eq 0 ]] || fail "请使用 sudo 或 root 执行。"
}

require_command() {
    command -v "$1" >/dev/null 2>&1 || fail "缺少命令：$1"
}

load_environment() {
    [[ -f "$ENV_FILE" ]] || fail "缺少 $ENV_FILE，请先执行 install 并填写配置。"
    # shellcheck disable=SC1090
    set -a
    source "$ENV_FILE"
    set +a

    [[ -n "${PM_DB_PASSWORD:-}" && "$PM_DB_PASSWORD" != "CHANGE_ME" ]] || fail "请配置 PM_DB_PASSWORD。"
    [[ -n "${PM_TOKEN_SECRET:-}" && "$PM_TOKEN_SECRET" != "CHANGE_ME" ]] || fail "请配置 PM_TOKEN_SECRET。"

    PM_DB_HOST="${PM_DB_HOST:-127.0.0.1}"
    PM_DB_PORT="${PM_DB_PORT:-3306}"
    PM_DB_NAME="${PM_DB_NAME:-pm}"
    PM_DB_USERNAME="${PM_DB_USERNAME:-pm_app}"
    PM_MIGRATION_DB_USERNAME="${PM_MIGRATION_DB_USERNAME:-$PM_DB_USERNAME}"
    PM_MIGRATION_DB_PASSWORD="${PM_MIGRATION_DB_PASSWORD:-$PM_DB_PASSWORD}"
    [[ -n "$PM_MIGRATION_DB_PASSWORD" && "$PM_MIGRATION_DB_PASSWORD" != "CHANGE_ME" ]] || fail "请配置 PM_MIGRATION_DB_PASSWORD。"
    PM_DB_BASELINE_VERSION="${PM_DB_BASELINE_VERSION:-55}"
    PM_SERVER_PORT="${PM_SERVER_PORT:-8088}"
    PM_HEALTH_URL="${PM_HEALTH_URL:-http://127.0.0.1:${PM_SERVER_PORT}/health}"
    PM_KEEP_RELEASES="${PM_KEEP_RELEASES:-5}"
    PM_KEEP_BACKUPS="${PM_KEEP_BACKUPS:-10}"
}

mysql_query() {
    MYSQL_PWD="$PM_MIGRATION_DB_PASSWORD" mysql --protocol=tcp \
        --host="$PM_DB_HOST" --port="$PM_DB_PORT" --user="$PM_MIGRATION_DB_USERNAME" \
        --default-character-set=utf8mb4 --batch --skip-column-names "$PM_DB_NAME" -e "$1"
}

mysql_file() {
    local file="$1"
    MYSQL_PWD="$PM_MIGRATION_DB_PASSWORD" mysql --protocol=tcp \
        --host="$PM_DB_HOST" --port="$PM_DB_PORT" --user="$PM_MIGRATION_DB_USERNAME" \
        --default-character-set=utf8mb4 "$PM_DB_NAME" < "$file"
}

sql_escape() {
    printf '%s' "$1" | sed "s/'/''/g"
}

install_server() {
    require_root
    require_command install
    local template_dir="$SCRIPT_DIR/templates"
    [[ -d "$template_dir" ]] || fail "模板目录不存在：$template_dir。请从解压后的发布包执行 install。"

    if ! getent group "$APP_GROUP" >/dev/null; then
        groupadd --system "$APP_GROUP"
    fi
    if ! id "$APP_USER" >/dev/null 2>&1; then
        useradd --system --gid "$APP_GROUP" --home-dir "$APP_HOME" --shell /usr/sbin/nologin "$APP_USER"
    fi

    install -d -m 0755 "$APP_HOME" "$RELEASES_DIR" "$BIN_DIR" "$SHARE_DIR" "$CONFIG_DIR" "$WEB_ROOT"
    install -d -o "$APP_USER" -g "$APP_GROUP" -m 0750 "$UPLOAD_DIR" "$LOG_DIR"
    install -d -m 0750 "$BACKUP_DIR"
    install -m 0755 "$SCRIPT_PATH" "$BIN_DIR/deploy.sh"
    install -m 0644 "$template_dir/nginx-pm.conf" "$SHARE_DIR/nginx-pm.conf.example"

    if [[ ! -f "$CONFIG_DIR/application-prod.yml" ]]; then
        install -m 0640 -o root -g "$APP_GROUP" "$template_dir/application-prod.yml" "$CONFIG_DIR/application-prod.yml"
    fi
    if [[ ! -f "$ENV_FILE" ]]; then
        install -m 0600 "$template_dir/pm.env.example" "$ENV_FILE"
    fi

    install -m 0644 "$template_dir/pm-backend.service" "/etc/systemd/system/${SERVICE_NAME}.service"
    systemctl daemon-reload

    log "服务器目录和 systemd 服务已安装。"
    warn "下一步必须编辑 $ENV_FILE，更换所有 CHANGE_ME。"
    warn "然后在宝塔站点中合并 $SHARE_DIR/nginx-pm.conf.example。"
}

verify_package() {
    local package="$1"
    [[ -f "$package" ]] || fail "发布包不存在：$package"
    case "$package" in
        *.tar.gz|*.tgz) ;;
        *) fail "仅支持 .tar.gz 或 .tgz 发布包。" ;;
    esac

    if [[ -f "${package}.sha256" ]]; then
        require_command sha256sum
        (cd "$(dirname "$package")" && sha256sum -c "$(basename "${package}.sha256")") \
            || fail "发布包 SHA-256 校验失败。"
    else
        warn "未找到 ${package}.sha256，跳过完整性校验。"
    fi
}

extract_package() {
    local package="$1" destination="$2"
    tar -xzf "$package" -C "$destination"
    local package_root
    package_root="$(find "$destination" -mindepth 1 -maxdepth 1 -type d -print -quit)"
    [[ -n "$package_root" ]] || fail "发布包内没有顶层目录。"
    [[ -f "$package_root/backend/ruoyi-admin.jar" ]] || fail "发布包缺少 backend/ruoyi-admin.jar。"
    [[ -f "$package_root/frontend/index.html" ]] || fail "发布包缺少 frontend/index.html。"
    [[ -f "$package_root/RELEASE_ID" ]] || fail "发布包缺少 RELEASE_ID。"
    printf '%s' "$package_root"
}

backup_database() {
    require_command mysqldump
    require_command gzip
    local release_id="$1"
    local backup_file="$BACKUP_DIR/${PM_DB_NAME}-${release_id}-$(date +%Y%m%d%H%M%S).sql.gz"
    log "备份数据库到 $backup_file"
    MYSQL_PWD="$PM_MIGRATION_DB_PASSWORD" mysqldump --protocol=tcp \
        --host="$PM_DB_HOST" --port="$PM_DB_PORT" --user="$PM_MIGRATION_DB_USERNAME" \
        --default-character-set=utf8mb4 --single-transaction --routines --triggers \
        "$PM_DB_NAME" | gzip -c > "$backup_file"
    chmod 0600 "$backup_file"
}

initialize_schema_history() {
    local package_root="$1"
    mysql_file "$package_root/database/bootstrap_schema_history.sql"

    local history_count core_table_count installed_by
    history_count="$(mysql_query 'SELECT COUNT(*) FROM pm_schema_history')"
    [[ "$history_count" == "0" ]] || return 0

    core_table_count="$(mysql_query "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'pm_project_info'")"
    if [[ "$core_table_count" == "0" ]]; then
        if [[ -f "$package_root/database/baseline.sql" ]]; then
            log "数据库为空，导入发布包中的基线文件。"
            mysql_file "$package_root/database/baseline.sql"
        else
            fail "数据库为空且发布包不含 database/baseline.sql。请先导入已验收的 pm 基线。"
        fi
    fi

    installed_by="$(sql_escape "$(id -un)")"
    mysql_query "INSERT INTO pm_schema_history(version, description, type, script, checksum, installed_by, execution_time_ms, success) VALUES ('$(sql_escape "$PM_DB_BASELINE_VERSION")', 'Imported verified PM baseline', 'BASELINE', 'baseline-${PM_DB_BASELINE_VERSION}', NULL, '$installed_by', 0, 1)"
    log "数据库已登记基线版本 $PM_DB_BASELINE_VERSION。"
}

apply_migrations() {
    local package_root="$1" migration_dir="$package_root/database/migrations"
    [[ -d "$migration_dir" ]] || return 0
    require_command sha256sum

    local file filename raw_version version description checksum existing_checksum start_ms end_ms elapsed installed_by
    while IFS= read -r -d '' file; do
        filename="$(basename "$file")"
        if [[ ! "$filename" =~ ^V([0-9]+)__(.+)\.sql$ ]]; then
            continue
        fi
        raw_version="${BASH_REMATCH[1]}"
        version="$((10#$raw_version))"
        description="${BASH_REMATCH[2]%.sql}"
        checksum="$(sha256sum "$file" | awk '{print $1}')"

        if (( version <= PM_DB_BASELINE_VERSION )); then
            log "跳过基线内脚本 $filename。"
            continue
        fi

        existing_checksum="$(mysql_query "SELECT COALESCE(checksum, '') FROM pm_schema_history WHERE version = '$(sql_escape "$version")' AND success = 1 LIMIT 1")"
        if [[ -n "$existing_checksum" ]]; then
            [[ "$existing_checksum" == "$checksum" ]] || fail "迁移 $filename 已执行但校验和已改变，禁止继续发布。"
            log "迁移已执行，跳过：$filename"
            continue
        fi

        log "执行数据库迁移：$filename"
        start_ms="$(date +%s%3N)"
        mysql_file "$file" || fail "迁移失败：$filename。数据库可能已发生部分 DDL，请检查并按备份恢复。"
        end_ms="$(date +%s%3N)"
        elapsed="$((end_ms - start_ms))"
        installed_by="$(sql_escape "$(id -un)")"
        mysql_query "INSERT INTO pm_schema_history(version, description, type, script, checksum, installed_by, execution_time_ms, success) VALUES ('$(sql_escape "$version")', '$(sql_escape "$description")', 'MIGRATION', '$(sql_escape "$filename")', '$checksum', '$installed_by', $elapsed, 1)"
    done < <(find "$migration_dir" -maxdepth 1 -type f -name 'V[0-9]*__*.sql' -print0 | sort -z -V)
}

publish_frontend() {
    local source_dir="$1"
    [[ -f "$source_dir/index.html" ]] || fail "前端目录缺少 index.html：$source_dir"
    install -d -m 0755 "$WEB_ROOT"

    # 先发布哈希资源且不清除旧文件，避免在线用户加载旧页面时找不到旧 chunk。
    local source relative destination
    while IFS= read -r -d '' source; do
        relative="${source#"$source_dir"/}"
        destination="$WEB_ROOT/$relative"
        install -D -m 0644 "$source" "$destination"
    done < <(find "$source_dir" -type f ! -name 'index.html' -print0)

    install -m 0644 "$source_dir/index.html" "$WEB_ROOT/.index.html.new"
    mv -f "$WEB_ROOT/.index.html.new" "$WEB_ROOT/index.html"
}

wait_for_health() {
    local attempts="${1:-60}"
    local i
    for ((i = 1; i <= attempts; i++)); do
        if curl --fail --silent --show-error --max-time 3 "$PM_HEALTH_URL" >/dev/null 2>&1; then
            return 0
        fi
        sleep 1
    done
    return 1
}

switch_release() {
    local release_dir="$1" previous_target="${2:-}"
    ln -sfn "$release_dir" "${CURRENT_LINK}.new"
    mv -Tf "${CURRENT_LINK}.new" "$CURRENT_LINK"
    chown -h "$APP_USER:$APP_GROUP" "$CURRENT_LINK"

    systemctl enable "$SERVICE_NAME" >/dev/null 2>&1 || true
    systemctl restart "$SERVICE_NAME"
    if ! wait_for_health 60; then
        warn "新后端健康检查失败。"
        if [[ -n "$previous_target" && -d "$previous_target" ]]; then
            warn "自动切回上一后端版本：$previous_target"
            ln -sfn "$previous_target" "${CURRENT_LINK}.new"
            mv -Tf "${CURRENT_LINK}.new" "$CURRENT_LINK"
            systemctl restart "$SERVICE_NAME" || true
        fi
        journalctl -u "$SERVICE_NAME" -n 80 --no-pager >&2 || true
        fail "发布失败，前端尚未切换；数据库不会自动回滚。"
    fi
}

prune_directories() {
    local parent="$1" keep="$2"
    [[ "$parent" == "$RELEASES_DIR" || "$parent" == "$BACKUP_DIR" ]] || fail "拒绝清理非发布/备份目录：$parent"
    [[ "$keep" =~ ^[0-9]+$ ]] || fail "保留数量必须是数字：$keep"

    local count=0 path resolved_parent resolved_path
    resolved_parent="$(readlink -f "$parent")/"
    while IFS= read -r path; do
        count=$((count + 1))
        (( count <= keep )) && continue
        resolved_path="$(readlink -f "$path")"
        [[ "$resolved_path/" == "$resolved_parent"* ]] || fail "拒绝删除目录范围外路径：$resolved_path"
        if [[ -L "$CURRENT_LINK" && "$resolved_path" == "$(readlink -f "$CURRENT_LINK")" ]]; then
            continue
        fi
        rm -rf -- "$resolved_path"
    done < <(find "$parent" -mindepth 1 -maxdepth 1 -printf '%T@ %p\n' | sort -nr | cut -d' ' -f2-)
}

deploy_package() {
    require_root
    load_environment
    require_command java
    require_command mysql
    require_command tar
    require_command curl
    require_command systemctl

    local package="$1" temp_dir package_root release_id release_dir previous_target
    package="$(readlink -f "$package")"
    verify_package "$package"
    temp_dir="$(mktemp -d /tmp/pm-deploy.XXXXXX)"
    trap 'rm -rf -- "$temp_dir"' RETURN
    package_root="$(extract_package "$package" "$temp_dir")"
    release_id="$(tr -d '\r\n' < "$package_root/RELEASE_ID")"
    [[ "$release_id" =~ ^[0-9A-Za-z._-]+$ ]] || fail "非法 RELEASE_ID：$release_id"
    release_dir="$RELEASES_DIR/$release_id"
    [[ ! -e "$release_dir" ]] || fail "版本已经存在：$release_id。请更换版本号，或执行 rollback。"

    previous_target=""
    if [[ -L "$CURRENT_LINK" ]]; then previous_target="$(readlink -f "$CURRENT_LINK")"; fi

    install -d -m 0755 "$release_dir"
    cp -a "$package_root/." "$release_dir/"
    chown -R root:root "$release_dir"
    chmod 0755 "$release_dir" "$release_dir/backend"
    chmod 0644 "$release_dir/backend/ruoyi-admin.jar"

    # 补齐系统内置头像；用户上传目录独立于版本目录，升级和回滚均不会丢失。
    install -d -o "$APP_USER" -g "$APP_GROUP" -m 0750 "$UPLOAD_DIR/avatar"
    local avatar
    for avatar in default-admin.png default-male.png default-female.png; do
        if [[ -f "$release_dir/deploy/assets/$avatar" && \
              ! -f "$UPLOAD_DIR/avatar/$avatar" ]]; then
            install -o "$APP_USER" -g "$APP_GROUP" -m 0644 \
                "$release_dir/deploy/assets/$avatar" \
                "$UPLOAD_DIR/avatar/$avatar"
        fi
    done

    backup_database "$release_id"
    initialize_schema_history "$release_dir"
    apply_migrations "$release_dir"
    switch_release "$release_dir" "$previous_target"
    publish_frontend "$release_dir/frontend"

    prune_directories "$RELEASES_DIR" "$PM_KEEP_RELEASES"
    prune_directories "$BACKUP_DIR" "$PM_KEEP_BACKUPS"
    log "发布成功：$release_id"
    log "健康检查：$PM_HEALTH_URL"
}

rollback_release() {
    require_root
    load_environment
    local target_id="${1:-}" current_target target_dir
    current_target=""
    if [[ -L "$CURRENT_LINK" ]]; then current_target="$(readlink -f "$CURRENT_LINK")"; fi

    if [[ -n "$target_id" ]]; then
        [[ "$target_id" =~ ^[0-9A-Za-z._-]+$ ]] || fail "非法版本号：$target_id"
        target_dir="$RELEASES_DIR/$target_id"
    else
        target_dir="$(find "$RELEASES_DIR" -mindepth 1 -maxdepth 1 -type d -printf '%T@ %p\n' | sort -nr | cut -d' ' -f2- | grep -Fvx "$current_target" | head -n 1 || true)"
    fi

    [[ -n "$target_dir" && -d "$target_dir" ]] || fail "找不到可回滚版本。"
    [[ -f "$target_dir/backend/ruoyi-admin.jar" && -f "$target_dir/frontend/index.html" ]] || fail "目标版本不完整：$target_dir"

    warn "代码将回滚到 $(basename "$target_dir")，数据库不会自动回滚。"
    switch_release "$target_dir" "$current_target"
    publish_frontend "$target_dir/frontend"
    log "回滚成功：$(basename "$target_dir")"
}

show_status() {
    load_environment
    printf 'Current release: %s\n' "$(readlink -f "$CURRENT_LINK" 2>/dev/null || echo none)"
    printf 'Health URL: %s\n' "$PM_HEALTH_URL"
    if curl --fail --silent --show-error --max-time 3 "$PM_HEALTH_URL"; then
        printf '\n'
    else
        warn "健康检查失败。"
    fi
    systemctl status "$SERVICE_NAME" --no-pager -l || true
}

usage() {
    cat <<'EOF'
用法：
  sudo bash deploy/deploy.sh install
  sudo /opt/pm/bin/deploy.sh deploy /tmp/pm-release-<version>.tar.gz
  sudo /opt/pm/bin/deploy.sh rollback [pm-release-<version>]
  sudo /opt/pm/bin/deploy.sh status

说明：
  install  仅首次执行，安装目录、配置模板和 systemd 服务。
  deploy   备份数据库、执行未执行迁移、启动后端、健康检查、切换前端。
  rollback 回滚前后端代码，不自动回滚数据库。
EOF
}

command_name="${1:-}"
case "$command_name" in
    install) install_server ;;
    deploy) [[ $# -ge 2 ]] || fail "请提供发布包路径。"; deploy_package "$2" ;;
    rollback) rollback_release "${2:-}" ;;
    status) show_status ;;
    *) usage; [[ -n "$command_name" ]] && exit 1 || true ;;
esac
