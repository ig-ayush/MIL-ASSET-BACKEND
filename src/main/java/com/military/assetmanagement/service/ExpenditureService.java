package com.military.assetmanagement.service;

import com.military.assetmanagement.audit.AuditService;
import com.military.assetmanagement.dto.expenditure.ExpenditureRequest;
import com.military.assetmanagement.dto.expenditure.ExpenditureResponse;
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
public class ExpenditureService {

    private final ExpenditureRepository expenditureRepository;
    private final AssetInventoryRepository assetInventoryRepository;
    private final BaseRepository baseRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final SecurityUtils securityUtils;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<ExpenditureResponse> getAll(Long baseId) {
        if (baseId == null && securityUtils.isAdmin()) {
            log.debug("ADMIN fetching all expenditures");
            return expenditureRepository.findAllByOrderByExpenditureDateDesc()
                    .stream()
                    .map(ExpenditureResponse::from)
                    .toList();
        }

        Long resolvedBaseId = resolveBaseId(baseId);
        securityUtils.enforceBaseAccess(resolvedBaseId);
        log.debug("Fetching expenditures for baseId={}", resolvedBaseId);
        return expenditureRepository.findByBaseIdOrderByExpenditureDateDesc(resolvedBaseId)
                .stream()
                .map(ExpenditureResponse::from)
                .toList();
    }

    public ExpenditureResponse getById(Long id) {
        Expenditure expenditure = loadExpenditure(id);
        if (!securityUtils.isAdmin()) {
            securityUtils.enforceBaseAccess(expenditure.getBase().getId());
        }
        return ExpenditureResponse.from(expenditure);
    }

    @Transactional
    public ExpenditureResponse create(ExpenditureRequest request) {
        log.info("Creating expenditure for baseId={}, equipmentTypeId={}, qty={}",
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
                    "Insufficient inventory for expenditure. Available: "
                            + inventory.getCurrentQuantity()
                            + ", Requested: " + request.quantity());
        }

        inventory.setCurrentQuantity(inventory.getCurrentQuantity() - request.quantity());
        inventory.setExpendedQuantity(inventory.getExpendedQuantity() + request.quantity());
        assetInventoryRepository.save(inventory);

        User currentUser = securityUtils.getCurrentUser();

        Expenditure expenditure = Expenditure.builder()
                .base(base)
                .equipmentType(equipmentType)
                .quantity(request.quantity())
                .reason(request.reason())
                .expenditureDate(request.expenditureDate())
                .notes(request.notes())
                .createdBy(currentUser)
                .build();

        expenditure = expenditureRepository.save(expenditure);

        auditService.log(
                "CREATE",
                "Expenditure",
                expenditure.getId(),
                base,
                currentUser.getId(),
                currentUser.getEmail(),
                "Expended " + request.quantity() + " x " + equipmentType.getName() + ": " + request.reason()
        );

        log.info("Expenditure created successfully with id={}", expenditure.getId());
        return ExpenditureResponse.from(expenditure);
    }

    private Expenditure loadExpenditure(Long id) {
        return expenditureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expenditure", id));
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
