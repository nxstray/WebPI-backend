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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnovaService {

    private final AnovaRepository anovaRepository;
    private final AnovaInputValidator validator;

    // Pattern untuk mendeteksi format angka Indonesia (dengan titik sebagai pemisah ribuan)
    private static final Pattern INDONESIAN_NUMBER_PATTERN = Pattern.compile("^-?\\d{1,3}(\\.\\d{3})*([,]\\d+)?$");
    
    // Pattern untuk mendeteksi format angka dengan koma sebagai desimal
    private static final Pattern DECIMAL_COMMA_PATTERN = Pattern.compile("^-?\\d+[,]\\d+$");

    /**
     * Parse angka dengan format Indonesia
     */
    private double parseIndonesianNumber(String numberStr) throws NumberFormatException {
        if (numberStr == null || numberStr.trim().isEmpty()) {
            throw new NumberFormatException("Empty number string");
        }
        
        String cleanStr = numberStr.trim();
        
        try {
            // Cek apakah ini format Indonesia (dengan titik sebagai pemisah ribuan)
            if (INDONESIAN_NUMBER_PATTERN.matcher(cleanStr).matches()) {
                log.debug("Parsing Indonesian format number: {}", cleanStr);
                
                // Pisahkan bagian integer dan desimal
                String[] parts = cleanStr.split(",");
                String integerPart = parts[0];
                String decimalPart = parts.length > 1 ? parts[1] : "";
                
                // Hapus titik dari bagian integer (pemisah ribuan)
                String cleanIntegerPart = integerPart.replace(".", "");
                
                // Gabungkan kembali dengan titik sebagai desimal
                String finalNumberStr = decimalPart.isEmpty() ? 
                    cleanIntegerPart : 
                    cleanIntegerPart + "." + decimalPart;
                
                log.debug("Converted to: {}", finalNumberStr);
                return Double.parseDouble(finalNumberStr);
            }
            // Cek format dengan koma sebagai desimal tanpa pemisah ribuan
            else if (DECIMAL_COMMA_PATTERN.matcher(cleanStr).matches()) {
                log.debug("Parsing decimal comma format: {}", cleanStr);
                String converted = cleanStr.replace(",", ".");
                return Double.parseDouble(converted);
            }
            // Format standar (tanpa pemisah ribuan atau dengan titik sebagai desimal)
            else {
                log.debug("Parsing standard format: {}", cleanStr);
                return Double.parseDouble(cleanStr);
            }
            
        } catch (NumberFormatException e) {
            // Fallback: coba dengan NumberFormat Indonesia
            try {
                NumberFormat format = NumberFormat.getInstance(new Locale("id", "ID"));
                Number number = format.parse(cleanStr);
                return number.doubleValue();
            } catch (ParseException pe) {
                throw new NumberFormatException("Unable to parse number: " + cleanStr + " - " + e.getMessage());
            }
        }
    }

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

            int rowNumber = 0;
            for (Row row : sheet) {
                rowNumber++;
                
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                // Mapping kolom: Grup di kolom A (index 0), Nilai di kolom B (index 1)
                Cell grupCell = row.getCell(0);
                Cell nilaiCell = row.getCell(1);

                // Skip empty rows
                if ((grupCell == null || getCellValueAsString(grupCell).trim().isEmpty()) && 
                    (nilaiCell == null || getCellValueAsString(nilaiCell).trim().isEmpty())) {
                    continue;
                }
                
                // Skip jika salah satu cell kosong
                if (grupCell == null || nilaiCell == null) {
                    log.warn("Skipping row {} due to empty cells", rowNumber);
                    continue;
                }
                
                String grupName;
                double nilaiValue;
                
                // Parse grup name
                try {
                    grupName = getCellValueAsString(grupCell).trim();
                    if (grupName.isEmpty()) {
                        log.warn("Empty group name at row {}, skipping", rowNumber);
                        continue;
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse group name at row {}: {}", rowNumber, e.getMessage());
                    continue;
                }
                
                // Parse nilai dengan dukungan format Indonesia
                try {
                    String nilaiStr = getCellValueAsString(nilaiCell).trim();
                    if (nilaiStr.isEmpty()) {
                        log.warn("Empty value at row {}, skipping", rowNumber);
                        continue;
                    }
                    
                    // Gunakan method parsing baru yang mendukung format Indonesia
                    nilaiValue = parseIndonesianNumber(nilaiStr);
                    
                    log.debug("Successfully parsed row {}: Group='{}', Value={} (from '{}')", 
                            rowNumber, grupName, nilaiValue, nilaiStr);
                    
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse value at row {}: {} - {}", rowNumber, getCellValueAsString(nilaiCell), e.getMessage());
                    continue;
                }
                
                // Check for NaN and Infinite values
                if (Double.isNaN(nilaiValue) || Double.isInfinite(nilaiValue)) {
                    log.warn("Skipping invalid value at row {}: Group={}, Value={}", rowNumber, grupName, nilaiValue);
                    continue;
                }

                // Tambahkan ke grup map
                grupMap.computeIfAbsent(grupName, k -> new ArrayList<>()).add(nilaiValue);
            }

            if (grupMap.isEmpty()) {
                throw new IllegalArgumentException("Data Excel kosong atau tidak valid. Pastikan file Excel berisi data yang benar dengan format: Kolom A = Grup, Kolom B = Nilai");
            }

            if (grupMap.size() < 3) {
                throw new IllegalArgumentException("Minimal harus ada 3 kelompok untuk analisis ANOVA. Ditemukan: " + grupMap.size() + " kelompok");
            }

            // Convert map to lists
            List<String> namaGrup = new ArrayList<>(grupMap.keySet());
            List<List<Double>> nilaiGrup = new ArrayList<>();
            
            for (String grup : namaGrup) {
                List<Double> nilai = grupMap.get(grup);
                if (nilai.size() < 2) {
                    throw new IllegalArgumentException("Grup " + grup + " harus memiliki minimal 2 data. Ditemukan: " + nilai.size() + " data");
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

            // Log sample data untuk debugging
            for (int i = 0; i < Math.min(namaGrup.size(), 3); i++) {
                String grup = namaGrup.get(i);
                List<Double> values = nilaiGrup.get(i);
                log.info("Sample group '{}': {} values, first few: {}", 
                        grup, values.size(), 
                        values.subList(0, Math.min(values.size(), 3)));
            }

            return dto;
            
        } catch (Exception e) {
            log.error("Failed to read Excel file: {}", e.getMessage());
            throw new IllegalArgumentException("Gagal membaca file Excel: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method untuk mendapatkan nilai cell sebagai string
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // Cek apakah ini tanggal
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Format angka tanpa notasi ilmiah
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        // Integer value
                        return String.valueOf((long) numericValue);
                    } else {
                        // Decimal value - format dengan DecimalFormat untuk menghindari notasi ilmiah
                        DecimalFormat df = new DecimalFormat("#.#########");
                        return df.format(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                // Evaluasi formula dan konversi hasilnya
                try {
                    return getCellValueAsString(cell.getCachedFormulaResultType(), cell);
                } catch (Exception e) {
                    log.warn("Failed to evaluate formula in cell, returning empty string: {}", e.getMessage());
                    return "";
                }
            case BLANK:
            case _NONE:
            default:
                return "";
        }
    }
    
    /**
     * Helper method untuk mendapatkan nilai dari cached formula result
     */
    private String getCellValueAsString(CellType cellType, Cell cell) {
        switch (cellType) {
            case STRING:
                return cell.getRichStringCellValue().getString();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    } else {
                        DecimalFormat df = new DecimalFormat("#.#########");
                        return df.format(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
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