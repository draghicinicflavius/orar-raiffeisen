package ro.echipa12.orar_personal;

import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.time.LocalDate;


@Service
public class ExcelParserService {

    @Autowired
    private ScheduleRepository scheduleRepository;

    public String processExcelFile(MultipartFile file) {
    if (file.isEmpty()) return "Fișierul este gol.";

    try {
        InputStream inputStream = file.getInputStream();
        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);
        Row dateRow = sheet.getRow(0);

        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();
        int count = 0;

        for (int i = 2; i <= sheet.getLastRowNum(); i++) {
            Row currentRow = sheet.getRow(i);
            if (currentRow == null) continue;

            Cell nameCell = currentRow.getCell(0);
            if (nameCell == null || nameCell.getCellType() != CellType.STRING) continue;
            String agentName = nameCell.getStringCellValue().trim();

            for (int j = 1; j < currentRow.getLastCellNum(); j++) {
                String shiftDetails = getCellText(currentRow.getCell(j));
                String dateNumberStr = getCellText(dateRow.getCell(j));

                if (!shiftDetails.isEmpty() && !dateNumberStr.isEmpty()) {
                    try {
                        int day = 1, month = currentMonth, year = currentYear;
                        if (dateNumberStr.contains(".")) {
                            String[] parts = dateNumberStr.split("\\.");
                            day = Integer.parseInt(parts[0]);
                            month = Integer.parseInt(parts[1]);
                            year = Integer.parseInt(parts[2]);
                            if (year < 100) year += 2000;
                        } else {
                            day = (int) Double.parseDouble(dateNumberStr);
                        }

                        LocalDate date = LocalDate.of(year, month, day);

                        // VERIFICARE SMART: Există deja tura asta?
                        var existing = scheduleRepository.findByAgentNameAndWorkDate(agentName, date);
                        
                        Schedule s = existing.orElse(new Schedule());
                        s.setAgentName(agentName);
                        s.setWorkDate(date);
                        s.setShiftDetails(shiftDetails);
                        
                        scheduleRepository.save(s);
                        count++;
                    } catch (Exception e) {}
                }
            }
        }
        return "Am procesat " + count + " ture. Datele au fost acumulate/actualizate.";
    } catch (Exception e) {
        return "Eroare: " + e.getMessage();
    }
}

    // METODA CARE LIPSEA DIN CODUL TĂU:
    private String getCellText(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                LocalDate date = cell.getLocalDateTimeCellValue().toLocalDate();
                return date.getDayOfMonth() + "." + date.getMonthValue() + "." + date.getYear();
            }
            // Returnăm numărul ca string (ex: "20.0" devine "20")
            double numericValue = cell.getNumericCellValue();
            if (numericValue == (long) numericValue) {
                return String.format("%d", (long) numericValue);
            } else {
                return String.valueOf(numericValue);
            }
        }
        return "";
    }
}
