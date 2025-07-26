package com.webpi.backend.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Anotasi kustom untuk validasi bahwa dua list memiliki ukuran yang sama.
 * Digunakan untuk memverifikasi bahwa jumlah grup dan jumlah nilai cocok.
 *
 * Target: digunakan pada level tipe (class).
 * Retention: runtime agar bisa diproses oleh validator saat program berjalan.
 * Constraint: mengacu pada kelas validator SameSizeListValidator.
 */
@Target({ ElementType.TYPE }) // Anotasi hanya bisa digunakan pada kelas
@Retention(RetentionPolicy.RUNTIME) // Anotasi tersedia saat runtime
@Constraint(validatedBy = SameSizeListValidator.class) // Menentukan kelas validator
public @interface SameSizeList {

    /**
     * Pesan default yang ditampilkan ketika validasi gagal.
     * @return pesan kesalahan
     */
    String message() default "Jumlah grup dan nilai tidak sesuai.";

    /**
     * Digunakan untuk mengelompokkan constraint. Umumnya dibiarkan default.
     * @return array kelas grup
     */
    Class<?>[] groups() default {};

    /**
     * Digunakan oleh engine validasi untuk memberikan metadata tambahan.
     * @return array kelas payload
     */
    Class<? extends Payload>[] payload() default {};
}