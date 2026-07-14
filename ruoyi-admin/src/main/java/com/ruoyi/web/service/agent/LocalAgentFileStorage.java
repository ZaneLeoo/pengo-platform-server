package com.ruoyi.web.service.agent;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** 使用 RuoYi 数据目录保存 Agent 文件内容。 */
@Component
public class LocalAgentFileStorage implements AgentFileStorage {
    private final Path storageRoot;

    public LocalAgentFileStorage(Environment environment) {
        String profilePath = environment.getRequiredProperty("ruoyi.profile");
        Path profile = Path.of(profilePath).toAbsolutePath().normalize();
        Path parent = profile.getParent();
        this.storageRoot = (parent == null ? profile.resolve("agent-file-store") : parent.resolve("agent-file-store"))
                .normalize();
    }

    @Override
    public Path createTemporary(String resourceId) throws IOException {
        Path directory = dailyDirectory();
        Files.createDirectories(directory);
        return Files.createTempFile(directory, resourceId, ".part");
    }

    @Override
    public String persist(Path temporary, String resourceId, String extension) throws IOException {
        Path directory = dailyDirectory();
        Files.createDirectories(directory);
        Path target = directory.resolve(resourceId + extension).normalize();
        ensureOwned(target);
        try {
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return storageRoot.relativize(target).toString().replace('\\', '/');
    }

    @Override
    public Path resolve(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return null;
        }
        Path file = storageRoot.resolve(relativePath).normalize();
        return file.startsWith(storageRoot) && Files.isRegularFile(file) ? file : null;
    }

    @Override
    public void delete(String relativePath) throws IOException {
        Path file = storageRoot.resolve(relativePath == null ? "" : relativePath).normalize();
        ensureOwned(file);
        Files.deleteIfExists(file);
    }

    private Path dailyDirectory() {
        LocalDate date = LocalDate.now();
        return storageRoot.resolve("outputs").resolve(String.valueOf(date.getYear()))
                .resolve(String.format("%02d", date.getMonthValue()))
                .resolve(String.format("%02d", date.getDayOfMonth()));
    }

    private void ensureOwned(Path path) throws IOException {
        if (!path.startsWith(storageRoot)) {
            throw new IOException("文件路径超出 Agent 存储目录");
        }
    }
}
