package com.ruoyi.agent.application.run;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

/** 生成和校验一次运行专用的随机工具令牌。 */
@Component
public class AgentRunTokenService
{
    private static final int TOKEN_BYTES = 32;
    private final SecureRandom secureRandom = new SecureRandom();

    /** 生成不可预测的短期 Bearer Token。 */
    public String generate()
    {
        byte[] value = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    /** 仅持久化令牌摘要。 */
    public String hash(String token)
    {
        try
        {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormatSupport.toHex(digest);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException("当前JDK不支持SHA-256", e);
        }
    }

    /** 使用恒定时间比较，降低摘要比较的时序泄漏。 */
    public boolean matches(String token, String expectedHash)
    {
        if (token == null || expectedHash == null)
        {
            return false;
        }
        return MessageDigest.isEqual(hash(token).getBytes(StandardCharsets.US_ASCII),
            expectedHash.getBytes(StandardCharsets.US_ASCII));
    }

    /** 避免依赖仅在较新JDK中提供的HexFormat，保持模块实现稳定。 */
    private static final class HexFormatSupport
    {
        private static final char[] HEX = "0123456789abcdef".toCharArray();

        private static String toHex(byte[] value)
        {
            char[] chars = new char[value.length * 2];
            for (int index = 0; index < value.length; index++)
            {
                int item = value[index] & 0xff;
                chars[index * 2] = HEX[item >>> 4];
                chars[index * 2 + 1] = HEX[item & 0x0f];
            }
            return new String(chars);
        }
    }
}
