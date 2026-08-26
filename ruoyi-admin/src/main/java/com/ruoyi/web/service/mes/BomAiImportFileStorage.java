package com.ruoyi.web.service.mes;

import com.ruoyi.common.exception.ServiceException;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** 保存 BOM AI 导入原始图纸的本地文件存储。 */
@Component
public class BomAiImportFileStorage {
    private final Path storageRoot;

    public BomAiImportFileStorage(Environment environment) {
        String profilePath = environment.getRequiredProperty("ruoyi.profile");
        Path profile = Path.of(profilePath).toAbsolutePath().normalize();
        Path parent = profile.getParent();
        this.storageRoot =
                (parent == null
                                ? profile.resolve("bom-ai-import-store")
                                : parent.resolve("bom-ai-import-store"))
                        .normalize();
    }

    /** 持久化原始图纸，并返回受控的相对路径。 */
    public String persist(String resourceId, String extension, byte[] content) {
        Path temporary = null;
        try {
            Path directory = dailyDirectory();
            Files.createDirectories(directory);
            temporary = Files.createTempFile(directory, resourceId, ".part");
            Files.write(temporary, content);
            Path target = directory.resolve(resourceId + extension).normalize();
            ensureOwned(target);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return storageRoot.relativize(target).toString().replace('\\', '/');
        } catch (IOException exception) {
            throw new ServiceException("保存原始图纸失败，请检查服务器文件目录")
                    .setDetailMessage(exception.getMessage());
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                    // 临时文件清理失败不影响已完成的保存。
                }
            }
        }
    }

    /** 根据受控相对路径定位已保存的原始图纸。 */
    public Path resolve(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return null;
        }
        Path file = storageRoot.resolve(relativePath).normalize();
        return file.startsWith(storageRoot) && Files.isRegularFile(file) ? file : null;
    }

    private Path dailyDirectory() {
        LocalDate date = LocalDate.now();
        return storageRoot
                .resolve(String.valueOf(date.getYear()))
                .resolve(String.format("%02d", date.getMonthValue()))
                .resolve(String.format("%02d", date.getDayOfMonth()));
    }

    private void ensureOwned(Path path) throws IOException {
        if (!path.startsWith(storageRoot)) {
            throw new IOException("图纸存储路径超出受控目录");
        }
    }
}
