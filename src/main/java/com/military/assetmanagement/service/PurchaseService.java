package com.military.assetmanagement.service;

import com.military.assetmanagement.audit.AuditService;
import com.military.assetmanagement.dto.purchase.PurchaseRequest;
import com.military.assetmanagement.dto.purchase.PurchaseResponse;
import com.military.assetmanagement.entity.*;
import com.military.assetmanagement.exception.ResourceNotFoundException;
import com.military.assetmanagement.repository.*;
import com.military.assetmanagement.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final AssetInventoryRepository assetInventoryRepository;
    private final BaseRepository baseRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<PurchaseResponse> getAll(Long baseId) {
        if (baseId == null && securityUtils.isAdmin()) {
            log.debug("ADMIN fetching all purchases");
            return purchaseRepository.findAllByOrderByPurchaseDateDesc()
                    .stream()
                    .map(PurchaseResponse::from)
                    .toList();
        }

        Long resolvedBaseId = resolveBaseId(baseId);
        securityUtils.enforceBaseAccess(resolvedBaseId);
        log.debug("Fetching purchases for baseId={}", resolvedBaseId);
        return purchaseRepository.findByBaseIdOrderByPurchaseDateDesc(resolvedBaseId)
                .stream()
                .map(PurchaseResponse::from)
                .toList();
    }

    public PurchaseResponse getById(Long id) {
        Purchase purchase = loadPurchase(id);
        if (!securityUtils.isAdmin()) {
            securityUtils.enforceBaseAccess(purchase.getBase().getId());
        }
        return PurchaseResponse.from(purchase);
    }

    @Transactional
    public PurchaseResponse create(PurchaseRequest request) {
        log.info("Creating purchase for baseId={}, equipmentTypeId={}, quantity={}",
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
                .orElseGet(() -> AssetInventory.builder()
                        .base(base)
                        .equipmentType(equipmentType)
                        .openingBalance(0)
                        .currentQuantity(0)
                        .assignedQuantity(0)
                        .expendedQuantity(0)
                        .build());

        BigDecimal totalPrice = null;
        if (request.unitPrice() != null) {
            totalPrice = request.unitPrice().multiply(BigDecimal.valueOf(request.quantity()));
        }

        User currentUser = securityUtils.getCurrentUser();

        Purchase purchase = Purchase.builder()
                .base(base)
                .equipmentType(equipmentType)
                .quantity(request.quantity())
                .unitPrice(request.unitPrice())
                .totalPrice(totalPrice)
                .supplier(request.supplier())
                .purchaseDate(request.purchaseDate())
                .notes(request.notes())
                .createdBy(currentUser)
                .build();

        purchase = purchaseRepository.save(purchase);

        inventory.setCurrentQuantity(inventory.getCurrentQuantity() + request.quantity());
        assetInventoryRepository.save(inventory);

        auditService.log(
                "CREATE",
                "Purchase",
                purchase.getId(),
                base,
                currentUser.getId(),
                currentUser.getEmail(),
                "Purchase of " + request.quantity() + " x " + equipmentType.getName()
        );

        log.info("Purchase created successfully with id={}", purchase.getId());
        return PurchaseResponse.from(purchase);
    }

    private Purchase loadPurchase(Long id) {
        return purchaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase", id));
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
