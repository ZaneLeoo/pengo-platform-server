# Dify Supervisor 导入

1. 运行 `python generate_tool_openapi.py --server-url https://你的RuoYi测试域名`，生成当前环境 OpenAPI。
2. 在 Dify「工具 → 自定义工具」导入生成的 OpenAPI，鉴权类型选择 API Key，Header 为 `X-Agent-Tool-Key`，值取 RuoYi 参数 `agent.v2.tool_gateway_key`。
3. 记录 Dify 分配给该自定义工具的 provider UUID。
4. 生成当前租户可导入的 DSL：

   ```bash
   python generate_supervisor_dsl.py \
     --tool-provider-id <DIFY_CUSTOM_TOOL_PROVIDER_UUID> \
     --model-provider langgenius/deepseek/deepseek \
     --model-name deepseek-chat
   ```

5. 将生成的 `agent-v2-supervisor.generated.yml` 导入 Dify，确认画布只有「开始 → Agent Supervisor → 直接回复」，没有问题分类器。
6. 如需知识库，在 Agent 节点增加一个知识检索 Workflow-as-Tool，并命名为 `knowledge_search`；这部分依赖当前 Dify 租户的数据集 ID，不能写入通用 DSL。
7. 发布应用、生成 Service API Key，将其保存到 RuoYi 的 `AGENT_SUPERVISOR` 配置并启用。

`run_id` 与 `run_token` 已在 DSL 中绑定到 Spring 输入变量，模型不能自行伪造。工具服务密钥只保存在 Dify 自定义工具凭据和 RuoYi 参数中，不进入 DSL 或浏览器。
