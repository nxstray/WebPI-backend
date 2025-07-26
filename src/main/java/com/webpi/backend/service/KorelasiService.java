package com.webpi.backend.service;

import com.webpi.backend.dto.KorelasiDTO;
import com.webpi.backend.dto.KorelasiResponseDTO;
import com.webpi.backend.entity.TabelKorelasi;
import com.webpi.backend.entity.TabelVariabel;
import com.webpi.backend.repository.KorelasiRepository;
import com.webpi.backend.repository.TabelVariabelRepository;
import com.webpi.backend.validator.KorelasiInputValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class KorelasiService {

    private final KorelasiRepository korelasiRepository;
    private final TabelVariabelRepository variabelRepository;
    private final KorelasiInputValidator validator; //Validator injection

    /**
     * Handle manual input dengan validator
     */
    public TabelKorelasi handleManualInput(KorelasiDTO dto) {
        log.info("Processing manual input for case: {}", dto.getNamaKasus());
        
        //Validasi input menggunakan validator
        String validationError = validator.validate(dto);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }

        log.info("Manual input validation passed. X values: {}, Y values: {}", 
            dto.getXValues().size(), dto.getYValues().size());

        return simpanKorelasi(dto);
    }

    /**
     * Handle Excel upload dengan parsing data dari frontend
     */
    public TabelKorelasi handleExcelUpload(MultipartFile file, KorelasiDTO frontendData) throws Exception {
        log.info("Processing Excel upload: {}", file.getOriginalFilename());
        
        // Import Excel dengan parsing data dari frontend
        KorelasiDTO dto = importExcel(file, frontendData);
        
        //Excel data juga perlu divalidasi
        String validationError = validator.validate(dto);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }
        
        return simpanKorelasi(dto);
    }

    /**
     * Simpan korelasi dengan inputMethod
     */
    @Transactional
    public TabelKorelasi simpanKorelasi(KorelasiDTO dto) {
        log.info("Saving correlation data for case: {} with inputMethod: {}", dto.getNamaKasus(), dto.getInputMethod());
        
        //Validation sudah dilakukan di validator
        
        //TabelKorelasi dengan inputMethod
        TabelKorelasi korelasi = new TabelKorelasi();
        korelasi.setNamaKasus(dto.getNamaKasus());
        korelasi.setNamaVarX(dto.getNamaVarX());
        korelasi.setNamaVarY(dto.getNamaVarY());
        korelasi.setAlpha(dto.getAlpha());
        korelasi.setN(dto.getXValues().size());
        korelasi.setInputMethod(dto.getInputMethod()); // SET inputMethod

        //TabelVariabel untuk setiap pasang data
        List<TabelVariabel> variabelList = new ArrayList<>();
        for (int i = 0; i < dto.getXValues().size(); i++) {
            TabelVariabel variabel = new TabelVariabel();
            variabel.setX(dto.getXValues().get(i));
            variabel.setY(dto.getYValues().get(i));
            variabel.setKorelasi(korelasi);
            variabelList.add(variabel);
        }

        korelasi.setDaftarVariabel(variabelList);
        
        TabelKorelasi savedKorelasi = korelasiRepository.save(korelasi);
        log.info("Correlation saved with ID: {} and inputMethod: {}", savedKorelasi.getIdKorelasi(), savedKorelasi.getInputMethod());
        
        return savedKorelasi;
    }

    /**
     * Import Excel dengan mapping kolom yang benar dan parsing data dari frontend
     */
    public KorelasiDTO importExcel(MultipartFile file, KorelasiDTO frontendData) throws Exception {
        try (InputStream inputStream = file.getInputStream()) {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            List<Double> xList = new ArrayList<>();
            List<Double> yList = new ArrayList<>();
            boolean skipHeader = true;

            //Parse data dari frontend untuk mendapatkan info case, var names, dll
            String namaKasus = frontendData.getNamaKasus();
            String namaVarX = frontendData.getNamaVarX();
            String namaVarY = frontendData.getNamaVarY();
            Double alpha = frontendData.getAlpha();

            //Coba ekstrak nama case dan variabel dari baris pertama Excel jika tersedia
            Row headerRow = sheet.getRow(0);
            if (headerRow != null) {
                //Cek jika ada nama case di cell A1
                Cell caseCell = headerRow.getCell(0);
                if (caseCell != null && caseCell.getCellType() == CellType.STRING) {
                    String excelCaseName = caseCell.getStringCellValue().trim();
                    if (!excelCaseName.isEmpty() && !excelCaseName.equalsIgnoreCase("No")) {
                        namaKasus = excelCaseName;
                    }
                }
                
                //Cek nama variabel X di cell B1 
                Cell varXCell = headerRow.getCell(1);
                if (varXCell != null && varXCell.getCellType() == CellType.STRING) {
                    String excelVarXName = varXCell.getStringCellValue().trim();
                    if (!excelVarXName.isEmpty()) {
                        namaVarX = excelVarXName;
                    }
                }
                
                //Cek nama variabel Y di cell C1
                Cell varYCell = headerRow.getCell(2);
                if (varYCell != null && varYCell.getCellType() == CellType.STRING) {
                    String excelVarYName = varYCell.getStringCellValue().trim();
                    if (!excelVarYName.isEmpty()) {
                        namaVarY = excelVarYName;
                    }
                }
            }

            for (Row row : sheet) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                //Mapping kolom yang benar - X di kolom B (index 1), Y di kolom C (index 2)
                Cell xCell = row.getCell(1); //Kolom B untuk X
                Cell yCell = row.getCell(2); //Kolom C untuk Y

                //Empty cells
                if (xCell == null || yCell == null) continue;
                
                double xValue, yValue;
                
                //Berbagai tipe cell untuk X
                try {
                    switch (xCell.getCellType()) {
                        case NUMERIC:
                            xValue = xCell.getNumericCellValue();
                            break;
                        case STRING:
                            String xStr = xCell.getStringCellValue().trim().replace(",", ".");
                            xValue = Double.parseDouble(xStr);
                            break;
                        default:
                            continue; //Skip non-numeric cells
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse X value at row {}: {}", row.getRowNum(), e.getMessage());
                    continue;
                }
                
                //Berbagai tipe cell untuk Y
                try {
                    switch (yCell.getCellType()) {
                        case NUMERIC:
                            yValue = yCell.getNumericCellValue();
                            break;
                        case STRING:
                            String yStr = yCell.getStringCellValue().trim().replace(",", ".");
                            yValue = Double.parseDouble(yStr);
                            break;
                        default:
                            continue; //Skip non-numeric cells
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse Y value at row {}: {}", row.getRowNum(), e.getMessage());
                    continue;
                }
                
                //Check for NaN and Infinite values
                if (Double.isNaN(xValue) || Double.isInfinite(xValue) || 
                    Double.isNaN(yValue) || Double.isInfinite(yValue)) {
                    log.warn("Skipping invalid values at row {}: X={}, Y={}", row.getRowNum(), xValue, yValue);
                    continue;
                }

                xList.add(xValue);
                yList.add(yValue);
            }

            if (xList.isEmpty() || yList.isEmpty()) {
                throw new IllegalArgumentException("Data Excel kosong atau tidak valid.");
            }

            //With imported and parsed data
            KorelasiDTO dto = new KorelasiDTO();
            dto.setXValues(xList);
            dto.setYValues(yList);
            dto.setNamaKasus(namaKasus); //Use parsed name from frontend/Excel
            dto.setNamaVarX(namaVarX);   //Use parsed var name from frontend/Excel
            dto.setNamaVarY(namaVarY);   //Use parsed var name from frontend/Excel
            dto.setAlpha(alpha);         //Use alpha from frontend
            dto.setInputMethod("excel"); //SET inputMethod untuk Excel

            log.info("Excel import successful. Data points: {}, Case: '{}', VarX: '{}', VarY: '{}', InputMethod: '{}'", 
                xList.size(), namaKasus, namaVarX, namaVarY, dto.getInputMethod());

            return dto;
            
        } catch (Exception e) {
            log.error("Failed to read Excel file: {}", e.getMessage());
            throw new IllegalArgumentException("Gagal membaca file Excel: " + e.getMessage(), e);
        }
    }

    /**
     * Get korelasi by ID dengan inputMethod
     */
    public KorelasiResponseDTO getKorelasiById(Long id) {
        log.info("Fetching correlation data with ID: {}", id);
        
        TabelKorelasi korelasi = korelasiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Korelasi dengan ID " + id + " tidak ditemukan"));

        List<TabelVariabel> variabelList = variabelRepository.findByKorelasiIdKorelasi(id);

        KorelasiResponseDTO response = new KorelasiResponseDTO();
        response.setIdKorelasi(korelasi.getIdKorelasi());
        response.setNamaKasus(korelasi.getNamaKasus());
        response.setNamaVarX(korelasi.getNamaVarX());
        response.setNamaVarY(korelasi.getNamaVarY());
        response.setAlpha(korelasi.getAlpha());
        response.setN(korelasi.getN());
        response.setInputMethod(korelasi.getInputMethod());
        response.setXValues(variabelList.stream().map(TabelVariabel::getX).toList());
        response.setYValues(variabelList.stream().map(TabelVariabel::getY).toList());

        log.info("Correlation data retrieved successfully for ID: {} with inputMethod: {}", id, response.getInputMethod());
        return response;
    }
}