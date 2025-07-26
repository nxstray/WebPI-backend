package com.webpi.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webpi.backend.dto.KorelasiDTO;
import com.webpi.backend.dto.KorelasiResponseDTO;
import com.webpi.backend.entity.TabelKorelasi;
import com.webpi.backend.service.KorelasiService;
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
@RequestMapping("/api/korelasi")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class KorelasiController {

    private final KorelasiService korelasiService;
    private final ObjectMapper objectMapper;

    @PostMapping(path = "/manual", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> simpanKorelasiManual(@RequestBody KorelasiDTO dto) {
        try {
            log.info("=== MANUAL KORELASI REQUEST ===");
            log.info("DTO received: {}", dto);
            log.info("namaKasus: '{}'", dto.getNamaKasus());
            log.info("namaVarX: '{}'", dto.getNamaVarX());
            log.info("namaVarY: '{}'", dto.getNamaVarY());
            log.info("alpha: {}", dto.getAlpha());
            log.info("inputMethod: '{}'", dto.getInputMethod());
            log.info("xValues: {}", dto.getXValues());
            log.info("yValues: {}", dto.getYValues());
            
            if (dto.getXValues() != null) {
                log.info("xValues size: {}", dto.getXValues().size());
            }
            if (dto.getYValues() != null) {
                log.info("yValues size: {}", dto.getYValues().size());
            }
            log.info("==============================");

            //Let service handle validation with validator
            TabelKorelasi hasil = korelasiService.handleManualInput(dto);
            
            //Return with ID for frontend
            return ResponseEntity.ok(new SuccessResponse(hasil.getIdKorelasi(), "Data berhasil disimpan"));
            
        } catch (IllegalArgumentException e) {
            //Return 400 Bad Request with validation message
            log.warn("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
            
        } catch (Exception e) {
            //Return 500 Internal Server Error
            log.error("Unexpected error processing manual input:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Terjadi kesalahan server: " + e.getMessage()));
        }
    }

    @PostMapping(path = "/excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> simpanKorelasiExcel(
            @RequestPart("data") String jsonData,
            @RequestPart("file") MultipartFile file) {
        try {
            log.info("=== EXCEL KORELASI REQUEST ===");
            log.info("File: {}", file.getOriginalFilename());
            log.info("JSON Data: {}", jsonData);
            
            KorelasiDTO frontendData = objectMapper.readValue(jsonData, KorelasiDTO.class);
            log.info("Parsed frontend data: {}", frontendData);
            log.info("=============================");

            //Let service handle validation
            TabelKorelasi hasil = korelasiService.handleExcelUpload(file, frontendData);
            
            //Return with ID for frontend
            return ResponseEntity.ok(new SuccessResponse(hasil.getIdKorelasi(), "Excel berhasil diproses"));
            
        } catch (IOException e) {
            log.error("JSON parsing error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse("Gagal membaca data JSON: " + e.getMessage()));
            
        } catch (IllegalArgumentException e) {
            //Return 400 Bad Request with validation message
            log.warn("Excel validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
            
        } catch (Exception e) {
            log.error("Unexpected error processing Excel upload:", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Terjadi kesalahan server: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<KorelasiResponseDTO> getById(@PathVariable Long id) {
        try {
            log.info("Fetching korelasi with ID: {}", id);
            KorelasiResponseDTO response = korelasiService.getKorelasiById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Korelasi not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    //RESPONSE DTOs
    record ErrorResponse(String message) {}
    record SuccessResponse(Long idKorelasi, String message) {}
}