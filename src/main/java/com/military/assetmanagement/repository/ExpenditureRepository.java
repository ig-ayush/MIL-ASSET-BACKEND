package com.military.assetmanagement.repository;

import com.military.assetmanagement.entity.Expenditure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExpenditureRepository extends JpaRepository<Expenditure, Long> {

    List<Expenditure> findByBaseIdOrderByExpenditureDateDesc(Long baseId);

    List<Expenditure> findAllByOrderByExpenditureDateDesc();

    @Query("SELECT COALESCE(SUM(e.quantity), 0) FROM Expenditure e WHERE e.base.id = :baseId")
    int sumQuantityByBaseId(@Param("baseId") Long baseId);
}
