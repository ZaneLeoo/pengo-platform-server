package com.ruoyi.projectmanagement.deliverable.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.projectmanagement.deliverable.domain.ProjectDeliverableType;
import com.ruoyi.projectmanagement.deliverable.mapper.ProjectDeliverableTypeMapper;
import com.ruoyi.projectmanagement.deliverable.service.IProjectDeliverableTypeService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectDeliverableTypeServiceImpl implements IProjectDeliverableTypeService {

    private final ProjectDeliverableTypeMapper mapper;

    public ProjectDeliverableTypeServiceImpl(ProjectDeliverableTypeMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<ProjectDeliverableType> list(ProjectDeliverableType filter) {
        return mapper.selectList(filter);
    }

    @Override
    public ProjectDeliverableType get(Long id) {
        ProjectDeliverableType entity = mapper.selectById(id);
        if (entity == null) throw new ServiceException("交付物类型不存在");
        return entity;
    }

    @Override
    @Transactional
    public int add(ProjectDeliverableType entity, String operator) {
        if (mapper.selectByCode(entity.getTypeCode()) != null)
            throw new ServiceException("交付物类型编码已存在");
        normalize(entity);
        int rows = mapper.insert(entity);
        saveFormats(entity);
        return rows;
    }

    @Override
    @Transactional
    public int edit(ProjectDeliverableType entity, String operator) {
        ProjectDeliverableType old = get(entity.getTypeId());
        if (!old.getTypeCode().equals(entity.getTypeCode())
                && mapper.selectByCode(entity.getTypeCode()) != null) {
            throw new ServiceException("交付物类型编码已存在");
        }
        normalize(entity);
        int rows = mapper.update(entity);
        mapper.deleteFormats(entity.getTypeId());
        saveFormats(entity);
        return rows;
    }

    @Override
    @Transactional
    public int remove(Long id) {
        mapper.deleteFormats(id);
        return mapper.deleteById(id);
    }

    private void normalize(ProjectDeliverableType entity) {
        entity.setTypeCode(entity.getTypeCode().trim().toUpperCase());
        entity.setTypeName(entity.getTypeName().trim());
        entity.setSubmissionMode(entity.getSubmissionMode().trim().toUpperCase());
        if (!List.of("FILE", "LINK").contains(entity.getSubmissionMode())) {
            throw new ServiceException("V1仅支持文件或外链提交方式");
        }
        if (entity.getDefaultApprovalRequired() == null) entity.setDefaultApprovalRequired("0");
        if (entity.getStatus() == null) entity.setStatus("0");
        if (entity.getSortOrder() == null) entity.setSortOrder(0);
        if ("LINK".equals(entity.getSubmissionMode())) entity.setAllowedExtensions(List.of());
    }

    private void saveFormats(ProjectDeliverableType entity) {
        for (String extension :
                entity.getAllowedExtensions() == null
                        ? List.<String>of()
                        : entity.getAllowedExtensions()) {
            String normalized =
                    extension == null
                            ? ""
                            : extension.trim().replaceFirst("^\\.", "").toLowerCase();
            if (!normalized.isBlank()) mapper.insertFormat(entity.getTypeId(), normalized);
        }
    }
}
