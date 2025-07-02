package com.webpi.backend.repository;

import com.webpi.backend.entity.TabelKorelasi;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KorelasiRepository extends JpaRepository<TabelKorelasi, Long> {
}