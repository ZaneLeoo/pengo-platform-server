package com.ruoyi.web.service.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ruoyi.agent.api.AgentFileView;
import com.ruoyi.agent.domain.AgentFile;
import com.ruoyi.agent.mapper.AgentFileMapper;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentFileServiceTest {
    private static final String RESOURCE_ID = "5924e975-b3fe-406a-8615-c4b6ba74f073";
    @Mock
    private AgentFileMapper fileMapper;

    @Mock
    private AgentFileStorage fileStorage;

    private AgentFileService service;

    @BeforeEach
    void setUp() {
        service = new AgentFileService(fileMapper, fileStorage);
    }

    @Test
    void shouldResolveOwnedFileFromDatabaseAndStorage() {
        AgentFile metadata = file(RESOURCE_ID);
        Path path = Path.of("D:/files/" + RESOURCE_ID + ".xlsx");
        when(fileMapper.selectOwned(RESOURCE_ID, 7L)).thenReturn(metadata);
        when(fileStorage.resolve("outputs/resource-1.xlsx")).thenReturn(path);

        AgentFileService.StoredFile result = service.findOwned(RESOURCE_ID, 7L);

        assertThat(result).isNotNull();
        assertThat(result.getPath()).isEqualTo(path);
        assertThat(result.getName()).isEqualTo("库存报表.xlsx");
    }

    @Test
    void shouldExposeSafeFileViewWithoutStoragePath() {
        when(fileMapper.selectByUserId(7L)).thenReturn(List.of(file(RESOURCE_ID)));

        List<AgentFileView> result = service.listOwned(7L);

        assertThat(result).singleElement().satisfies(file -> {
            assertThat(file.getResourceId()).isEqualTo(RESOURCE_ID);
            assertThat(file.getDownloadUrl()).isEqualTo("/agent/files/" + RESOURCE_ID);
            assertThat(file.getPreview()).isEqualTo("download");
        });
    }

    @Test
    void shouldMarkOwnedFileDeletedAndRemoveContent() throws Exception {
        AgentFile metadata = file(RESOURCE_ID);
        when(fileMapper.selectOwned(RESOURCE_ID, 7L)).thenReturn(metadata);
        when(fileMapper.markDeleted(RESOURCE_ID, 7L, "admin")).thenReturn(1);

        boolean deleted = service.deleteOwned(RESOURCE_ID, 7L, "admin");

        assertThat(deleted).isTrue();
        verify(fileStorage).delete("outputs/resource-1.xlsx");
    }

    private AgentFile file(String resourceId) {
        AgentFile file = new AgentFile();
        file.setResourceId(resourceId);
        file.setUserId(7L);
        file.setFileName("库存报表.xlsx");
        file.setRelativePath("outputs/resource-1.xlsx");
        file.setExtension("xlsx");
        file.setMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        file.setFileKind("spreadsheet");
        file.setFileSize(1024L);
        file.setPreviewMode("DOWNLOAD");
        file.setCreateTime(new Date());
        return file;
    }
}
