package com.webpi.backend.validator;

import com.webpi.backend.dto.AnovaDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validator untuk input data ANOVA.
 * Mengvalidasi logika bisnis khusus untuk analisis One-Way ANOVA.
 */
@Slf4j
@Component
public class AnovaInputValidator {

    /**
     * Validasi utama untuk input ANOVA
     */
    public String validate(AnovaDTO dto) {
        List<List<Double>> grupNilai = dto.getNilaiGrup();
        List<String> grupNama = dto.getNamaGrup();

        // Validasi grup data
        if (grupNama == null || grupNilai == null) {
            return "Eits, pastikan semua data kelompok terisi ya.";
        }

        if (grupNama.isEmpty() || grupNilai.isEmpty()) {
            return "Eits, pastikan semua data kelompok terisi ya.";
        }

        if (grupNama.size() != grupNilai.size()) {
            return "Jumlah nama grup dan nilai grup harus sama";
        }

        // Minimal 3 grup
        if (grupNama.size() < 3) {
            return "Eits, minimal tiga kelompok ya.";
        }

        // Validasi nama grup kosong atau duplikat
        if (grupNama.stream().anyMatch(n -> n == null || n.trim().isEmpty())) {
            return "Nama grup tidak boleh kosong ya.";
        }

        Set<String> uniqueGroupNames = new HashSet<>(grupNama);
        if (uniqueGroupNames.size() != grupNama.size()) {
            return "Nama grup tidak boleh duplikat ya.";
        }

        // Validasi nilai kosong dengan pesan yang konsisten
        for (int i = 0; i < grupNilai.size(); i++) {
            List<Double> nilai = grupNilai.get(i);
            
            if (nilai == null || nilai.isEmpty()) {
                return "Pastikan nilai kelompok tidak ada yang kosong ya.";
            }

            // Minimal 2 data per grup (disesuaikan dengan frontend yang membutuhkan >= 2)
            if (nilai.size() < 2) {
                return "Pastikan nilai kelompok tidak ada yang kosong ya.";
            }

            // Validasi setiap nilai dalam grup
            for (int j = 0; j < nilai.size(); j++) {
                Double value = nilai.get(j);
                if (value == null) {
                    return "Pastikan nilai kelompok tidak ada yang kosong ya.";
                }
                if (Double.isNaN(value) || Double.isInfinite(value)) {
                    return "Pastikan nilai kelompok tidak ada yang kosong ya.";
                }
            }
        }

        // Validasi variance - cek apakah ada variasi dalam data
        String varianceValidation = validateVariance(dto);
        if (varianceValidation != null) {
            return varianceValidation;
        }

        log.info("ANOVA input validation passed for case: {}", dto.getNamaKasus());
        return null;
    }

    /**
     * Validasi variance untuk memastikan ada variasi dalam data
     */
    private String validateVariance(AnovaDTO dto) {
        // Kumpulkan semua nilai dari semua grup
        List<Double> allValues = dto.getNilaiGrup().stream()
                .flatMap(List::stream)
                .toList();

        // Cek apakah semua nilai identik (tidak ada variasi sama sekali)
        Set<Double> uniqueValues = new HashSet<>(allValues);
        if (uniqueValues.size() == 1) {
            return "Semua nilai dalam data identik. ANOVA membutuhkan variasi dalam data untuk dapat dihitung.";
        }

        // Cek variance per grup dan overall
        boolean hasWithinGroupVariance = false;
        boolean hasBetweenGroupVariance = false;

        // Hitung mean per grup untuk cek between-group variance
        double[] groupMeans = new double[dto.getNilaiGrup().size()];
        for (int i = 0; i < dto.getNilaiGrup().size(); i++) {
            List<Double> grupValues = dto.getNilaiGrup().get(i);
            
            // Cek within-group variance
            Set<Double> uniqueGroupValues = new HashSet<>(grupValues);
            if (uniqueGroupValues.size() > 1) {
                hasWithinGroupVariance = true;
            }

            // Hitung mean grup
            groupMeans[i] = grupValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        }

        // Cek between-group variance dengan tolerance untuk floating point
        Set<Double> uniqueMeans = new HashSet<>();
        for (double mean : groupMeans) {
            // Round ke 6 decimal places untuk menghindari floating point precision issues
            uniqueMeans.add(Math.round(mean * 1000000.0) / 1000000.0);
        }
        if (uniqueMeans.size() > 1) {
            hasBetweenGroupVariance = true;
        }

        // Jika tidak ada variasi sama sekali (baik within maupun between group)
        if (!hasWithinGroupVariance && !hasBetweenGroupVariance) {
            return "Data tidak memiliki variasi yang cukup untuk analisis ANOVA. " +
                   "Pastikan ada perbedaan nilai dalam grup atau antar grup.";
        }

        // Log warning jika variance terbatas (tapi tidak error)
        if (!hasWithinGroupVariance || !hasBetweenGroupVariance) {
            log.warn("Data memiliki variasi terbatas. Within-group variance: {}, Between-group variance: {}", 
                    hasWithinGroupVariance, hasBetweenGroupVariance);
        }

        return null;
    }

    /**
     * Hitung variance untuk array nilai (tidak digunakan saat ini, tapi berguna untuk debugging)
     */
    @SuppressWarnings("unused")
    private double calculateVariance(List<Double> values) {
        if (values.size() < 2) return 0.0;
        
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double sumSquaredDiff = values.stream()
                .mapToDouble(value -> Math.pow(value - mean, 2))
                .sum();
        
        return sumSquaredDiff / (values.size() - 1);
    }
}