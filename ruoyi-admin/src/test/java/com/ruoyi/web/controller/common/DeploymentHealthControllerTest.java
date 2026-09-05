package com.ruoyi.web.controller.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class DeploymentHealthControllerTest {
    @Test
    void returnsUpWhenDatabaseAndRedisAreAvailable() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection databaseConnection = mock(Connection.class);
        RedisConnectionFactory redisConnectionFactory = mock(RedisConnectionFactory.class);
        RedisConnection redisConnection = mock(RedisConnection.class);
        when(dataSource.getConnection()).thenReturn(databaseConnection);
        when(databaseConnection.isValid(2)).thenReturn(true);
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenReturn("PONG");

        ResponseEntity<Map<String, Object>> response =
                new DeploymentHealthController(dataSource, redisConnectionFactory).health();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("status", "UP");
    }

    @Test
    void returnsServiceUnavailableWhenADependencyIsDown() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection databaseConnection = mock(Connection.class);
        RedisConnectionFactory redisConnectionFactory = mock(RedisConnectionFactory.class);
        RedisConnection redisConnection = mock(RedisConnection.class);
        when(dataSource.getConnection()).thenReturn(databaseConnection);
        when(databaseConnection.isValid(2)).thenReturn(true);
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenThrow(new IllegalStateException("redis unavailable"));

        ResponseEntity<Map<String, Object>> response =
                new DeploymentHealthController(dataSource, redisConnectionFactory).health();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).containsEntry("status", "DOWN");
    }
}

