# Agent V2 部署与验收

## 1. 对外工具网关

Dify Cloud 不能访问 `localhost`。将 RuoYi 部署到测试环境 HTTPS 域名，或在开发期使用经过授权的 HTTPS 隧道。只需向 Dify 暴露：

- `/internal/agent-tools/query-business-data`
- `/internal/agent-tools/analyze-dataset`
- `/internal/agent-tools/render-chart`
- `/internal/agent-tools/request-clarification`

反向代理应启用 TLS、限制请求体大小、设置合理超时并对该路径限流。不要把 `X-Agent-Tool-Key` 写入 Nginx 配置、前端环境变量或 Git；密钥只保存到 RuoYi `sys_config` 和 Dify 自定义工具凭据。

## 2. 生成环境 OpenAPI

```powershell
python ruoyi-agent/dify/generate_tool_openapi.py `
  --server-url https://mes-test.example.com
```

将生成的 `agent-v2-tools.generated.openapi.yaml` 导入 Dify 自定义工具，并设置 `X-Agent-Tool-Key`。

## 3. 生成 Supervisor DSL

取得 Dify 自定义工具 provider UUID 后：

```powershell
python ruoyi-agent/dify/generate_supervisor_dsl.py `
  --tool-provider-id <provider-uuid> `
  --model-provider langgenius/deepseek/deepseek `
  --model-name deepseek-chat
```

导入并发布生成的 DSL。确认没有问题分类器，`runId`、`runToken` 均显示为固定变量绑定，而不是由 LLM 填写。

## 4. RuoYi 应用配置

在 Dify 应用配置中编辑 `AGENT_SUPERVISOR`：

- API 地址：`https://api.dify.ai/v1`
- API Key：新 Supervisor 发布后的 Service API Key
- 状态：启用

不要复用旧分类 Chatflow 的 API Key。

## 5. 自动验收

```powershell
$password = Read-Host 'RuoYi password' -AsSecureString
./ruoyi-agent/dify/verify_agent_v2.ps1 `
  -BaseUrl https://mes-test.example.com `
  -Username admin `
  -Password $password
```

验收脚本要求：Run 正常完成；依次出现数据查询、数据分析和图表工具；至少产生一个 Artifact；SSE sequence 严格递增。
