package com.military.assetmanagement.service;

import com.military.assetmanagement.audit.AuditService;
import com.military.assetmanagement.dto.equipment.EquipmentTypeRequest;
import com.military.assetmanagement.dto.equipment.EquipmentTypeResponse;
import com.military.assetmanagement.entity.EquipmentType;
import com.military.assetmanagement.entity.User;
import com.military.assetmanagement.exception.BadRequestException;
import com.military.assetmanagement.exception.ResourceNotFoundException;
import com.military.assetmanagement.repository.EquipmentTypeRepository;
import com.military.assetmanagement.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EquipmentTypeService {

    private final EquipmentTypeRepository equipmentTypeRepository;
    private final SecurityUtils securityUtils;
    private final AuditService auditService;

    public List<EquipmentTypeResponse> getAllEquipmentTypes() {
        return equipmentTypeRepository.findByActiveTrue()
                .stream()
                .map(EquipmentTypeResponse::from)
                .toList();
    }

    public EquipmentTypeResponse getById(Long id) {
        return equipmentTypeRepository.findById(id)
                .map(EquipmentTypeResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("EquipmentType", id));
    }

    @Transactional
    public EquipmentTypeResponse create(EquipmentTypeRequest request) {
        if (equipmentTypeRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new BadRequestException("Equipment type with name '" + request.name() + "' already exists");
        }

        String unit = (request.unit() != null && !request.unit().isBlank()) ? request.unit().trim() : "unit";

        EquipmentType equipmentType = EquipmentType.builder()
                .name(request.name().trim())
                .description(request.description())
                .unit(unit)
                .active(true)
                .build();

        EquipmentType saved = equipmentTypeRepository.save(equipmentType);

        User currentUser = getCurrentUserSafely();
        auditService.log(
                "CREATE",
                "EquipmentType",
                saved.getId(),
                null,
                currentUser != null ? currentUser.getId() : null,
                currentUser != null ? currentUser.getEmail() : null,
                "Created equipment type: " + saved.getName()
        );

        return EquipmentTypeResponse.from(saved);
    }

    @Transactional
    public EquipmentTypeResponse update(Long id, EquipmentTypeRequest request) {
        EquipmentType equipmentType = equipmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EquipmentType", id));

        String newName = request.name().trim();
        if (!equipmentType.getName().equalsIgnoreCase(newName) && equipmentTypeRepository.existsByNameIgnoreCase(newName)) {
            throw new BadRequestException("Equipment type with name '" + newName + "' already exists");
        }

        equipmentType.setName(newName);
        equipmentType.setDescription(request.description());
        if (request.unit() != null && !request.unit().isBlank()) {
            equipmentType.setUnit(request.unit().trim());
        }

        EquipmentType updated = equipmentTypeRepository.save(equipmentType);

        User currentUser = getCurrentUserSafely();
        auditService.log(
                "UPDATE",
                "EquipmentType",
                updated.getId(),
                null,
                currentUser != null ? currentUser.getId() : null,
                currentUser != null ? currentUser.getEmail() : null,
                "Updated equipment type: " + updated.getName()
        );

        return EquipmentTypeResponse.from(updated);
    }

    @Transactional
    public void delete(Long id) {
        EquipmentType equipmentType = equipmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EquipmentType", id));

        equipmentType.setActive(false);
        equipmentTypeRepository.save(equipmentType);

        User currentUser = getCurrentUserSafely();
        auditService.log(
                "DELETE",
                "EquipmentType",
                equipmentType.getId(),
                null,
                currentUser != null ? currentUser.getId() : null,
                currentUser != null ? currentUser.getEmail() : null,
                "Deactivated equipment type: " + equipmentType.getName()
        );
    }

    private User getCurrentUserSafely() {
        try {
            return securityUtils.getCurrentUser();
        } catch (Exception e) {
            return null;
        }
    }
}
