/**
 * AI 自动化业务领域。
 *
 * <p>这里承载由智能助手发起、但必须受企业业务规则约束的业务动作，例如采购建单、库存预警处理、
 * 单据草稿生成和文档生成。它不负责 Dify 协议解析，也不直接承担 HTTP 接口职责。</p>
 *
 * <p>自动化能力统一遵循“准备 - 用户确认 - 执行”的边界：准备阶段只读取并校验业务数据；
 * 执行阶段由当前登录用户明确确认后创建草稿或提交动作。具体业务能力按
 * {@code application}、{@code domain}、{@code infrastructure} 分层扩展。</p>
 */
package com.ruoyi.agent.business.automation;
