package com.webpi.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "t_anova")
@Data
public class TabelAnova {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAnova;

    @Column(columnDefinition = "TEXT")
    private String namaKasus;

    private String namaVarY;
    private Double alpha;
    private String inputMethod;

    @OneToMany(mappedBy = "anova", cascade = CascadeType.ALL)
    private List<TabelGrup> grups;
}