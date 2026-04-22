package ro.echipa12.orar_personal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    
    // Căutăm dacă agentul are deja tură în ziua respectivă (pentru Smart Update)
    Optional<Schedule> findByAgentNameAndWorkDate(String agentName, LocalDate workDate);

    // Metoda pentru ștergerea selectivă a unei luni
    @Transactional
    @Modifying
    @Query("DELETE FROM Schedule s WHERE s.workDate >= :start AND s.workDate <= :end")
    void deleteByMonth(@Param("start") LocalDate start, @Param("end") LocalDate end);

    // Metoda de ștergere totală (Reset general)
    void deleteAll();
}