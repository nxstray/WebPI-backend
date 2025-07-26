package com.webpi.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object (DTO) untuk input data ANOVA dari frontend.
 * Digunakan untuk menampung semua informasi yang dibutuhkan 
 * sebelum dikirim ke proses perhitungan ANOVA.
 */
@Data
public class AnovaDTO {

    /**
     * Nama kasus yang sedang dianalisis.
     * Tidak boleh kosong.
     */
    @NotBlank(message = "Eits, pastikan semua field terisi dengan benar ya!")
    private String namaKasus;

    /**
     * Nama variabel dependen (Y) yang akan dianalisis dalam ANOVA.
     * Tidak boleh kosong.
     */
    @NotBlank(message = "Eits, pastikan semua field terisi dengan benar ya!")
    private String namaVariableDependen;

    /**
     * Nama variabel independen (X) yang akan dianalisis dalam ANOVA.
     * Tidak boleh kosong.
     */
    @NotBlank(message = "Eits, pastikan semua field terisi dengan benar ya!")
    private String namaVariableIndependen;

    /**
     * Nilai alpha (taraf signifikansi) untuk uji ANOVA.
     * Harus bernilai lebih dari 0 dan tidak boleh null.
     */
    @NotNull(message = "Eits, pastikan semua field terisi dengan benar ya!")
    @DecimalMin(value = "0.000001", message = "Alpha harus lebih besar dari 0")
    private Double alpha;

    /**
     * Metode input yang digunakan: bisa "manual" atau "file".
     * Tidak boleh kosong.
     */
    @NotBlank(message = "Eits, pilih metode input data dulu ya!")
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
     * List nama grup yang dianalisis (misalnya: Grup A, Grup B, dst).
     * Harus memiliki minimal satu grup dan setiap nama tidak boleh kosong.
     */
    @NotNull(message = "Nama grup tidak boleh kosong")
    @Size(min = 1, message = "Harus ada minimal satu grup")
    private List<
        @NotBlank(message = "Nama grup tidak boleh kosong") String
    > namaGrup;

    /**
     * List nilai untuk masing-masing grup.
     * Setiap grup harus memiliki minimal satu nilai dan tidak boleh null.
     * Digunakan untuk menghitung variasi antar dan dalam grup.
     * Memperbolehkan nilai negatif, desimal, dan duplikasi dengan syarat tertentu.
     */
    @NotNull(message = "Nilai grup tidak boleh kosong")
    @Size(min = 1, message = "Harus ada minimal satu nilai grup")
    private List<
        @NotNull(message = "Grup tidak boleh null")
        @Size(min = 1, message = "Grup harus memiliki setidaknya satu nilai")
        List<@NotNull(message = "Nilai tidak boleh null") Double>
    > nilaiGrup;
}