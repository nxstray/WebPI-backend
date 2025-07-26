package com.webpi.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Entitas JPA yang merepresentasikan tabel 't_grup' dalam basis data.
 * Digunakan untuk menyimpan data masing-masing grup dalam analisis ANOVA,
 * termasuk nama grup, nilai, dan relasi ke entitas TabelAnova.
 */
@Entity
@Table(name = "t_grup")
@Data
public class TabelGrup {

    /**
     * Primary key dari tabel 't_grup' dengan strategi auto-increment.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idGrup;

    /**
     * Nama grup atau label kategori dari data.
     */
    private String grup;

    /**
     * Nilai yang termasuk dalam grup ini.
     */
    private Double nilai;

    /**
     * Relasi ManyToOne ke entitas TabelAnova.
     * - fetch = FetchType.LAZY: data TabelAnova tidak langsung dimuat saat entitas ini dipanggil.
     * - @JoinColumn(name = "id_anova"): foreign key yang mengacu ke kolom 'id_anova' di tabel 't_anova'.
     * - @JsonIgnore: mencegah siklus serialisasi JSON saat response dikembalikan ke frontend.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_anova")
    @JsonIgnore
    private TabelAnova anova;
}