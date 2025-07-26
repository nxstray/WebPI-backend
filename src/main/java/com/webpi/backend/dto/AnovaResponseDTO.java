package com.webpi.backend.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO untuk response hasil input atau perhitungan ANOVA.
 * Objek ini dikirim dari backend ke frontend sebagai hasil penyimpanan atau proses analisis.
 */
@Data
public class AnovaResponseDTO {

    /**
     * ID unik untuk data ANOVA.
     */
    private Long idAnova;

    /**
     * Nama kasus analisis ANOVA yang telah disimpan atau dihitung.
     */
    private String namaKasus;

    /**
     * Nama variabel dependen (Y) yang digunakan dalam analisis.
     */
    private String namaVariableDependen;

    /**
     * Nama variabel independen (X) yang digunakan dalam analisis.
     */
    private String namaVariableIndependen;

    /**
     * Nilai alpha (taraf signifikansi) yang digunakan dalam uji ANOVA.
     */
    private Double alpha;

    /**
     * Metode input data yang digunakan (manual / file).
     */
    private String inputMethod;

    /**
     * Jumlah total data/observasi dalam analisis ANOVA.
     */
    private Integer n;

    /**
     * Jumlah kelompok/grup dalam analisis ANOVA.
     */
    private Integer k;

    /**
     * Daftar grup yang berisi nama grup dan nilai rata-rata atau nilai analisis lainnya.
     */
    private List<GrupDTO> grups;

    /**
     * Inner static class GrupDTO merepresentasikan setiap grup dan nilai yang terkait.
     */
    @Data
    public static class GrupDTO {

        /**
         * Nama grup.
         */
        private String grup;

        /**
         * Nilai numerik yang terkait dengan grup (misalnya: rata-rata, total, dsb).
         */
        private Double nilai;
    }
}