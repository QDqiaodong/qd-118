package com.weakcurrent.service.impl;

import com.weakcurrent.common.BusinessException;
import com.weakcurrent.common.ResultCode;
import com.weakcurrent.dto.ConstructionProjectCreateDTO;
import com.weakcurrent.dto.ConstructionProjectUpdateDTO;
import com.weakcurrent.dto.ProjectConsumptionDTO;
import com.weakcurrent.entity.ConstructionProject;
import com.weakcurrent.entity.StockOut;
import com.weakcurrent.repository.ConstructionProjectRepository;
import com.weakcurrent.repository.StockOutRepository;
import com.weakcurrent.service.ConstructionProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ConstructionProjectServiceImpl implements ConstructionProjectService {

    private final ConstructionProjectRepository constructionProjectRepository;
    private final StockOutRepository stockOutRepository;

    @Override
    @Transactional
    public ConstructionProject create(ConstructionProjectCreateDTO dto) {
        if (constructionProjectRepository.existsByProjectNo(dto.getProjectNo())) {
            throw new BusinessException(ResultCode.DATA_DUPLICATE, "项目编号已存在: " + dto.getProjectNo());
        }
        ConstructionProject project = new ConstructionProject();
        project.setProjectNo(dto.getProjectNo());
        project.setProjectName(dto.getProjectName());
        project.setFactoryArea(dto.getFactoryArea());
        project.setStatus(dto.getStatus() != null ? dto.getStatus() : true);
        project.setRemark(dto.getRemark());
        return constructionProjectRepository.save(project);
    }

    @Override
    @Transactional
    public ConstructionProject update(ConstructionProjectUpdateDTO dto) {
        ConstructionProject project = constructionProjectRepository.findById(dto.getId())
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));

        if (!project.getProjectNo().equals(dto.getProjectNo())
                && constructionProjectRepository.existsByProjectNo(dto.getProjectNo())) {
            throw new BusinessException(ResultCode.DATA_DUPLICATE, "项目编号已存在: " + dto.getProjectNo());
        }

        project.setProjectNo(dto.getProjectNo());
        project.setProjectName(dto.getProjectName());
        project.setFactoryArea(dto.getFactoryArea());
        if (dto.getStatus() != null) {
            project.setStatus(dto.getStatus());
        }
        project.setRemark(dto.getRemark());
        return constructionProjectRepository.save(project);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ConstructionProject project = constructionProjectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));
        List<StockOut> records = stockOutRepository.findByProjectId(id);
        if (!records.isEmpty()) {
            throw new BusinessException(ResultCode.DATA_IN_USE, "该项目存在领用记录，无法删除");
        }
        constructionProjectRepository.deleteById(id);
    }

    @Override
    public ConstructionProject getById(Long id) {
        return constructionProjectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND));
    }

    @Override
    public List<ConstructionProject> list() {
        return constructionProjectRepository.findAll();
    }

    @Override
    public List<ConstructionProject> listActive() {
        return constructionProjectRepository.findByStatus(true);
    }

    @Override
    public List<ProjectConsumptionDTO> getProjectConsumptionSummary(Long projectId) {
        ConstructionProject project = getById(projectId);
        List<StockOut> records = stockOutRepository.findByProjectId(projectId);
        return buildConsumptionSummary(project, records);
    }

    @Override
    public List<ProjectConsumptionDTO> getAllProjectConsumptionSummary() {
        List<ConstructionProject> projects = constructionProjectRepository.findAll();
        List<ProjectConsumptionDTO> result = new ArrayList<>();
        for (ConstructionProject project : projects) {
            List<StockOut> records = stockOutRepository.findByProjectId(project.getId());
            if (!records.isEmpty()) {
                result.addAll(buildConsumptionSummary(project, records));
            }
        }
        return result;
    }

    private List<ProjectConsumptionDTO> buildConsumptionSummary(ConstructionProject project, List<StockOut> records) {
        Map<String, ProjectConsumptionDTO> summaryMap = new LinkedHashMap<>();

        for (StockOut record : records) {
            String key = record.getAccessoryName() + "|" + record.getWorkshop();
            ProjectConsumptionDTO dto = summaryMap.get(key);
            if (dto == null) {
                dto = new ProjectConsumptionDTO();
                dto.setProjectId(project.getId());
                dto.setProjectNo(project.getProjectNo());
                dto.setProjectName(project.getProjectName());
                dto.setFactoryArea(project.getFactoryArea());
                dto.setAccessoryName(record.getAccessoryName());
                dto.setWorkshop(record.getWorkshop());
                dto.setPurpose(record.getPurpose());
                dto.setTotalQuantity(0L);
                dto.setRecordCount(0);
                summaryMap.put(key, dto);
            }
            dto.setTotalQuantity(dto.getTotalQuantity() + record.getQuantity());
            dto.setRecordCount(dto.getRecordCount() + 1);
        }

        return new ArrayList<>(summaryMap.values());
    }
}
