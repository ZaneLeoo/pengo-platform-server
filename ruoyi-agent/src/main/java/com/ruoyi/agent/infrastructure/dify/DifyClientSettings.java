package com.ruoyi.agent.infrastructure.dify;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 一次 Dify 调用所需配置。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DifyClientSettings {
    private String baseUrl;
    private String apiKey;

}
