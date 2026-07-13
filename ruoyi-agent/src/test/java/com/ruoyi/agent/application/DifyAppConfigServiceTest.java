package com.ruoyi.agent.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ruoyi.agent.domain.DifyAppConfig;
import com.ruoyi.agent.infrastructure.dify.DifyClientSettings;
import com.ruoyi.agent.mapper.DifyAppConfigMapper;
import com.ruoyi.common.exception.ServiceException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class DifyAppConfigServiceTest {
    @Test
    void shouldResolveEnabledConfigToSettings() {
        FakeMapper mapper = new FakeMapper();
        mapper.config = config("AGENT_CHAT", "", "secret", "Y");
        DifyAppConfigService service = new DifyAppConfigService(mapper);

        DifyClientSettings settings = service.findSettings("AGENT_CHAT");

        assertEquals(DifyAppConfigService.DEFAULT_API_BASE_URL, settings.baseUrl());
        assertEquals("secret", settings.apiKey());
    }

    @Test
    void shouldIgnoreMissingDisabledOrBlankKeyConfig() {
        FakeMapper mapper = new FakeMapper();
        DifyAppConfigService service = new DifyAppConfigService(mapper);
        assertNull(service.findSettings("BOM_OCR"));

        mapper.config = config("BOM_OCR", "https://example.com/v1", "secret", "N");
        assertNull(service.findSettings("BOM_OCR"));

        mapper.config = config("BOM_OCR", "https://example.com/v1", "", "Y");
        assertNull(service.findSettings("BOM_OCR"));
    }

    @Test
    void shouldPreserveMaskedApiKeyWhenUpdating() {
        FakeMapper mapper = new FakeMapper();
        mapper.config = config("AGENT_CHAT", "https://old.example/v1", "old-secret", "Y");
        mapper.config.setId(9L);
        DifyAppConfigService service = new DifyAppConfigService(mapper);
        DifyAppConfig update = config("agent_chat", "", DifyAppConfigService.MASKED_API_KEY, "");
        update.setId(9L);

        service.updateDifyAppConfig(update);

        assertEquals("AGENT_CHAT", mapper.updated.getAppCode());
        assertEquals(DifyAppConfigService.DEFAULT_API_BASE_URL, mapper.updated.getApiBaseUrl());
        assertEquals("old-secret", mapper.updated.getApiKey());
        assertEquals("Y", mapper.updated.getEnabled());
    }

    @Test
    void shouldRequireValidConfig() {
        DifyAppConfigService service = new DifyAppConfigService(new FakeMapper());

        assertThrows(ServiceException.class, () -> service.requireSettings("BOM_OCR"));
    }

    private static DifyAppConfig config(String code, String baseUrl, String apiKey, String enabled) {
        DifyAppConfig config = new DifyAppConfig();
        config.setAppCode(code);
        config.setApiBaseUrl(baseUrl);
        config.setApiKey(apiKey);
        config.setEnabled(enabled);
        return config;
    }

    private static class FakeMapper implements DifyAppConfigMapper {
        private DifyAppConfig config;
        private DifyAppConfig updated;

        public List<DifyAppConfig> selectDifyAppConfigList(DifyAppConfig value) {
            return Collections.emptyList();
        }
        public DifyAppConfig selectDifyAppConfigById(Long id) {
            return config;
        }
        public DifyAppConfig selectDifyAppConfigByCode(String appCode) {
            return config;
        }
        public int insertDifyAppConfig(DifyAppConfig value) {
            return 1;
        }
        public int updateDifyAppConfig(DifyAppConfig value) {
            updated = value;
            return 1;
        }
        public int deleteDifyAppConfigByIds(Long[] ids) {
            return 1;
        }
    }
}
