package com.military.assetmanagement.service;

import com.military.assetmanagement.dto.inventory.InventoryResponse;
import com.military.assetmanagement.exception.ResourceNotFoundException;
import com.military.assetmanagement.repository.AssetInventoryRepository;
import com.military.assetmanagement.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final AssetInventoryRepository assetInventoryRepository;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public List<InventoryResponse> getInventoryForBase(Long baseId) {
        if (baseId == null && securityUtils.isAdmin()) {
            log.debug("ADMIN fetching inventory across all bases");
            return assetInventoryRepository.findAll()
                    .stream()
                    .map(InventoryResponse::from)
                    .toList();
        }

        Long resolvedBaseId = securityUtils.resolveBaseId(baseId);
        if (resolvedBaseId == null) {
            throw new ResourceNotFoundException("No base identified for current user or request");
        }

        log.debug("Fetching inventory for baseId={}", resolvedBaseId);
        return assetInventoryRepository.findByBaseId(resolvedBaseId)
                .stream()
                .map(InventoryResponse::from)
                .toList();
    }
}
