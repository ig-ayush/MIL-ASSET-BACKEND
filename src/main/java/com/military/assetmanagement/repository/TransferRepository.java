package com.military.assetmanagement.repository;

import com.military.assetmanagement.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, Long> {

    List<Transfer> findBySourceBaseIdOrDestinationBaseIdOrderByTransferDateDesc(Long sourceBaseId,
                                                                                 Long destinationBaseId);

    List<Transfer> findAllByOrderByTransferDateDesc();

    @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM Transfer t WHERE t.destinationBase.id = :baseId AND t.status = com.military.assetmanagement.entity.TransferStatus.COMPLETED")
    int sumTransferInByBaseId(@Param("baseId") Long baseId);

    @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM Transfer t WHERE t.sourceBase.id = :baseId AND t.status = com.military.assetmanagement.entity.TransferStatus.COMPLETED")
    int sumTransferOutByBaseId(@Param("baseId") Long baseId);
}
