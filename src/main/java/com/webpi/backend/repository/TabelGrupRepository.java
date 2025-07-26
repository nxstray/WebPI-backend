package com.webpi.backend.repository;

import com.webpi.backend.entity.TabelGrup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface untuk entitas TabelGrup.
 * Menyediakan operasi CRUD untuk tabel t_grup.
 */
@Repository
public interface TabelGrupRepository extends JpaRepository<TabelGrup, Integer> {
}