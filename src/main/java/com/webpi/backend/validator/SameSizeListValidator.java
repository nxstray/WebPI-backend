package com.webpi.backend.validator;

import com.webpi.backend.dto.AnovaDTO;
import com.webpi.backend.dto.KorelasiDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SameSizeListValidator implements ConstraintValidator<SameSizeList, Object> {

    @Override
    public boolean isValid(Object dto, ConstraintValidatorContext context) {
        if (dto instanceof AnovaDTO anova) {
            if (anova.getNamaGrup() == null || anova.getNilaiGrup() == null) return true;
            return anova.getNamaGrup().size() == anova.getNilaiGrup().size();
        } else if (dto instanceof KorelasiDTO korelasi) {
            if (korelasi.getXValues() == null || korelasi.getYValues() == null) return true;
            return korelasi.getXValues().size() == korelasi.getYValues().size();
        }
        return true; // Untuk tipe lainnya, abaikan saja
    }
}