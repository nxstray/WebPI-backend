package com.webpi.backend.service;

import com.webpi.backend.dto.AnovaDTO;
import com.webpi.backend.dto.AnovaResponseDTO;
import com.webpi.backend.dto.AnovaResponseDTO.GrupDTO;
import com.webpi.backend.entity.TabelAnova;
import com.webpi.backend.entity.TabelGrup;
import com.webpi.backend.repository.AnovaRepository;
import com.webpi.backend.repository.TabelGrupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnovaService {

    private final AnovaRepository anovaRepository;
    private final TabelGrupRepository tabelGrupRepository;

    public TabelAnova simpanAnova(AnovaDTO dto) {
        TabelAnova anova = new TabelAnova();
        anova.setNamaKasus(dto.getNamaKasus());
        anova.setNamaVarY(dto.getNamaVariabelDependen());
        anova.setHo(dto.getHo());
        anova.setHa(dto.getHa());
        anova.setAlpha(dto.getAlpha());
        anova.setInputMethod(dto.getInputMethod());

        TabelAnova savedAnova = anovaRepository.save(anova);

        for (int i = 0; i < dto.getNamaGrup().size(); i++) {
            String grup = dto.getNamaGrup().get(i);
            List<Double> nilaiList = dto.getNilaiGrup().get(i);
            for (Double nilai : nilaiList) {
                TabelGrup ent = new TabelGrup();
                ent.setGrup(grup);
                ent.setNilai(nilai);
                ent.setAnova(savedAnova);
                tabelGrupRepository.save(ent);
            }
        }

        return savedAnova;
    }

    public AnovaResponseDTO getAnovaById(Long id) {
        TabelAnova anova = anovaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Data ANOVA dengan ID " + id + " tidak ditemukan."));

        List<GrupDTO> grupDTOList = anova.getGrups().stream().map(g -> {
            GrupDTO dto = new GrupDTO();
            dto.setGrup(g.getGrup());
            dto.setNilai(g.getNilai());
            return dto;
        }).collect(Collectors.toList());

        AnovaResponseDTO response = new AnovaResponseDTO();
        response.setIdAnova(anova.getIdAnova());
        response.setNamaKasus(anova.getNamaKasus());
        response.setNamaVarY(anova.getNamaVarY());
        response.setHo(anova.getHo());
        response.setHa(anova.getHa());
        response.setAlpha(anova.getAlpha());
        response.setInputMethod(anova.getInputMethod());
        response.setGrups(grupDTOList);

        return response;
    }
}