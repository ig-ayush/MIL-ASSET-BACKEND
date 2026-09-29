package com.military.assetmanagement.service;

import com.military.assetmanagement.audit.AuditService;
import com.military.assetmanagement.dto.transfer.TransferRequest;
import com.military.assetmanagement.dto.transfer.TransferResponse;
import com.military.assetmanagement.entity.*;
import com.military.assetmanagement.exception.BadRequestException;
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
public class TransferService {

    private final TransferRepository transferRepository;
    private final AssetInventoryRepository assetInventoryRepository;
    private final BaseRepository baseRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final SecurityUtils securityUtils;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<TransferResponse> getAll(Long baseId) {
        if (baseId == null && securityUtils.isAdmin()) {
            log.debug("ADMIN fetching all transfers");
            return transferRepository.findAllByOrderByTransferDateDesc()
                    .stream()
                    .map(TransferResponse::from)
                    .toList();
        }

        Long resolvedBaseId = resolveBaseId(baseId);
        securityUtils.enforceBaseAccess(resolvedBaseId);
        log.debug("Fetching transfers for baseId={}", resolvedBaseId);
        return transferRepository.findBySourceBaseIdOrDestinationBaseIdOrderByTransferDateDesc(resolvedBaseId, resolvedBaseId)
                .stream()
                .map(TransferResponse::from)
                .toList();
    }

    public TransferResponse getById(Long id) {
        Transfer transfer = loadTransfer(id);
        if (!securityUtils.isAdmin()) {
            User currentUser = securityUtils.getCurrentUser();
            Long userBaseId = currentUser.getBase() != null ? currentUser.getBase().getId() : null;
            boolean isPartyToBase = (userBaseId != null) &&
                    (userBaseId.equals(transfer.getSourceBase().getId()) ||
                     userBaseId.equals(transfer.getDestinationBase().getId()));
            if (!isPartyToBase) {
                securityUtils.enforceBaseAccess(transfer.getSourceBase().getId());
            }
        }
        return TransferResponse.from(transfer);
    }

    @Transactional
    public TransferResponse create(TransferRequest request) {
        log.info("Creating transfer: src={}, dst={}, equip={}, qty={}",
                request.sourceBaseId(), request.destinationBaseId(), request.equipmentTypeId(), request.quantity());

        if (request.sourceBaseId().equals(request.destinationBaseId())) {
            throw new BadRequestException("Source and destination bases cannot be the same");
        }

        if (request.quantity() <= 0) {
            throw new BadRequestException("Transfer quantity must be greater than zero");
        }

        if (!securityUtils.isAdmin()) {
            securityUtils.enforceBaseAccess(request.sourceBaseId());
        }

        Base sourceBase = baseRepository.findById(request.sourceBaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Source base", request.sourceBaseId()));

        Base destinationBase = baseRepository.findById(request.destinationBaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination base", request.destinationBaseId()));

        EquipmentType equipmentType = equipmentTypeRepository.findById(request.equipmentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("EquipmentType", request.equipmentTypeId()));

        AssetInventory sourceInventory = assetInventoryRepository
                .findByBaseIdAndEquipmentTypeId(request.sourceBaseId(), request.equipmentTypeId())
                .orElseThrow(() -> new InsufficientInventoryException(
                        "No inventory found at source base for equipment: " + equipmentType.getName()));

        if (sourceInventory.getCurrentQuantity() < request.quantity()) {
            throw new InsufficientInventoryException(
                    "Insufficient inventory at source base. Available: "
                            + sourceInventory.getCurrentQuantity()
                            + ", Requested: " + request.quantity());
        }

        sourceInventory.setCurrentQuantity(sourceInventory.getCurrentQuantity() - request.quantity());
        assetInventoryRepository.save(sourceInventory);

        AssetInventory destInventory = assetInventoryRepository
                .findByBaseIdAndEquipmentTypeId(request.destinationBaseId(), request.equipmentTypeId())
                .orElseGet(() -> AssetInventory.builder()
                        .base(destinationBase)
                        .equipmentType(equipmentType)
                        .openingBalance(0)
                        .currentQuantity(0)
                        .assignedQuantity(0)
                        .expendedQuantity(0)
                        .build());

        destInventory.setCurrentQuantity(destInventory.getCurrentQuantity() + request.quantity());
        assetInventoryRepository.save(destInventory);

        User currentUser = securityUtils.getCurrentUser();

        Transfer transfer = Transfer.builder()
                .sourceBase(sourceBase)
                .destinationBase(destinationBase)
                .equipmentType(equipmentType)
                .quantity(request.quantity())
                .transferDate(request.transferDate())
                .reason(request.reason())
                .status(TransferStatus.COMPLETED)
                .notes(request.notes())
                .createdBy(currentUser)
                .build();

        transfer = transferRepository.save(transfer);

        auditService.log(
                "CREATE",
                "Transfer",
                transfer.getId(),
                sourceBase,
                currentUser.getId(),
                currentUser.getEmail(),
                "Transferred " + request.quantity() + " x " + equipmentType.getName()
                        + " from " + sourceBase.getName() + " to " + destinationBase.getName()
        );

        log.info("Transfer completed successfully with id={}", transfer.getId());
        return TransferResponse.from(transfer);
    }

    private Transfer loadTransfer(Long id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer", id));
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
