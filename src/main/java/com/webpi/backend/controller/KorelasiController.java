package com.webpi.backend.controller;

import com.webpi.backend.dto.KorelasiDTO;
import com.webpi.backend.dto.KorelasiResponseDTO;
import com.webpi.backend.entity.TabelKorelasi;
import com.webpi.backend.service.KorelasiService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/korelasi")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class KorelasiController {

    private final KorelasiService korelasiService;

    @PostMapping
    public ResponseEntity<TabelKorelasi> simpan(@RequestBody KorelasiDTO dto) {
        TabelKorelasi korelasi = korelasiService.simpanKorelasi(dto);
        return ResponseEntity.ok(korelasi);
    }

    @GetMapping("/{id}")
    public ResponseEntity<KorelasiResponseDTO> getById(@PathVariable Long id) {
        KorelasiResponseDTO result = korelasiService.getKorelasiById(id);
        return ResponseEntity.ok(result);
    }
}