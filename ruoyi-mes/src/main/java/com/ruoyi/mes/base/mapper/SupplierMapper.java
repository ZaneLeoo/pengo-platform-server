package com.ruoyi.mes.base.mapper;

import com.ruoyi.mes.base.domain.Supplier;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/** 供应商档案数据访问接口。 */
public interface SupplierMapper {

    List<Supplier> selectList(Supplier q);

    /** 查询供 AI 工具使用的供应商列表，返回数量由 SQL 控制在安全范围内。 */
    List<Supplier> selectListForAgent(@Param("keyword") String keyword, @Param("status") String status);

    Supplier selectById(Long id);

    int insert(Supplier o);

    int update(Supplier o);

    int deleteByIds(Long[] ids);
}
