package com.webpi.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * Data Transfer Object (DTO) untuk respon hasil analisis korelasi.
 * Digunakan untuk mengirimkan data kembali ke frontend setelah proses analisis selesai.
 */
@Data
public class KorelasiResponseDTO {

    /**
     * ID unik dari hasil analisis korelasi.
     */
    private Long idKorelasi;

    /**
     * Nama kasus dari analisis korelasi.
     */
    private String namaKasus;

    /**
     * Nama variabel independen (X).
     */
    private String namaVarX;

    /**
     * Nama variabel dependen (Y).
     */
    private String namaVarY;

    /**
     * Metode input yang digunakan untuk memasukkan data (misalnya manual atau file).
     */
    private String inputMethod;

    /**
     * Nilai alpha (taraf signifikansi) yang digunakan dalam analisis.
     */
    private Double alpha;

    /**
     * Jumlah pasangan data (n).
     */
    private Integer n;

    /**
     * Daftar nilai-nilai variabel X.
     * Dipetakan secara eksplisit dari/ke JSON menggunakan anotasi @JsonProperty.
     */
    @JsonProperty("xValues")
    private List<Double> xValues;

    /**
     * Daftar nilai-nilai variabel Y.
     * Dipetakan secara eksplisit dari/ke JSON menggunakan anotasi @JsonProperty.
     */
    @JsonProperty("yValues")
    private List<Double> yValues;
}