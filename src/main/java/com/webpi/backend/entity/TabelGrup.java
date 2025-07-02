package com.webpi.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "t_grup")
@Data
public class TabelGrup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idGrup;

    private String grup;
    private Double nilai;

    @ManyToOne
    @JoinColumn(name = "id_anova")
    private TabelAnova anova;
}
