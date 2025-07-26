package com.webpi.backend.validator;

import com.webpi.backend.dto.KorelasiDTO;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class KorelasiInputValidator {

    public String validate(KorelasiDTO dto) {
        
        //1. Validasi basic fields - semua field harus terisi
        if (dto.getNamaKasus() == null || dto.getNamaKasus().trim().isEmpty() ||
            dto.getNamaVarX() == null || dto.getNamaVarX().trim().isEmpty() ||
            dto.getNamaVarY() == null || dto.getNamaVarY().trim().isEmpty() ||
            dto.getAlpha() == null) {
            return "Eits, pastikan semua field terisi dengan benar ya!";
        }

        //2. Validasi input method harus dipilih
        if (dto.getInputMethod() == null || dto.getInputMethod().trim().isEmpty()) {
            return "Eits, pilih metode input data dulu ya!";
        }

        //3. Validasi xValues dan yValues tidak boleh null/empty
        List<Double> x = dto.getXValues();
        List<Double> y = dto.getYValues();
        
        if (x == null || y == null || x.isEmpty() || y.isEmpty()) {
            return "Eits, pastikan semua field terisi dengan benar ya!";
        }

        //4. Validasi minimal 5 nilai untuk X dan Y
        if (x.size() < 5 || y.size() < 5) {
            return "Minimal harus ada 5 input untuk variabel X dan Y.";
        }

        //5. Validasi jumlah X dan Y harus sama
        if (x.size() != y.size()) {
            return "Jumlah input nilai X dan Y harus sama ya.";
        }

        //6. Validasi tidak boleh ada nilai null di dalam array
        for (int i = 0; i < x.size(); i++) {
            if (x.get(i) == null || y.get(i) == null) {
                return "Pastikan nilai X dan Y tidak ada yang kosong ya.";
            }
        }

        //7. Validasi tidak boleh ada NaN dan Infinite values
        for (int i = 0; i < x.size(); i++) {
            Double xVal = x.get(i);
            Double yVal = y.get(i);
            
            if (Double.isNaN(xVal) || Double.isInfinite(xVal) ||
                Double.isNaN(yVal) || Double.isInfinite(yVal)) {
                return "Pastikan nilai X dan Y tidak ada yang kosong ya.";
            }
        }

        //8. Validasi variansi - cek apakah semua nilai sama (tidak ada variansi)
        Set<Double> uniqueX = new HashSet<>(x);
        Set<Double> uniqueY = new HashSet<>(y);
        
        if (uniqueX.size() == 1) {
            return "Nilai X tidak boleh sama semua ya.";
        }
        if (uniqueY.size() == 1) {
            return "Nilai Y tidak boleh sama semua ya.";
        }

        return null; //Semua validasi passed - return 200 OK
    }
}