package com.webpi.backend.repository;

import com.webpi.backend.entity.TabelKorelasi;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface untuk entitas TabelKorelasi.
 * Menyediakan operasi CRUD untuk tabel t_korelasi.
 */
public interface KorelasiRepository extends JpaRepository<TabelKorelasi, Long> {
}