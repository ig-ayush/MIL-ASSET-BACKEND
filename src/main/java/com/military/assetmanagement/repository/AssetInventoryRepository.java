package com.military.assetmanagement.repository;

import com.military.assetmanagement.entity.AssetInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssetInventoryRepository extends JpaRepository<AssetInventory, Long> {

    List<AssetInventory> findByBaseId(Long baseId);

    Optional<AssetInventory> findByBaseIdAndEquipmentTypeId(Long baseId, Long equipmentTypeId);

    @Query("SELECT SUM(ai.currentQuantity) FROM AssetInventory ai WHERE ai.base.id = :baseId")
    Integer sumCurrentQuantityByBaseId(@Param("baseId") Long baseId);

    @Query("SELECT SUM(ai.openingBalance) FROM AssetInventory ai WHERE ai.base.id = :baseId")
    Integer sumOpeningBalanceByBaseId(@Param("baseId") Long baseId);
}
