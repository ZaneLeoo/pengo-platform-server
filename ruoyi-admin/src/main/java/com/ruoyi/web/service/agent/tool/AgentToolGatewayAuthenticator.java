package com.ruoyi.web.service.agent.tool;

import com.ruoyi.agent.domain.enums.AgentV2ConfigKey;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.service.ISysConfigService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.stereotype.Service;

/** 校验 Dify 到 Spring 内部工具网关的服务级凭据。 */
@Service
public class AgentToolGatewayAuthenticator
{
    private final ISysConfigService configService;

    public AgentToolGatewayAuthenticator(ISysConfigService configService)
    {
        this.configService = configService;
    }

    /** 服务密钥和运行令牌形成双重校验；此处只处理服务密钥。 */
    public void requireValid(String providedKey)
    {
        String expected = configService.selectConfigByKey(AgentV2ConfigKey.TOOL_GATEWAY_KEY.getKey());
        if (expected == null || expected.isBlank())
        {
            throw new ServiceException("Agent V2工具网关尚未配置");
        }
        if (providedKey == null || !MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
            providedKey.getBytes(StandardCharsets.UTF_8)))
        {
            throw new ServiceException("Agent V2工具网关认证失败");
        }
    }
}
