package com.webpi.backend.config;

// Import ObjectMapper dari pustaka Jackson untuk serialisasi dan deserialisasi JSON
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

// Import anotasi konfigurasi dari Spring
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Konfigurasi untuk mengatur behavior Jackson ObjectMapper di seluruh aplikasi.
 * Digunakan untuk mengatur bagaimana JSON akan diproses saat deserialisasi.
 */
@Configuration
public class JacksonConfig {

    /**
     * Membuat dan mengembalikan instance ObjectMapper yang dikustomisasi.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Mengabaikan properti yang tidak dikenali saat proses deserialisasi JSON
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Mengizinkan satu nilai tunggal diperlakukan sebagai array selama deserialisasi
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        return mapper;
    }
}