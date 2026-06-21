# Dify 原始 SSE 事件日志设计

## 目标

在后端调试期间逐条查看 Dify Chatflow 返回的原始 SSE 事件，同时不改变现有事件解析、业务处理和前端流式转发行为。

## 设计

- 在 Dify HTTP 客户端读取每个 SSE 行后、解析之前记录原始内容。
- 使用独立的 `DifyRawEventLogger`，保持 HTTP 客户端与日志安全处理职责分离。
- 仅在该日志器的 DEBUG 级别启用时执行格式化与输出，默认不增加生产日志量。
- 保留 `event:`、`data:` 和无法解析的原始行；空行不记录。
- 对 Authorization、API Key、token、文件 Base64 等敏感或大体积字段脱敏。
- 单条日志最长 32KB，超过部分以截断标记替代，防止日志放大和内存占用。
- 日志异常不得中断 Dify SSE 的读取、解析或转发。

## 配置与输出

临时查看时将以下 logger 调整为 DEBUG：

```yaml
logging:
  level:
    com.ruoyi.agent.infrastructure.dify.DifyRawEventLogger: debug
```

输出格式：

```text
Dify SSE raw: data: {"event":"node_started",...}
```

## 测试

- 原始事件能够原样进入安全日志格式化流程。
- 敏感字段和 Base64 内容会被脱敏。
- 超过 32KB 的事件会被截断。
- 空行不会输出。
- 现有 Dify SSE 解析及客户端测试全部保持通过。
