package com.military.assetmanagement.repository;

import com.military.assetmanagement.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findByBaseIdOrderByPurchaseDateDesc(Long baseId);

    List<Purchase> findAllByOrderByPurchaseDateDesc();

    @Query("SELECT COALESCE(SUM(p.quantity), 0) FROM Purchase p WHERE p.base.id = :baseId")
    int sumQuantityByBaseId(@Param("baseId") Long baseId);

    @Query("SELECT COALESCE(SUM(p.quantity), 0) FROM Purchase p WHERE p.base.id = :baseId AND p.equipmentType.id = :equipmentTypeId")
    int sumQuantityByBaseIdAndEquipmentTypeId(@Param("baseId") Long baseId,
                                              @Param("equipmentTypeId") Long equipmentTypeId);

    @Query("SELECT COALESCE(SUM(p.quantity), 0) FROM Purchase p WHERE p.base.id = :baseId AND p.purchaseDate BETWEEN :from AND :to")
    int sumQuantityByBaseIdAndDateRange(@Param("baseId") Long baseId,
                                        @Param("from") LocalDate from,
                                        @Param("to") LocalDate to);
}
