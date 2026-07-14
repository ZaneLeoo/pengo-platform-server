package com.ruoyi.agent.mapper;

import com.ruoyi.agent.domain.AgentFile;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** Agent 文件元数据访问。 */
@Mapper
public interface AgentFileMapper {
    /** 新增文件元数据。 */
    int insert(AgentFile file);

    /** 查询当前用户拥有的可用文件。 */
    AgentFile selectOwned(@Param("resourceId") String resourceId, @Param("userId") Long userId);

    /** 查询当前用户的文件，按创建时间倒序排列。 */
    List<AgentFile> selectByUserId(Long userId);

    /** 将当前用户拥有的文件标记为已删除。 */
    int markDeleted(@Param("resourceId") String resourceId, @Param("userId") Long userId,
            @Param("updateBy") String updateBy);
}
