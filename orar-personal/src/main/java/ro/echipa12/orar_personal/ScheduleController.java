package ro.echipa12.orar_personal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orar")
@CrossOrigin(origins = "*") // Permite accesul din browser
public class ScheduleController {

    @Autowired
    private ExcelParserService excelParserService;
    @Autowired
    private ScheduleRepository scheduleRepository;

    // Aici primim fisierul Excel prin internet/browser
    @PostMapping("/upload")
    public ResponseEntity<String> uploadExcelFile(@RequestParam("file") MultipartFile file) {
        // Trimitem fisierul la "Detectivul" nostru
        String result = excelParserService.processExcelFile(file);
        
        // Returnam raspunsul (succes sau eroare) inapoi in browser
        return ResponseEntity.ok(result);
    }
    @GetMapping("/toate")
    public ResponseEntity<List<Schedule>> getAllSchedules() {
        return ResponseEntity.ok(scheduleRepository.findAll());
    }
    @DeleteMapping("/sterge-luna")
public ResponseEntity<?> deleteMonth(@RequestParam int month, @RequestParam int year) {
    // Calculăm prima și ultima zi a lunii
    LocalDate start = LocalDate.of(year, month, 1);
    LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
    
    // Apelăm metoda din repository pe care am scris-o data trecută
    scheduleRepository.deleteByMonth(start, end);
    
    return ResponseEntity.ok().build();
}
}