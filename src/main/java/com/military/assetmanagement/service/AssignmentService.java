package com.military.assetmanagement.service;

import com.military.assetmanagement.audit.AuditService;
import com.military.assetmanagement.dto.assignment.AssignmentRequest;
import com.military.assetmanagement.dto.assignment.AssignmentResponse;
import com.military.assetmanagement.entity.*;
import com.military.assetmanagement.exception.InsufficientInventoryException;
import com.military.assetmanagement.exception.ResourceNotFoundException;
import com.military.assetmanagement.repository.*;
import com.military.assetmanagement.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssetInventoryRepository assetInventoryRepository;
    private final BaseRepository baseRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final SecurityUtils securityUtils;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAll(Long baseId) {
        if (baseId == null && securityUtils.isAdmin()) {
            log.debug("ADMIN fetching all assignments");
            return assignmentRepository.findAllByOrderByAssignmentDateDesc()
                    .stream()
                    .map(AssignmentResponse::from)
                    .toList();
        }

        Long resolvedBaseId = resolveBaseId(baseId);
        securityUtils.enforceBaseAccess(resolvedBaseId);
        log.debug("Fetching assignments for baseId={}", resolvedBaseId);
        return assignmentRepository.findByBaseIdOrderByAssignmentDateDesc(resolvedBaseId)
                .stream()
                .map(AssignmentResponse::from)
                .toList();
    }

    public AssignmentResponse getById(Long id) {
        Assignment assignment = loadAssignment(id);
        if (!securityUtils.isAdmin()) {
            securityUtils.enforceBaseAccess(assignment.getBase().getId());
        }
        return AssignmentResponse.from(assignment);
    }

    @Transactional
    public AssignmentResponse create(AssignmentRequest request) {
        log.info("Creating assignment for baseId={}, equipmentTypeId={}, qty={}",
                request.baseId(), request.equipmentTypeId(), request.quantity());

        if (!securityUtils.isAdmin()) {
            securityUtils.enforceBaseAccess(request.baseId());
        }

        Base base = baseRepository.findById(request.baseId())
                .orElseThrow(() -> new ResourceNotFoundException("Base", request.baseId()));

        EquipmentType equipmentType = equipmentTypeRepository.findById(request.equipmentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("EquipmentType", request.equipmentTypeId()));

        AssetInventory inventory = assetInventoryRepository
                .findByBaseIdAndEquipmentTypeId(request.baseId(), request.equipmentTypeId())
                .orElseThrow(() -> new InsufficientInventoryException(
                        "No inventory found for equipment: " + equipmentType.getName()));

        if (inventory.getCurrentQuantity() < request.quantity()) {
            throw new InsufficientInventoryException(
                    "Insufficient inventory for assignment. Available: "
                            + inventory.getCurrentQuantity()
                            + ", Requested: " + request.quantity());
        }

        inventory.setCurrentQuantity(inventory.getCurrentQuantity() - request.quantity());
        inventory.setAssignedQuantity(inventory.getAssignedQuantity() + request.quantity());
        assetInventoryRepository.save(inventory);

        User currentUser = securityUtils.getCurrentUser();

        Assignment assignment = Assignment.builder()
                .base(base)
                .equipmentType(equipmentType)
                .quantity(request.quantity())
                .assignedTo(request.assignedTo())
                .purpose(request.purpose())
                .assignmentDate(request.assignmentDate())
                .returnDate(request.returnDate())
                .notes(request.notes())
                .createdBy(currentUser)
                .build();

        assignment = assignmentRepository.save(assignment);

        auditService.log(
                "CREATE",
                "Assignment",
                assignment.getId(),
                base,
                currentUser.getId(),
                currentUser.getEmail(),
                "Assigned " + request.quantity() + " x " + equipmentType.getName() + " to " + request.assignedTo()
        );

        log.info("Assignment created successfully with id={}", assignment.getId());
        return AssignmentResponse.from(assignment);
    }

    private Assignment loadAssignment(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", id));
    }

    private Long resolveBaseId(Long baseId) {
        if (baseId != null) {
            return baseId;
        }
        User user = securityUtils.getCurrentUser();
        if (user.getBase() == null) {
            throw new ResourceNotFoundException("No base associated with current user");
        }
        return user.getBase().getId();
    }
}
