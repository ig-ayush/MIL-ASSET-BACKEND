package com.military.assetmanagement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "asset_inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_id", nullable = false)
    private Base base;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_type_id", nullable = false)
    private EquipmentType equipmentType;

    @Column(name = "opening_balance", nullable = false)
    @Builder.Default
    private int openingBalance = 0;

    @Column(name = "current_quantity", nullable = false)
    @Builder.Default
    private int currentQuantity = 0;

    @Column(name = "assigned_quantity", nullable = false)
    @Builder.Default
    private int assignedQuantity = 0;

    @Column(name = "expended_quantity", nullable = false)
    @Builder.Default
    private int expendedQuantity = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
