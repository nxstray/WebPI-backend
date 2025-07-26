package com.webpi.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.webpi.backend.validator.SameSizeList;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object (DTO) untuk input data korelasi bivariate.
 * Digunakan untuk menerima data dari frontend dan divalidasi sebelum diproses.
 */
@SameSizeList // Validasi custom untuk memastikan ukuran xValues dan yValues sama
@Data
public class KorelasiDTO {

    /**
     * Nama kasus untuk analisis korelasi.
     * Tidak boleh kosong.
     */
    @NotBlank(message = "Nama kasus wajib diisi")
    @JsonProperty("namaKasus") // Memetakan nama properti JSON ke field Java
    private String namaKasus;

    /**
     * Nama variabel X (independen).
     * Tidak boleh kosong.
     */
    @NotBlank
    @JsonProperty("namaVarX")
    private String namaVarX;

    /**
     * Nama variabel Y (dependen).
     * Tidak boleh kosong.
     */
    @NotBlank
    @JsonProperty("namaVarY")
    private String namaVarY;

    /**
     * Nilai alpha (taraf signifikansi).
     * Tidak boleh null.
     */
    @NotNull
    @JsonProperty("alpha")
    private Double alpha;

    /**
     * Metode input data (misalnya manual / excel).
     * Tidak boleh kosong.
     */
    @NotBlank
    @JsonProperty("inputMethod")
    private String inputMethod;

    /**
     * Daftar nilai untuk variabel X.
     * Dapat kosong saat awal, tetapi akan divalidasi oleh @SameSizeList.
     */
    @JsonProperty("xValues")
    private List<Double> xValues;

    /**
     * Daftar nilai untuk variabel Y.
     * Dapat kosong saat awal, tetapi akan divalidasi oleh @SameSizeList.
     */
    @JsonProperty("yValues")
    private List<Double> yValues;

    /**
     * Konstruktor default yang diperlukan untuk deserialisasi JSON oleh Jackson.
     */
    public KorelasiDTO() {}

    /**
     * Konstruktor lengkap, berguna untuk keperluan debugging atau testing.
     */
    public KorelasiDTO(String namaKasus, String namaVarX, String namaVarY,
                       Double alpha, String inputMethod,
                       List<Double> xValues, List<Double> yValues) {
        this.namaKasus = namaKasus;
        this.namaVarX = namaVarX;
        this.namaVarY = namaVarY;
        this.alpha = alpha;
        this.inputMethod = inputMethod;
        this.xValues = xValues;
        this.yValues = yValues;
    }
}