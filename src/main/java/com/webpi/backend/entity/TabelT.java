package com.webpi.backend.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "t_tabel")
@Data
public class TabelT {
    @Id
    private Integer df;

    @Column(precision = 10, scale = 3)
    private BigDecimal nilai_0_5;
    private BigDecimal nilai_0_25;
    private BigDecimal nilai_0_1;
    private BigDecimal nilai_0_05;
    private BigDecimal nilai_0_025;
    private BigDecimal nilai_0_01;
    private BigDecimal nilai_0_005;
}
