package com.webpi.backend.repository;

import com.webpi.backend.entity.TabelGrup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TabelGrupRepository extends JpaRepository<TabelGrup, Integer> {
}