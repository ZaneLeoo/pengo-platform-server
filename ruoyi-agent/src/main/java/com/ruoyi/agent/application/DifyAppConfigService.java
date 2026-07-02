package com.ruoyi.agent.application;

import com.ruoyi.agent.domain.DifyAppConfig;
import com.ruoyi.agent.infrastructure.dify.DifyClientSettings;
import com.ruoyi.agent.mapper.DifyAppConfigMapper;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import java.util.List;
import org.springframework.stereotype.Service;

/** Dify 多应用配置服务。 */
@Service
public class DifyAppConfigService
{
    public static final String DEFAULT_API_BASE_URL = "https://api.dify.ai/v1";
    public static final String MASKED_API_KEY = "******";

    private final DifyAppConfigMapper configMapper;

    public DifyAppConfigService(DifyAppConfigMapper configMapper)
    {
        this.configMapper = configMapper;
    }

    /** 查询配置列表。 */
    public List<DifyAppConfig> selectDifyAppConfigList(DifyAppConfig config)
    {
        return configMapper.selectDifyAppConfigList(config);
    }

    /** 查询配置详情。 */
    public DifyAppConfig selectDifyAppConfigById(Long id)
    {
        return configMapper.selectDifyAppConfigById(id);
    }

    /** 新增配置。 */
    public int insertDifyAppConfig(DifyAppConfig config)
    {
        normalize(config);
        return configMapper.insertDifyAppConfig(config);
    }

    /** 更新配置。 */
    public int updateDifyAppConfig(DifyAppConfig config)
    {
        normalize(config);
        if (MASKED_API_KEY.equals(config.getApiKey()))
        {
            DifyAppConfig old = configMapper.selectDifyAppConfigById(config.getId());
            config.setApiKey(old == null ? null : old.getApiKey());
        }
        return configMapper.updateDifyAppConfig(config);
    }

    /** 删除配置。 */
    public int deleteDifyAppConfigByIds(Long[] ids)
    {
        return configMapper.deleteDifyAppConfigByIds(ids);
    }

    /** 查询可用应用设置；不存在、未启用或未配置密钥时返回 null，便于调用方兜底。 */
    public DifyClientSettings findSettings(String appCode)
    {
        DifyAppConfig config = configMapper.selectDifyAppConfigByCode(appCode);
        if (!isUsable(config))
        {
            return null;
        }
        return toSettings(config);
    }

    /** 查询必需应用设置；缺失时抛出可读错误。 */
    public DifyClientSettings requireSettings(String appCode)
    {
        DifyAppConfig config = configMapper.selectDifyAppConfigByCode(appCode);
        if (config == null)
        {
            throw new ServiceException("请先配置 Dify 应用：" + appCode);
        }
        if (!"Y".equals(config.getEnabled()))
        {
            throw new ServiceException("Dify 应用未启用：" + appCode);
        }
        if (StringUtils.isBlank(config.getApiKey()))
        {
            throw new ServiceException("请先配置 Dify API Key：" + appCode);
        }
        return toSettings(config);
    }

    private boolean isUsable(DifyAppConfig config)
    {
        return config != null && "Y".equals(config.getEnabled()) && StringUtils.isNotBlank(config.getApiKey());
    }

    private DifyClientSettings toSettings(DifyAppConfig config)
    {
        String baseUrl = StringUtils.isBlank(config.getApiBaseUrl()) ? DEFAULT_API_BASE_URL : config.getApiBaseUrl();
        return new DifyClientSettings(baseUrl, config.getApiKey());
    }

    private void normalize(DifyAppConfig config)
    {
        if (StringUtils.isNotBlank(config.getAppCode()))
        {
            config.setAppCode(config.getAppCode().trim().toUpperCase());
        }
        if (StringUtils.isBlank(config.getApiBaseUrl()))
        {
            config.setApiBaseUrl(DEFAULT_API_BASE_URL);
        }
        if (StringUtils.isBlank(config.getEnabled()))
        {
            config.setEnabled("Y");
        }
    }
}
