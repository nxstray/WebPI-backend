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

            // Validasi manual input
            if (dto.getNamaGrup() == null || dto.getNilaiGrup() == null ||
                    dto.getNamaGrup().isEmpty() || dto.getNilaiGrup().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Eits, pastikan semua field terisi dengan benar ya!."));
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
            log.info("JSON Data: {}", jsonData);
            
            AnovaDTO frontendData = objectMapper.readValue(jsonData, AnovaDTO.class);
            log.info("Parsed frontend data: {}", frontendData);
            log.info("namaVariableIndependen: '{}'", frontendData.getNamaVariableIndependen());
            log.info("===========================");

            // Validasi file Excel
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("File Excel tidak ditemukan."));
            }

            // Handle Excel upload dengan data dari frontend - mirip KorelasiService
            TabelAnova hasil = anovaService.handleExcelUpload(file, frontendData);
            
            // Return dengan ID untuk frontend
            return ResponseEntity.ok(new SuccessResponse(hasil.getIdAnova(), "Excel berhasil diproses"));
            
        } catch (IOException e) {
            log.error("JSON parsing error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse("Gagal membaca data JSON: " + e.getMessage()));
            
        } catch (IllegalArgumentException e) {
            // Return 400 Bad Request dengan pesan validasi
            log.warn("Excel validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
            
        } catch (Exception e) {
            log.error("Unexpected error processing Excel upload:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Terjadi kesalahan server: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnovaResponseDTO> getById(@PathVariable Long id) {
        try {
            log.info("Fetching anova with ID: {}", id);
            AnovaResponseDTO response = anovaService.getAnovaById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Anova not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    // RESPONSE DTOs
    record ErrorResponse(String message) {}
    record SuccessResponse(Long idAnova, String message) {}
}