package com.red.franquicias.infrastructure.drivenadapter.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("products")
public class ProductEntity {
    @Id
    private Long id;
    @Column("branch_id")
    private Long branchId;
    @Column("name")
    private String name;
    @Column("stock")
    private Integer stock;
}

