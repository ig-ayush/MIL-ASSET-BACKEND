package com.military.assetmanagement.repository;

import com.military.assetmanagement.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByBaseIdOrderByAssignmentDateDesc(Long baseId);

    List<Assignment> findAllByOrderByAssignmentDateDesc();

    @Query("SELECT COALESCE(SUM(a.quantity), 0) FROM Assignment a WHERE a.base.id = :baseId")
    int sumQuantityByBaseId(@Param("baseId") Long baseId);
}
