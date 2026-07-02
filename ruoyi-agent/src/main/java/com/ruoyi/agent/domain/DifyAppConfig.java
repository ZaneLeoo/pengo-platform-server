package com.ruoyi.agent.domain;

import com.ruoyi.common.core.domain.BaseEntity;

/** Dify 应用配置。 */
public class DifyAppConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 配置 ID。 */
    private Long id;

    /** 应用编码，例如 AGENT_CHAT、BOM_OCR。 */
    private String appCode;

    /** 应用名称。 */
    private String appName;

    /** 应用类型，例如 chatflow、workflow。 */
    private String appType;

    /** Dify API 地址。 */
    private String apiBaseUrl;

    /** Dify API Key。 */
    private String apiKey;

    /** 是否启用：Y 启用，N 停用。 */
    private String enabled;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAppCode() { return appCode; }
    public void setAppCode(String appCode) { this.appCode = appCode; }

    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }

    public String getAppType() { return appType; }
    public void setAppType(String appType) { this.appType = appType; }

    public String getApiBaseUrl() { return apiBaseUrl; }
    public void setApiBaseUrl(String apiBaseUrl) { this.apiBaseUrl = apiBaseUrl; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getEnabled() { return enabled; }
    public void setEnabled(String enabled) { this.enabled = enabled; }
}
