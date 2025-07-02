package com.webpi.backend.repository;

import com.webpi.backend.entity.TabelAnova;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnovaRepository extends JpaRepository<TabelAnova, Long> {
}
