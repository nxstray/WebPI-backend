package com.webpi.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class AnovaDTO {
    private String namaKasus;
    private String namaVariabelDependen;
    private Double alpha;
    private String inputMethod;
    private List<String> namaGrup;
    private List<List<Double>> nilaiGrup;
}