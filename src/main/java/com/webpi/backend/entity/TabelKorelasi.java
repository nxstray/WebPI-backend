package com.webpi.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "t_korelasi")
@Data
public class TabelKorelasi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idKorelasi;

    @Column(columnDefinition = "TEXT")
    private String namaKasus;

    private String namaVarX;
    private String namaVarY;
    private String ho;
    private String ha;
    private Double alpha;
    private Integer n;
}