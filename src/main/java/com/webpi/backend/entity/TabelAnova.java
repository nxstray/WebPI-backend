package com.webpi.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

/**
 * Entitas JPA yang merepresentasikan tabel 't_anova' dalam basis data.
 * Digunakan untuk menyimpan informasi terkait kasus uji ANOVA,
 * termasuk nama kasus, variabel dependen, variabel independen, nilai alpha, metode input,
 * serta daftar grup data yang terkait.
 */
@Entity
@Table(name = "t_anova")
@Data
public class TabelAnova {

    /**
     * Primary key dari tabel 't_anova', dengan strategi auto-increment.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAnova;

    /**
     * Nama kasus untuk uji ANOVA.
     * Disimpan dalam tipe TEXT di basis data untuk mendukung teks panjang.
     */
    @Column(columnDefinition = "TEXT")
    private String namaKasus;

    /**
     * Nama variabel dependen yang diuji dalam ANOVA.
     * Disimpan dalam kolom dengan nama eksplisit 'nama_dependen'.
     */
    @Column(name = "nama_dependen")
    private String namaVariableDependen;

    /**
     * Nama variabel independen yang diuji dalam ANOVA.
     * Disimpan dalam kolom dengan nama eksplisit 'nama_independen'.
     */
    @Column(name = "nama_independen")
    private String namaVariableIndependen;

    /**
     * Nilai alpha (tingkat signifikansi) yang digunakan dalam uji ANOVA.
     */
    private Double alpha;

    /**
     * Metode input yang digunakan untuk memasukkan data (misalnya manual atau file).
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
     * Relasi OneToMany dengan entitas TabelGrup.
     * - mappedBy: menunjukkan properti 'anova' di entitas TabelGrup sebagai pemilik relasi.
     * - cascade: semua operasi (merge, remove, dll) akan diteruskan ke TabelGrup.
     * - fetch: menggunakan FetchType.EAGER untuk mengambil seluruh grup sekaligus saat TabelAnova diambil.
     */
    @OneToMany(mappedBy = "anova", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<TabelGrup> grups;
}