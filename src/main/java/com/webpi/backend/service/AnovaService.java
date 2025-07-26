package com.webpi.backend.service;

import com.webpi.backend.dto.AnovaDTO;
import com.webpi.backend.dto.AnovaResponseDTO;
import com.webpi.backend.entity.TabelAnova;
import com.webpi.backend.entity.TabelGrup;
import com.webpi.backend.repository.AnovaRepository;
import com.webpi.backend.validator.AnovaInputValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnovaService {

    private final AnovaRepository anovaRepository;
    private final AnovaInputValidator validator;

    /**
     * Simpan data ANOVA dari input manual
     */
    @Transactional
    public TabelAnova simpanAnova(AnovaDTO dto) {
        log.info("Saving ANOVA data for case: {} with inputMethod: {}", dto.getNamaKasus(), dto.getInputMethod());
        
        // Validasi sudah dilakukan di controller
        
        // Buat entitas TabelAnova
        TabelAnova anova = new TabelAnova();
        anova.setNamaKasus(dto.getNamaKasus());
        anova.setNamaVariableDependen(dto.getNamaVariableDependen());
        anova.setNamaVariableIndependen(dto.getNamaVariableIndependen());
        anova.setAlpha(dto.getAlpha());
        anova.setInputMethod(dto.getInputMethod());
        
        // Hitung n dan k
        int totalN = dto.getNilaiGrup().stream()
                .mapToInt(List::size)
                .sum();
        anova.setN(totalN);
        anova.setK(dto.getNamaGrup().size());

        // Buat TabelGrup untuk setiap grup
        List<TabelGrup> grupList = new ArrayList<>();
        for (int i = 0; i < dto.getNamaGrup().size(); i++) {
            String namaGrup = dto.getNamaGrup().get(i);
            List<Double> nilaiGrup = dto.getNilaiGrup().get(i);
            
            for (Double nilai : nilaiGrup) {
                TabelGrup grup = new TabelGrup();
                grup.setGrup(namaGrup);
                grup.setNilai(nilai);
                grup.setAnova(anova);
                grupList.add(grup);
            }
        }
        
        anova.setGrups(grupList);
        
        TabelAnova savedAnova = anovaRepository.save(anova);
        log.info("ANOVA saved with ID: {} and inputMethod: {}", savedAnova.getIdAnova(), savedAnova.getInputMethod());
        
        return savedAnova;
    }

    /**
     * Handle Excel upload dengan parsing data dari frontend
     */
    public TabelAnova handleExcelUpload(MultipartFile file, AnovaDTO frontendData) throws Exception {
        log.info("Processing Excel upload: {}", file.getOriginalFilename());
        
        // Import Excel dengan parsing data dari frontend
        AnovaDTO dto = importExcel(file, frontendData);
        
        // Excel data juga perlu divalidasi
        String validationError = validator.validate(dto);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }
        
        return simpanAnova(dto);
    }

    /**
     * Import Excel dengan mapping kolom yang benar dan parsing data dari frontend
     */
    public AnovaDTO importExcel(MultipartFile file, AnovaDTO frontendData) throws Exception {
        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            Map<String, List<Double>> grupMap = new HashMap<>();
            boolean skipHeader = true;

            // Parse data dari frontend untuk mendapatkan info case, var names, dll
            String namaKasus = frontendData.getNamaKasus();
            String namaVariableDependen = frontendData.getNamaVariableDependen();
            String namaVariableIndependen = frontendData.getNamaVariableIndependen();
            Double alpha = frontendData.getAlpha();

            // Coba ekstrak nama case dan variabel dari baris pertama Excel jika tersedia
            Row headerRow = sheet.getRow(0);
            if (headerRow != null) {
                // Cek jika ada nama case di cell A1
                Cell caseCell = headerRow.getCell(0);
                if (caseCell != null && caseCell.getCellType() == CellType.STRING) {
                    String excelCaseName = caseCell.getStringCellValue().trim();
                    if (!excelCaseName.isEmpty() && !excelCaseName.equalsIgnoreCase("Grup")) {
                        namaKasus = excelCaseName;
                    }
                }
                
                // Cek nama variabel independen di cell A1 (nama grup)
                Cell varIndependenCell = headerRow.getCell(0);
                if (varIndependenCell != null && varIndependenCell.getCellType() == CellType.STRING) {
                    String excelVarIndependenName = varIndependenCell.getStringCellValue().trim();
                    if (!excelVarIndependenName.isEmpty() && !excelVarIndependenName.equalsIgnoreCase("Grup")) {
                        namaVariableIndependen = excelVarIndependenName;
                    }
                }
                
                // Cek nama variabel dependen di cell B1
                Cell varDependenCell = headerRow.getCell(1);
                if (varDependenCell != null && varDependenCell.getCellType() == CellType.STRING) {
                    String excelVarDependenName = varDependenCell.getStringCellValue().trim();
                    if (!excelVarDependenName.isEmpty() && !excelVarDependenName.equalsIgnoreCase("Nilai")) {
                        namaVariableDependen = excelVarDependenName;
                    }
                }
            }

            for (Row row : sheet) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                // Mapping kolom: Grup di kolom A (index 0), Nilai di kolom B (index 1)
                Cell grupCell = row.getCell(0);
                Cell nilaiCell = row.getCell(1);

                // Skip empty cells
                if (grupCell == null || nilaiCell == null) continue;
                
                String grupName;
                double nilaiValue;
                
                // Parse grup name
                try {
                    switch (grupCell.getCellType()) {
                        case STRING:
                            grupName = grupCell.getStringCellValue().trim();
                            break;
                        case NUMERIC:
                            grupName = String.valueOf((int) grupCell.getNumericCellValue());
                            break;
                        default:
                            continue; // Skip invalid cells
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse group name at row {}: {}", row.getRowNum(), e.getMessage());
                    continue;
                }
                
                // Parse nilai - MEMPERBOLEHKAN NILAI NEGATIF DAN DESIMAL
                try {
                    switch (nilaiCell.getCellType()) {
                        case NUMERIC:
                            nilaiValue = nilaiCell.getNumericCellValue();
                            break;
                        case STRING:
                            String nilaiStr = nilaiCell.getStringCellValue().trim().replace(",", ".");
                            nilaiValue = Double.parseDouble(nilaiStr);
                            break;
                        default:
                            continue; // Skip non-numeric cells
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse value at row {}: {}", row.getRowNum(), e.getMessage());
                    continue;
                }
                
                // Check for NaN and Infinite values
                if (Double.isNaN(nilaiValue) || Double.isInfinite(nilaiValue)) {
                    log.warn("Skipping invalid value at row {}: Group={}, Value={}", row.getRowNum(), grupName, nilaiValue);
                    continue;
                }

                // Tambahkan ke grup map
                grupMap.computeIfAbsent(grupName, k -> new ArrayList<>()).add(nilaiValue);
            }

            if (grupMap.isEmpty()) {
                throw new IllegalArgumentException("Data Excel kosong atau tidak valid.");
            }

            if (grupMap.size() < 3) {
                throw new IllegalArgumentException("Minimal harus ada 3 kelompok untuk analisis ANOVA.");
            }

            // Convert map to lists
            List<String> namaGrup = new ArrayList<>(grupMap.keySet());
            List<List<Double>> nilaiGrup = new ArrayList<>();
            
            for (String grup : namaGrup) {
                List<Double> nilai = grupMap.get(grup);
                if (nilai.size() < 2) {
                    throw new IllegalArgumentException("Grup " + grup + " harus memiliki minimal 2 data.");
                }
                nilaiGrup.add(nilai);
            }

            // Buat DTO dengan imported and parsed data
            AnovaDTO dto = new AnovaDTO();
            dto.setNamaKasus(namaKasus);
            dto.setNamaVariableDependen(namaVariableDependen);
            dto.setNamaVariableIndependen(namaVariableIndependen);
            dto.setAlpha(alpha);
            dto.setInputMethod("excel");
            dto.setNamaGrup(namaGrup);
            dto.setNilaiGrup(nilaiGrup);

            // Hitung n dan k
            int totalN = nilaiGrup.stream().mapToInt(List::size).sum();
            dto.setN(totalN);
            dto.setK(namaGrup.size());

            log.info("Excel import successful. Groups: {}, Total data points: {}, Case: '{}', VarDependen: '{}', VarIndependen: '{}', InputMethod: '{}'", 
                    namaGrup.size(), totalN, namaKasus, namaVariableDependen, namaVariableIndependen, dto.getInputMethod());

            return dto;
            
        } catch (Exception e) {
            log.error("Failed to read Excel file: {}", e.getMessage());
            throw new IllegalArgumentException("Gagal membaca file Excel: " + e.getMessage(), e);
        }
    }

    /**
     * Get ANOVA by ID
     */
    public AnovaResponseDTO getAnovaById(Long id) {
        log.info("Fetching ANOVA data with ID: {}", id);
        
        TabelAnova anova = anovaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ANOVA dengan ID " + id + " tidak ditemukan"));

        AnovaResponseDTO response = new AnovaResponseDTO();
        response.setIdAnova(anova.getIdAnova());
        response.setNamaKasus(anova.getNamaKasus());
        response.setNamaVariableDependen(anova.getNamaVariableDependen());
        response.setNamaVariableIndependen(anova.getNamaVariableIndependen());
        response.setAlpha(anova.getAlpha());
        response.setN(anova.getN());
        response.setK(anova.getK());
        response.setInputMethod(anova.getInputMethod());

        // Convert TabelGrup to GrupDTO
        List<AnovaResponseDTO.GrupDTO> grupDTOs = new ArrayList<>();
        for (TabelGrup grup : anova.getGrups()) {
            AnovaResponseDTO.GrupDTO grupDTO = new AnovaResponseDTO.GrupDTO();
            grupDTO.setGrup(grup.getGrup());
            grupDTO.setNilai(grup.getNilai());
            grupDTOs.add(grupDTO);
        }
        response.setGrups(grupDTOs);

        log.info("ANOVA data retrieved successfully for ID: {} with inputMethod: {}", id, response.getInputMethod());
        return response;
    }
}