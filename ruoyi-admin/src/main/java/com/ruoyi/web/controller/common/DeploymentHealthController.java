package com.ruoyi.web.controller.common;

import java.sql.Connection;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** 为部署脚本和负载均衡提供不含敏感信息的应用健康检查。 */
@RestController
public class DeploymentHealthController {
    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;

    public DeploymentHealthController(
            DataSource dataSource, RedisConnectionFactory redisConnectionFactory) {
        this.dataSource = dataSource;
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean databaseUp = isDatabaseUp();
        boolean redisUp = isRedisUp();
        boolean healthy = databaseUp && redisUp;

        Map<String, String> components = new LinkedHashMap<>();
        components.put("database", databaseUp ? "UP" : "DOWN");
        components.put("redis", redisUp ? "UP" : "DOWN");

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", healthy ? "UP" : "DOWN");
        body.put("components", components);
        body.put("timestamp", OffsetDateTime.now().toString());

        return ResponseEntity.status(healthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
                .body(body);
    }

    private boolean isDatabaseUp() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isRedisUp() {
        RedisConnection connection = null;
        try {
            connection = redisConnectionFactory.getConnection();
            return "PONG".equalsIgnoreCase(connection.ping());
        } catch (Exception ignored) {
            return false;
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }
}

