package com.webpi.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entitas JPA yang merepresentasikan tabel 't_variabel' dalam basis data.
 * Menyimpan data pasangan nilai X dan Y yang digunakan dalam analisis korelasi.
 */
@Entity
@Table(name = "t_variabel")
@Data
public class TabelVariabel {

    /**
     * Primary key dari tabel 't_variabel' dengan auto-increment.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_var")
    private Integer idVar;

    /**
     * Nilai dari variabel independen (X).
     */
    private Double x;

    /**
     * Nilai dari variabel dependen (Y).
     */
    private Double y;

    /**
     * Relasi ManyToOne ke entitas TabelKorelasi.
     * - fetch = FetchType.LAZY: data korelasi tidak langsung dimuat saat objek ini dipanggil.
     * - @JoinColumn(name = "id_korelasi"): foreign key yang mengacu ke tabel 't_korelasi'.
     * - nullable = false: setiap entri variabel harus terhubung ke sebuah kasus korelasi.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_korelasi", nullable = false)
    private TabelKorelasi korelasi;
}