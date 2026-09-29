package com.military.assetmanagement.repository;

import com.military.assetmanagement.entity.Base;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BaseRepository extends JpaRepository<Base, Long> {
    List<Base> findByActiveTrue();
    boolean existsByNameIgnoreCase(String name);
}
