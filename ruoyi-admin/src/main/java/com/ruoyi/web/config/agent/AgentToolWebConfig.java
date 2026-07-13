package com.ruoyi.web.config.agent;

import com.ruoyi.web.interceptor.agent.AgentToolKeyInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Agent 工具 Web 入口配置。 */
@Configuration
public class AgentToolWebConfig implements WebMvcConfigurer
{
    private final AgentToolKeyInterceptor toolKeyInterceptor;

    public AgentToolWebConfig(AgentToolKeyInterceptor toolKeyInterceptor)
    {
        this.toolKeyInterceptor = toolKeyInterceptor;
    }

    /** 保证新增到 /agent/tools/** 的接口默认受到工具密钥保护。 */
    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(toolKeyInterceptor).addPathPatterns("/agent/tools/**");
    }
}
