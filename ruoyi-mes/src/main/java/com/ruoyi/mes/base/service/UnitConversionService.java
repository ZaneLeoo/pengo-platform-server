package com.ruoyi.mes.base.service;

import com.ruoyi.mes.base.domain.Material;
import com.ruoyi.mes.base.domain.UnitConversionFormula;
import com.ruoyi.mes.base.domain.UnitGroup;
import com.ruoyi.mes.base.domain.UnitGroupDetail;
import com.ruoyi.mes.base.dto.ConversionRequest;
import com.ruoyi.mes.base.dto.ConversionResult;
import com.ruoyi.mes.base.engine.FormulaEngine;
import com.ruoyi.mes.base.mapper.MaterialMapper;
import com.ruoyi.mes.base.mapper.UnitGroupDetailMapper;
import com.ruoyi.mes.base.mapper.UnitGroupMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 换算图引擎。
 * <p>给定物料、录入单位和数量，基于单位组中的公式边返回所有可达单位的结果。</p>
 */
@Service
public class UnitConversionService {

    @Autowired
    private MaterialMapper materialMapper;
    @Autowired
    private UnitGroupMapper unitGroupMapper;
    @Autowired
    private UnitGroupDetailMapper detailMapper;
    @Autowired
    private FormulaEngine formulaEngine;

    /**
     * 核心入口：给定物料ID、录入单位和数量，返回单位组内所有可达单位的结果。
     */
    public Map<String, ConversionResult> calculateAllUnits(ConversionRequest request) {
        if (request == null || request.getMaterialId() == null) {
            throw new IllegalArgumentException("物料不能为空");
        }
        Material material = materialMapper.selectMaterialById(request.getMaterialId());
        if (material == null) {
            throw new IllegalArgumentException("物料不存在: " + request.getMaterialId());
        }
        return calculate(material, request.getInputUnitCode(),
                request.getInputQuantity(), request.getRuntimeOverrides());
    }

    /** 获取物料绑定的单位组明细，供业务单据回显和服务端保存时换算使用。 */
    public List<UnitGroupDetail> getUnitDetails(Long materialId) {
        Material material = materialMapper.selectMaterialById(materialId);
        if (material == null) {
            throw new IllegalArgumentException("物料不存在: " + materialId);
        }
        UnitGroup group = findGroup(material);
        List<UnitGroupDetail> details = detailMapper.selectByGroupId(group.getId());
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("计量单位组没有单位明细: " + group.getGroupCode());
        }
        return details;
    }

    /**
     * 基于公式关系进行 BFS 换算。
     * <p>单位组内没有中心单位，输入单位可以是任意成员，结果返回所有可达成员的数量。</p>
     */
    public Map<String, ConversionResult> calculate(Material material, String inputUnitCode,
                                                    BigDecimal inputQuantity,
                                                    Map<String, BigDecimal> overrides) {
        if (material == null) {
            throw new IllegalArgumentException("物料不能为空");
        }
        if (inputUnitCode == null || inputUnitCode.isBlank()) {
            throw new IllegalArgumentException("录入单位不能为空");
        }
        if (inputQuantity == null || inputQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("录入数量必须大于0");
        }

        UnitGroup group = findGroup(material);
        List<UnitGroupDetail> details = detailMapper.selectByGroupId(group.getId());
        if (details == null || details.isEmpty()) {
            throw new IllegalArgumentException("计量单位组没有单位明细: " + group.getGroupCode());
        }

        Map<String, Node> nodeMap = new LinkedHashMap<>();
        for (UnitGroupDetail detail : details) {
            nodeMap.put(detail.getUnitCode(), new Node(detail));
        }

        Node inputNode = nodeMap.get(inputUnitCode);
        if (inputNode == null) {
            throw new IllegalArgumentException("录入单位 " + inputUnitCode
                    + " 不在单位组 " + group.getGroupCode() + " 中");
        }
        Map<String, BigDecimal> quantities = new LinkedHashMap<>();
        Map<String, String> paths = new LinkedHashMap<>();
        quantities.put(inputUnitCode, inputQuantity);
        paths.put(inputUnitCode, inputUnitCode);

        Deque<String> queue = new ArrayDeque<>();
        queue.add(inputUnitCode);
        while (!queue.isEmpty()) {
            String fromCode = queue.poll();
            BigDecimal fromQuantity = quantities.get(fromCode);
            Node fromNode = nodeMap.get(fromCode);
            for (Node toNode : nodeMap.values()) {
                String toCode = toNode.detail.getUnitCode();
                if (quantities.containsKey(toCode)) {
                    continue;
                }
                BigDecimal toQuantity = convert(fromNode, toNode, group.getId(), material,
                        fromQuantity, overrides);
                if (toQuantity == null || toQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                quantities.put(toCode, toQuantity);
                paths.put(toCode, paths.get(fromCode) + "→" + toCode);
                queue.add(toCode);
            }
        }

        Map<String, ConversionResult> resultMap = new LinkedHashMap<>();
        for (Node node : nodeMap.values()) {
            String unitCode = node.detail.getUnitCode();
            BigDecimal quantity = quantities.getOrDefault(unitCode, BigDecimal.ZERO);
            ConversionResult result = new ConversionResult();
            result.setUnitCode(unitCode);
            result.setUnitName(node.detail.getUnitName());
            result.setQuantity(quantity);
            result.setConversionPath(paths.getOrDefault(unitCode, ""));
            resultMap.put(unitCode, result);
        }
        return resultMap;
    }

    /** 直接公式优先；没有直接公式时使用目标单位公式的反向边。 */
    private BigDecimal convert(Node fromNode, Node toNode, Long groupId, Material material,
                               BigDecimal fromQuantity, Map<String, BigDecimal> overrides) {
        String fromCode = fromNode.detail.getUnitCode();
        String toCode = toNode.detail.getUnitCode();

        UnitConversionFormula direct = formulaEngine.findFormula(groupId, material,
                fromCode, toCode, fromNode.detail.getFormulaId());
        if (direct != null) {
            return formulaEngine.forwardEvaluate(direct, material, fromQuantity, overrides);
        }

        UnitConversionFormula reverse = formulaEngine.findFormula(groupId, material,
                toCode, fromCode, toNode.detail.getFormulaId());
        if (reverse != null) {
            return formulaEngine.reverseEvaluate(reverse, material, fromQuantity, overrides);
        }
        return null;
    }

    private UnitGroup findGroup(Material material) {
        if (material.getUnitGroupCode() == null || material.getUnitGroupCode().isBlank()) {
            throw new IllegalArgumentException("物料未配置有效的计量单位组: " + material.getMaterialCode());
        }
        UnitGroup group = unitGroupMapper.selectUnitGroupByCode(material.getUnitGroupCode());
        if (group == null) {
            throw new IllegalArgumentException("物料未配置有效的计量单位组: " + material.getMaterialCode());
        }
        return group;
    }

    static class Node {
        private final UnitGroupDetail detail;

        Node(UnitGroupDetail detail) {
            this.detail = detail;
        }
    }
}
