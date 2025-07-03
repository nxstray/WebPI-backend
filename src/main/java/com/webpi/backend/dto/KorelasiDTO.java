package com.webpi.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class KorelasiDTO {
    private String namaKasus;
    private String namaVarX;
    private String namaVarY;
    private Double alpha;
    private String inputMethod;

    @JsonProperty("xValues")
    private List<Double> xValues;

    @JsonProperty("yValues")
    private List<Double> yValues;
}