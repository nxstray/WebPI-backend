package com.webpi.backend.repository;

import com.webpi.backend.entity.TabelAnova;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface untuk entitas TabelAnova.
 * Menyediakan operasi CRUD untuk tabel t_anova.
 */
@Repository
public interface AnovaRepository extends JpaRepository<TabelAnova, Long> {
}