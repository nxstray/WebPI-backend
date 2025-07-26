package com.webpi.backend.repository;

import com.webpi.backend.entity.TabelVariabel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface untuk entitas TabelVariabel.
 * Menyediakan operasi CRUD untuk tabel t_variabel serta
 * query tambahan untuk mendapatkan data berdasarkan ID korelasi.
 */
public interface TabelVariabelRepository extends JpaRepository<TabelVariabel, Integer> {

    /**
     * Mengambil semua variabel (x, y) berdasarkan ID korelasi yang terkait.
     */
    List<TabelVariabel> findByKorelasiIdKorelasi(Long idKorelasi);
}