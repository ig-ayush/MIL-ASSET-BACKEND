package com.military.assetmanagement.repository;

import com.military.assetmanagement.entity.EquipmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EquipmentTypeRepository extends JpaRepository<EquipmentType, Long> {
    List<EquipmentType> findByActiveTrue();
    boolean existsByNameIgnoreCase(String name);
}
