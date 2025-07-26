package com.webpi.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Konfigurasi Web untuk mengatur pengaturan CORS (Cross-Origin Resource Sharing).
 * CORS digunakan agar frontend yang berjalan di domain berbeda dapat berkomunikasi dengan backend.
 */
@Configuration
public class WebConfig {

    /**
     * Mendefinisikan bean konfigurasi WebMvcConfigurer untuk pengaturan CORS.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            
            /**
             * Menambahkan aturan CORS ke semua endpoint backend.
             *
             */
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Mengizinkan semua path diakses dari origin tertentu
                        .allowedOrigins("http://localhost:4200", "https://webpi-frontend.vercel.app") // Origin yang diizinkan
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Metode HTTP yang diizinkan
                        .allowedHeaders("*") // Mengizinkan semua jenis header
                        .maxAge(3600); // Menentukan berapa lama (dalam detik) hasil pre-flight request dapat disimpan (cache)
            }
        };
    }
}