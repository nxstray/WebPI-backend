package com.webpi.backend.repository;

import com.webpi.backend.entity.TabelVariabel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TabelVariabelRepository extends JpaRepository<TabelVariabel, Integer> {

    List<TabelVariabel> findByKorelasiIdKorelasi(Long idKorelasi);
}