package com.webpi.backend.service;

import com.webpi.backend.dto.KorelasiDTO;
import com.webpi.backend.dto.KorelasiResponseDTO;
import com.webpi.backend.entity.TabelKorelasi;
import com.webpi.backend.entity.TabelVariabel;
import com.webpi.backend.repository.KorelasiRepository;
import com.webpi.backend.repository.TabelVariabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KorelasiService {

    private final KorelasiRepository korelasiRepository;
    private final TabelVariabelRepository variabelRepository;

    public TabelKorelasi simpanKorelasi(KorelasiDTO dto) {
        List<Double> xValues = dto.getXValues();
        List<Double> yValues = dto.getYValues();

        if (xValues == null || yValues == null) {
            throw new IllegalArgumentException("List xValues dan yValues tidak boleh null.");
        }

        if (xValues.size() != yValues.size()) {
            throw new IllegalArgumentException("Jumlah elemen pada xValues dan yValues harus sama.");
        }

        TabelKorelasi korelasi = new TabelKorelasi();
        korelasi.setNamaKasus(dto.getNamaKasus());
        korelasi.setNamaVarX(dto.getNamaVarX());
        korelasi.setNamaVarY(dto.getNamaVarY());
        korelasi.setHo(dto.getHo());
        korelasi.setHa(dto.getHa());
        korelasi.setAlpha(dto.getAlpha());
        korelasi.setN(xValues.size());

        TabelKorelasi savedKorelasi = korelasiRepository.save(korelasi);

        for (int i = 0; i < xValues.size(); i++) {
            TabelVariabel variabel = new TabelVariabel();
            variabel.setX(xValues.get(i));
            variabel.setY(yValues.get(i));
            variabel.setKorelasi(savedKorelasi);
            variabelRepository.save(variabel);
        }

        return savedKorelasi;
    }

    public KorelasiResponseDTO getKorelasiById(Long id) {
        TabelKorelasi korelasi = korelasiRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Korelasi tidak ditemukan"));

        List<TabelVariabel> variabelList = variabelRepository.findByKorelasiIdKorelasi(id);

        KorelasiResponseDTO response = new KorelasiResponseDTO();
        
        response.setIdKorelasi(korelasi.getIdKorelasi());
        response.setNamaKasus(korelasi.getNamaKasus());
        response.setNamaVarX(korelasi.getNamaVarX());
        response.setNamaVarY(korelasi.getNamaVarY());
        response.setHo(korelasi.getHo());
        response.setHa(korelasi.getHa());
        response.setAlpha(korelasi.getAlpha());
        response.setN(korelasi.getN());

        response.setXValues(variabelList.stream().map(TabelVariabel::getX).toList());
        response.setYValues(variabelList.stream().map(TabelVariabel::getY).toList());

        return response;
    }
}