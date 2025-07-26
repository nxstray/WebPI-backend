package com.webpi.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

/**
 * Entitas JPA yang merepresentasikan tabel 't_korelasi' dalam basis data.
 * Menyimpan informasi kasus korelasi seperti nama variabel X, Y, alpha, dan ukuran sampel (n).
 */
@Entity
@Table(name = "t_korelasi")
@Data
public class TabelKorelasi {

    /**
     * Primary key dari tabel 't_korelasi' dengan auto-increment.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_korelasi")
    private Long idKorelasi;

    /**
     * Nama kasus atau judul dari analisis korelasi.
     * Disimpan dalam tipe teks panjang (TEXT).
     */
    @Column(columnDefinition = "TEXT")
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
     * Nilai tingkat signifikansi (alpha) yang digunakan dalam analisis korelasi.
     */
    private Double alpha;

    /**
     * Jumlah data (n) yang digunakan dalam analisis korelasi.
     */
    private Integer n;

    /**
     * Relasi OneToMany dengan entitas TabelVariabel.
     * - mappedBy: nama field di TabelVariabel yang menjadi pemilik relasi.
     * - cascade = CascadeType.ALL: setiap perubahan di entitas ini akan memengaruhi entitas terkait.
     * - orphanRemoval = true: data variabel akan dihapus jika tidak lagi terkait dengan korelasi.
     */
    @OneToMany(mappedBy = "korelasi", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TabelVariabel> daftarVariabel;
}