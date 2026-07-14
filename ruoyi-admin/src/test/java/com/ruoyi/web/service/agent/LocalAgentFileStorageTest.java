package com.ruoyi.web.service.agent;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.env.MockEnvironment;

class LocalAgentFileStorageTest {
    @TempDir
    private Path temporaryDirectory;

    @Test
    void shouldReadProfileFromEnvironmentAndPersistFile() throws Exception {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("ruoyi.profile", temporaryDirectory.resolve("uploadPath").toString());
        LocalAgentFileStorage storage = new LocalAgentFileStorage(environment);
        Path temporary = storage.createTemporary("resource-1");
        Files.writeString(temporary, "agent file");

        String relativePath = storage.persist(temporary, "resource-1", ".txt");

        assertThat(relativePath).contains("outputs/");
        assertThat(storage.resolve(relativePath)).hasContent("agent file");
    }
}
