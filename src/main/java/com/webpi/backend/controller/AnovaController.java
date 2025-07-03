package com.webpi.backend.controller;

import com.webpi.backend.dto.AnovaDTO;
import com.webpi.backend.dto.AnovaResponseDTO;
import com.webpi.backend.entity.TabelAnova;
import com.webpi.backend.service.AnovaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/anova")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AnovaController {

    private final AnovaService anovaService;

    @PostMapping
    public TabelAnova simpanAnova(@RequestBody AnovaDTO dto) {
        return anovaService.simpanAnova(dto);
    }

    @GetMapping("/{id}")
    public AnovaResponseDTO getAnovaById(@PathVariable Long id) {
        return anovaService.getAnovaById(id);
    }
}