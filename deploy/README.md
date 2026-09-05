# 项目管理系统 Linux 发布说明

## 1. 什么是发布包

发布包是一次前后端版本的完整、不可变交付物，文件名类似：

```text
pm-release-20260904-test01.tar.gz
pm-release-20260904-test01.tar.gz.sha256
```

压缩包包含：

```text
pm-release-<version>/
├── backend/ruoyi-admin.jar
├── frontend/                 # Vite dist
├── database/
│   ├── bootstrap_schema_history.sql
│   ├── migrations/           # V0056__xxx.sql 等增量脚本
│   └── baseline.sql          # 仅指定 -BaselineSqlPath 时存在
├── deploy/
│   ├── deploy.sh
│   ├── templates/
│   └── docker/               # Compose 隔离部署模板
├── RELEASE_ID
├── RELEASE.txt               # 构建时间及前后端 Git 提交号
└── DEPLOYMENT.md
```

服务器不需要安装 Node.js、pnpm 或 Maven，只需要运行构建完成的静态文件和 JAR。

当前测试服务器采用 `deploy/docker/compose.yml`：项目独享 MySQL、Redis、后端和 Nginx，仅映射宿主机 `9000`，不会使用或污染宿主机已有的 3306/6379 数据。

## 2. 本地构建发布包

在工作区根目录 `C:\Work\Code\Test-Platform` 执行：

```powershell
pwsh -File .\Backend\deploy\build-release.ps1 `
  -Version 20260904-test01 `
  -MavenCommand C:\App\Dev\Tools\Maven\apache-maven-3.9.16\bin\mvn.cmd
```

常用选项：

```powershell
# node_modules 已安装时跳过 pnpm install，加快构建
-SkipFrontendInstall

# 仅在紧急测试发布时跳过后端测试
-SkipBackendTests

# 首次安装时把已验收的 pm 完整导出一并放入包内
-BaselineSqlPath C:\backup\pm-baseline.sql
```

输出目录：

```text
Backend/deploy/output/
```

每次发布必须使用新的版本号，禁止覆盖服务器上已经存在的版本。

### 一键构建并发布

服务器已经配置长期部署密钥后，可以使用带中文注释的 `release.ps1`：

```powershell
pwsh -File .\Backend\deploy\release.ps1 `
  -Version 20260905-test01 `
  -IdentityFile C:\Keys\pm-deploy-ed25519 `
  -SkipFrontendInstall
```

该脚本会自动上传发布包，并调用服务器部署根目录中的 `pmctl.sh` 完成数据库备份、增量迁移、版本替换和健康检查。

## 3. 服务器要求

- 推荐 Ubuntu 22.04/24.04 或兼容发行版。
- Java 17。
- MySQL 8.x、Redis、Nginx（可以由宝塔安装和管理）。
- Linux 命令：`mysql`、`mysqldump`、`tar`、`gzip`、`curl`、`sha256sum`、`systemctl`。
- 仅向公网开放 80/443；8088、3306、6379只监听本机或内网。

建议建立两个数据库账号：

- `pm_app`：后端运行账号，只授予业务所需的查询和增删改权限。
- `pm_deploy`：部署专用账号，负责备份、建表和执行增量 DDL；凭据只保存在 `/etc/pm/pm.env`。

测试阶段也可以先让两个配置使用同一个数据库账号，但不建议长期使用 MySQL `root`。

## 4. 第一次安装

上传发布包和校验文件：

```bash
scp Backend/deploy/output/pm-release-20260904-test01.tar.gz* root@服务器IP:/tmp/
```

登录服务器并解压安装器：

```bash
cd /tmp
tar -xzf pm-release-20260904-test01.tar.gz
sudo bash pm-release-20260904-test01/deploy/deploy.sh install
```

编辑配置：

```bash
sudo nano /etc/pm/pm.env
sudo chmod 600 /etc/pm/pm.env
sudo nano /etc/pm/application-prod.yml
```

至少修改所有 `CHANGE_ME`，Token 密钥可用以下命令生成：

```bash
openssl rand -base64 48
```

### 初始数据库

当前历史 SQL 不能直接在空库中按 01-55 全量重放。首次部署有两种方式：

**方式 A（推荐）：提前恢复已验收的 pm 数据库备份**

```bash
mysql -u root -p -e "CREATE DATABASE pm DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p pm < /安全目录/pm-baseline.sql
```

**方式 B：构建时通过 `-BaselineSqlPath` 将基线放进发布包**

目标数据库必须已经创建且为空，`deploy.sh` 会自动导入 `database/baseline.sql`。

配置宝塔 Nginx：把以下模板中的 `server_name` 改成实际域名，然后合并到宝塔站点：

```text
/opt/pm/share/nginx-pm.conf.example
```

检查并重新加载：

```bash
nginx -t && nginx -s reload
```

最后执行首次发布：

```bash
sudo /opt/pm/bin/deploy.sh deploy /tmp/pm-release-20260904-test01.tar.gz
```

## 5. 后续更新

本地重新构建一个新版本并上传，然后服务器只执行：

```bash
sudo /opt/pm/bin/deploy.sh deploy /tmp/pm-release-20260905-test02.tar.gz
```

部署脚本依次执行：

1. 校验 SHA-256。
2. 完整备份当前数据库。
3. 创建并检查 `pm_schema_history`。
4. 按版本执行尚未执行的 `Vxxxx__description.sql`。
5. 切换后端版本并重启 systemd 服务。
6. 调用 `/health` 验证 MySQL、Redis和应用。
7. 健康后再切换前端入口文件。
8. 保留最近若干发布版本和数据库备份。

查看状态：

```bash
sudo /opt/pm/bin/deploy.sh status
journalctl -u pm-backend -f
```

## 6. 数据库增量脚本

新增脚本放在：

```text
Backend/deploy/database/migrations/
```

命名示例：

```text
V0056__add_project_reminder.sql
V0057__add_notification_setting.sql
```

发布成功后可查询：

```sql
SELECT version, description, script, checksum, installed_on
FROM pm_schema_history
ORDER BY installed_rank;
```

已发布脚本绝对不能修改。脚本校验和发生变化时，部署会主动终止。

## 7. 回滚

回滚到上一个代码版本：

```bash
sudo /opt/pm/bin/deploy.sh rollback
```

指定版本：

```bash
sudo /opt/pm/bin/deploy.sh rollback pm-release-20260904-test01
```

回滚会切换前端和后端，但**不会自动逆向修改数据库**。如果新迁移与旧代码不兼容，需要使用发布前生成的 `/var/backups/pm/*.sql.gz` 恢复数据库，并先停止后端：

```bash
sudo systemctl stop pm-backend
gunzip -c /var/backups/pm/备份文件.sql.gz | mysql -u root -p pm
sudo /opt/pm/bin/deploy.sh rollback pm-release-目标版本
```

## 8. 目录说明

```text
/opt/pm/bin/deploy.sh          一键部署脚本
/opt/pm/releases/              后端和前端历史发布版本
/opt/pm/current                当前后端版本软链接
/opt/pm/share/                 Nginx 示例配置
/etc/pm/                       服务器配置和密钥
/var/www/pm/                   当前前端静态文件
/data/pm/uploads/              永久附件目录，发布时不会覆盖
/var/log/pm/                   后端日志
/var/backups/pm/               发布前数据库备份
```
