package com.webpi.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webpi.backend.dto.AnovaDTO;
import com.webpi.backend.dto.AnovaResponseDTO;
import com.webpi.backend.entity.TabelAnova;
import com.webpi.backend.service.AnovaService;
import com.webpi.backend.validator.AnovaInputValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/anova")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnovaController {

    private final AnovaService anovaService;
    private final AnovaInputValidator validator;
    private final ObjectMapper objectMapper;

    @PostMapping(path = "/manual", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> simpanAnovaManual(@RequestBody AnovaDTO dto) {
        try {
            log.info("=== MANUAL ANOVA REQUEST ===");
            log.info("DTO received: {}", dto);
            log.info("namaKasus: '{}'", dto.getNamaKasus());
            log.info("namaVariableDependen: '{}'", dto.getNamaVariableDependen());
            log.info("namaVariableIndependen: '{}'", dto.getNamaVariableIndependen());
            log.info("alpha: {}", dto.getAlpha());
            log.info("inputMethod: '{}'", dto.getInputMethod());
            log.info("namaGrup: {}", dto.getNamaGrup());
            log.info("nilaiGrup: {}", dto.getNilaiGrup());
            
            if (dto.getNamaGrup() != null) {
                log.info("namaGrup size: {}", dto.getNamaGrup().size());
            }
            if (dto.getNilaiGrup() != null) {
                log.info("nilaiGrup size: {}", dto.getNilaiGrup().size());
            }
            log.info("=============================");

            // Validasi basic manual input
            if (dto.getNamaGrup() == null || dto.getNilaiGrup() == null ||
                    dto.getNamaGrup().isEmpty() || dto.getNilaiGrup().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Eits, pastikan semua field terisi dengan benar ya!"));
            }
            
            if (dto.getNamaGrup().size() != dto.getNilaiGrup().size()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Eits, pastikan semua data kelompok terisi ya."));
            }

            // Validasi menggunakan AnovaInputValidator
            String validationError = validator.validate(dto);
            if (validationError != null) {
                log.warn("Validation error: {}", validationError);
                return ResponseEntity.badRequest().body(new ErrorResponse(validationError));
            }

            // Simpan data ANOVA
            TabelAnova hasil = anovaService.simpanAnova(dto);
            
            // Return dengan ID untuk frontend
            return ResponseEntity.ok(new SuccessResponse(hasil.getIdAnova(), "Data berhasil disimpan"));
            
        } catch (IllegalArgumentException e) {
            // Return 400 Bad Request dengan pesan validasi
            log.warn("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
            
        } catch (Exception e) {
            // Return 500 Internal Server Error
            log.error("Unexpected error processing manual input:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Terjadi kesalahan server: " + e.getMessage()));
        }
    }

    @PostMapping(path = "/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> simpanAnovaExcel(
            @RequestPart("data") String jsonData,
            @RequestPart("file") MultipartFile file) {
        try {
            log.info("=== EXCEL ANOVA REQUEST ===");
            log.info("File: {}", file.getOriginalFilename());
            log.info("File size: {} bytes", file.getSize());
            log.info("JSON Data: {}", jsonData);
            
            // Validasi file Excel terlebih dahulu
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("File Excel tidak ditemukan atau kosong."));
            }
            
            // Validasi tipe file
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || 
                (!originalFilename.toLowerCase().endsWith(".xlsx") && 
                 !originalFilename.toLowerCase().endsWith(".xls"))) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("Format file tidak didukung. Gunakan file Excel (.xlsx atau .xls)")
                );
            }
            
            // Validasi ukuran file (misalnya max 10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("Ukuran file terlalu besar. Maksimal 10MB.")
                );
            }

            // Parse JSON data dari frontend
            AnovaDTO frontendData;
            try {
                frontendData = objectMapper.readValue(jsonData, AnovaDTO.class);
            } catch (IOException e) {
                log.error("JSON parsing error: {}", e.getMessage());
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("Format data JSON tidak valid: " + e.getMessage())
                );
            }
            
            log.info("Parsed frontend data: {}", frontendData);
            log.info("namaKasus: '{}'", frontendData.getNamaKasus());
            log.info("namaVariableDependen: '{}'", frontendData.getNamaVariableDependen());
            log.info("namaVariableIndependen: '{}'", frontendData.getNamaVariableIndependen());
            log.info("alpha: {}", frontendData.getAlpha());
            log.info("===========================");

            // Handle Excel upload dengan data dari frontend
            TabelAnova hasil = anovaService.handleExcelUpload(file, frontendData);
            
            // Return dengan ID untuk frontend
            return ResponseEntity.ok(new SuccessResponse(hasil.getIdAnova(), "Excel berhasil diproses dan data disimpan"));
            
        } catch (IllegalArgumentException e) {
            // Error dari validasi atau parsing Excel
            log.warn("Excel validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
            
        } catch (Exception e) {
            // Error tak terduga
            log.error("Unexpected error processing Excel upload:", e);
            
            // Cek apakah error terkait file Excel yang corrupt
            if (e.getMessage() != null && 
                (e.getMessage().contains("Invalid header signature") ||
                 e.getMessage().contains("unable to read signature") ||
                 e.getMessage().contains("corrupted"))) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("File Excel rusak atau tidak valid. Silakan coba file Excel yang lain.")
                );
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Terjadi kesalahan server saat memproses Excel: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            log.info("Fetching anova with ID: {}", id);
            
            // Validasi ID
            if (id == null || id <= 0) {
                return ResponseEntity.badRequest().body(new ErrorResponse("ID tidak valid"));
            }
            
            AnovaResponseDTO response = anovaService.getAnovaById(id);
            log.info("Successfully retrieved ANOVA data with ID: {}", id);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("Anova not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Data ANOVA dengan ID " + id + " tidak ditemukan"));
                
        } catch (Exception e) {
            log.error("Unexpected error fetching ANOVA with ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Terjadi kesalahan server: " + e.getMessage()));
        }
    }

    // RESPONSE DTOs
    record ErrorResponse(String message) {}
    record SuccessResponse(Long idAnova, String message) {}
}