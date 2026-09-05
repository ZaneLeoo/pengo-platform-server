# Docker Compose 隔离部署

该方案为项目管理系统单独运行 MySQL、Redis、Spring Boot 和 Nginx，只暴露宿主机 `9000` 端口，不使用宿主机已有的 3306/6379，也不会修改已有微服务。

目录约定为 `/opt/pm-docker`：

- `current`：指向当前发布版本的软链接；
- `releases/<release-id>`：解压后的不可变发布目录；
- `database/init`：仅在 MySQL 数据卷首次创建时导入的结构和数据基线；
- `data/uploads`、`data/logs`：持久化附件和日志；
- `.env`：服务器本地密钥，不纳入版本控制。

首次启动：

```bash
docker compose -f compose.yml pull
docker compose -f compose.yml up -d
docker compose -f compose.yml ps
curl -fsS http://127.0.0.1:9000/dev-api/health
```

把发布包中的 `deploy/docker/pmctl.sh` 复制到部署根目录后，日常发布可简化为：

```bash
cp current/deploy/docker/pmctl.sh ./pmctl.sh
chmod 700 ./pmctl.sh
./pmctl.sh deploy /www/wwwroot/pm/pm-release-<version>.tar.gz
```

发布更新时只需解压新包、执行尚未应用的 `database/migrations/*.sql`、切换 `current` 软链接并重新创建 `backend`、`web`：

```bash
docker compose -f compose.yml up -d --force-recreate backend web
```

回滚代码时把 `current` 指回上一个 release，再执行同一条重建命令。数据库迁移必须提供对应的回滚说明，不能靠删除数据卷回滚。
