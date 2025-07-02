package com.webpi.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class KorelasiResponseDTO {
    private Long idKorelasi;
    private String namaKasus;
    private String namaVarX;
    private String namaVarY;
    private String ho;
    private String ha;
    private Double alpha;
    private Integer n;
    private List<Double> xValues;
    private List<Double> yValues;
}