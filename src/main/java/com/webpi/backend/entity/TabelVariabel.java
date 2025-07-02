package com.webpi.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "t_variabel")
@Data
public class TabelVariabel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idVar;

    private Double x;
    private Double y;

    @ManyToOne
    @JoinColumn(name = "id_korelasi")
    private TabelKorelasi korelasi;

}