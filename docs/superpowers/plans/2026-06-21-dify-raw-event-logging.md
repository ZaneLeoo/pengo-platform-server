# Dify Raw SSE Event Logging Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 DEBUG 日志中安全记录 Dify 返回的每条原始 SSE 事件。

**Architecture:** 新建独立 `DifyRawEventLogger` 负责过滤空行、敏感信息脱敏、Base64 压缩和 32KB 截断。`DifyChatflowClientImpl` 在读取行后、解析前调用日志器，不改变解析和事件消费逻辑。

**Tech Stack:** Java 17、Spring、SLF4J、JUnit 5

## Global Constraints

- 仅 DEBUG 开启时处理并输出原始事件。
- 单条输出上限为 32KB。
- 日志处理不得中断 SSE 流。
- 方法保持单一职责并提供方法级注释。

---

### Task 1: 原始事件安全日志器

**Files:**
- Create: `ruoyi-agent/src/main/java/com/ruoyi/agent/infrastructure/dify/DifyRawEventLogger.java`
- Test: `ruoyi-agent/src/test/java/com/ruoyi/agent/infrastructure/dify/DifyRawEventLoggerTest.java`

**Interfaces:**
- Consumes: Dify SSE 原始 `String` 行
- Produces: `void log(String rawLine)`

- [ ] **Step 1: Write the failing tests**

覆盖普通 data 行保留、空行忽略、token/API Key/Base64 脱敏、超过 32KB 截断。

- [ ] **Step 2: Run tests to verify RED**

Run: `mvn -pl ruoyi-agent -Dtest=DifyRawEventLoggerTest test`
Expected: FAIL，因为 `DifyRawEventLogger` 尚不存在。

- [ ] **Step 3: Implement the minimal logger**

实现 `log(String)`、安全格式化和 32KB 上限；仅在 `LOGGER.isDebugEnabled()` 时格式化。

- [ ] **Step 4: Run tests to verify GREEN**

Run: `mvn -pl ruoyi-agent -Dtest=DifyRawEventLoggerTest test`
Expected: PASS。

### Task 2: 接入 Dify 流读取链路

**Files:**
- Modify: `ruoyi-agent/src/main/java/com/ruoyi/agent/infrastructure/dify/DifyChatflowClientImpl.java`
- Modify: `ruoyi-agent/src/test/java/com/ruoyi/agent/infrastructure/dify/DifyChatflowClientImplTest.java`

**Interfaces:**
- Consumes: `DifyRawEventLogger.log(String rawLine)`
- Produces: 每个非空 SSE 原始行在解析前交给日志器

- [ ] **Step 1: Write a failing integration test**

注入可观察日志器，断言 `data:` 与 `event: ping` 均被接收且业务事件仍正常消费。

- [ ] **Step 2: Run test to verify RED**

Run: `mvn -pl ruoyi-agent -Dtest=DifyChatflowClientImplTest test`
Expected: FAIL，因为客户端尚未调用日志器。

- [ ] **Step 3: Inject and invoke the logger**

扩展构造器依赖，并在 `reader.readLine()` 后、`parser.parseDataLine()` 前调用 `log`。

- [ ] **Step 4: Run full verification**

Run: `mvn -pl ruoyi-admin -am test`
Expected: BUILD SUCCESS，全部测试通过。
