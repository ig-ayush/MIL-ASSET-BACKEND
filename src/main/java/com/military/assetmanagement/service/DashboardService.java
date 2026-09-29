package com.military.assetmanagement.service;

import com.military.assetmanagement.dto.dashboard.DashboardResponse;
import com.military.assetmanagement.dto.dashboard.DashboardResponse.EquipmentSummary;
import com.military.assetmanagement.entity.AssetInventory;
import com.military.assetmanagement.entity.Base;
import com.military.assetmanagement.entity.EquipmentType;
import com.military.assetmanagement.entity.Purchase;
import com.military.assetmanagement.entity.Transfer;
import com.military.assetmanagement.entity.TransferStatus;
import com.military.assetmanagement.exception.ResourceNotFoundException;
import com.military.assetmanagement.repository.*;
import com.military.assetmanagement.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BaseRepository baseRepository;
    private final AssetInventoryRepository assetInventoryRepository;
    private final PurchaseRepository purchaseRepository;
    private final TransferRepository transferRepository;
    private final AssignmentRepository assignmentRepository;
    private final ExpenditureRepository expenditureRepository;
    private final SecurityUtils securityUtils;
    
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long baseId) {
        Long resolvedBaseId = securityUtils.resolveBaseId(baseId);

        if (resolvedBaseId != null) {
            return getDashboardForBase(resolvedBaseId);
        } else {
            return getDashboardForAllBases();
        }
    }

    private DashboardResponse getDashboardForBase(Long baseId) {
        Base base = baseRepository.findById(baseId)
                .orElseThrow(() -> new ResourceNotFoundException("Base", baseId));

        List<AssetInventory> inventories = assetInventoryRepository.findByBaseId(baseId);

        int openingBalance = inventories.stream().mapToInt(AssetInventory::getOpeningBalance).sum();
        int totalPurchases = purchaseRepository.sumQuantityByBaseId(baseId);
        int totalTransferIn = transferRepository.sumTransferInByBaseId(baseId);
        int totalTransferOut = transferRepository.sumTransferOutByBaseId(baseId);

        int netMovement = totalPurchases + totalTransferIn - totalTransferOut;
        int closingBalance = openingBalance + totalPurchases + totalTransferIn - totalTransferOut;

        int totalAssigned = inventories.stream().mapToInt(AssetInventory::getAssignedQuantity).sum();
        int totalExpended = inventories.stream().mapToInt(AssetInventory::getExpendedQuantity).sum();

        List<EquipmentSummary> equipmentSummaries = inventories.stream()
                .map(inv -> new EquipmentSummary(
                        inv.getEquipmentType().getId(),
                        inv.getEquipmentType().getName(),
                        inv.getEquipmentType().getUnit(),
                        inv.getOpeningBalance(),
                        inv.getCurrentQuantity(),
                        inv.getAssignedQuantity(),
                        inv.getExpendedQuantity()
                ))
                .toList();

        return new DashboardResponse(
                base.getId(),
                base.getName(),
                openingBalance,
                totalPurchases,
                totalTransferIn,
                totalTransferOut,
                closingBalance,
                totalAssigned,
                totalExpended,
                netMovement,
                equipmentSummaries
        );
    }

    private DashboardResponse getDashboardForAllBases() {
        List<AssetInventory> inventories = assetInventoryRepository.findAll();

        int openingBalance = inventories.stream().mapToInt(AssetInventory::getOpeningBalance).sum();
        int totalPurchases = purchaseRepository.findAll().stream().mapToInt(Purchase::getQuantity).sum();

        List<Transfer> completedTransfers = transferRepository.findAll().stream()
                .filter(t -> t.getStatus() == TransferStatus.COMPLETED)
                .toList();
        int totalTransferIn = completedTransfers.stream().mapToInt(Transfer::getQuantity).sum();
        int totalTransferOut = completedTransfers.stream().mapToInt(Transfer::getQuantity).sum();

        int netMovement = totalPurchases + totalTransferIn - totalTransferOut;
        int closingBalance = openingBalance + totalPurchases + totalTransferIn - totalTransferOut;

        int totalAssigned = inventories.stream().mapToInt(AssetInventory::getAssignedQuantity).sum();
        int totalExpended = inventories.stream().mapToInt(AssetInventory::getExpendedQuantity).sum();

        // Aggregate by equipment type across bases
        Map<Long, List<AssetInventory>> grouped = inventories.stream()
                .collect(Collectors.groupingBy(inv -> inv.getEquipmentType().getId()));

        List<EquipmentSummary> equipmentSummaries = new ArrayList<>();
        for (List<AssetInventory> list : grouped.values()) {
            if (list.isEmpty()) continue;
            EquipmentType et = list.get(0).getEquipmentType();
            int eqOpening = list.stream().mapToInt(AssetInventory::getOpeningBalance).sum();
            int eqCurrent = list.stream().mapToInt(AssetInventory::getCurrentQuantity).sum();
            int eqAssigned = list.stream().mapToInt(AssetInventory::getAssignedQuantity).sum();
            int eqExpended = list.stream().mapToInt(AssetInventory::getExpendedQuantity).sum();

            equipmentSummaries.add(new EquipmentSummary(
                    et.getId(),
                    et.getName(),
                    et.getUnit(),
                    eqOpening,
                    eqCurrent,
                    eqAssigned,
                    eqExpended
            ));
        }

        return new DashboardResponse(
                null,
                "All Bases (Consolidated)",
                openingBalance,
                totalPurchases,
                totalTransferIn,
                totalTransferOut,
                closingBalance,
                totalAssigned,
                totalExpended,
                netMovement,
                equipmentSummaries
        );
    }
}
