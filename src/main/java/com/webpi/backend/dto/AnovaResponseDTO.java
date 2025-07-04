package com.webpi.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class AnovaResponseDTO {
    private Long idAnova;
    private String namaKasus;
    private String namaVarY;
    private Double alpha;
    private String inputMethod;
    private List<GrupDTO> grups;

    @Data
    public static class GrupDTO {
        private String grup;
        private Double nilai;
    }
}