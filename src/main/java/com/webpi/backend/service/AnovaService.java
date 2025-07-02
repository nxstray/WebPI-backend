package com.webpi.backend.service;

import com.webpi.backend.dto.AnovaDTO;
import com.webpi.backend.entity.TabelAnova;
import com.webpi.backend.entity.TabelGrup;
import com.webpi.backend.repository.AnovaRepository;
import com.webpi.backend.repository.TabelGrupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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

        List<String> namaGrup = dto.getNamaGrup();
        List<List<Double>> nilaiGrup = dto.getNilaiGrup();

        for (int i = 0; i < namaGrup.size(); i++) {
            String grupName = namaGrup.get(i);
            List<Double> nilaiList = nilaiGrup.get(i);

            for (Double nilai : nilaiList) {
                TabelGrup grup = new TabelGrup();
                grup.setGrup(grupName);
                grup.setNilai(nilai);
                grup.setAnova(savedAnova);
                tabelGrupRepository.save(grup);
            }
        }

        return savedAnova;
    }
}