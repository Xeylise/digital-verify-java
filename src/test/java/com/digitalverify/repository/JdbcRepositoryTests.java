package com.digitalverify.repository;

import com.digitalverify.model.Bet;
import com.digitalverify.model.KycCase;
import com.digitalverify.model.Notification;
import com.digitalverify.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JdbcRepositoryTests {

    @Autowired
    private UserJdbcRepository userRepo;

    @Autowired
    private KycCaseJdbcRepository caseRepo;

    @Autowired
    private BetJdbcRepository betRepo;

    @Autowired
    private NotificationJdbcRepository notifRepo;

    @Autowired
    private AuditLogJdbcRepository auditRepo;

    @Test
    @DisplayName("Should find user by email and verify seed data")
    void testFindUserByEmail() {
        Optional<User> user = userRepo.findByEmail("carlos.mendoza@email.com");
        assertTrue(user.isPresent());
        assertEquals("Carlos Mendoza", user.get().getFullName());
        assertEquals("PLAYER", user.get().getRole());
    }

    @Test
    @DisplayName("Should query all cases and count by status")
    void testCaseRepositoryQueries() {
        List<KycCase> cases = caseRepo.findAll();
        assertFalse(cases.isEmpty());
        assertTrue(cases.size() >= 5);

        long pending = caseRepo.countByStatus("pending");
        assertTrue(pending >= 1);

        long total = caseRepo.countTotal();
        assertTrue(total >= 5);
    }

    @Test
    @DisplayName("Should update case status and decision using JDBC")
    void testUpdateCaseStatus() {
        Optional<KycCase> caseOpt = caseRepo.findById(1L);
        assertTrue(caseOpt.isPresent());

        int updated = caseRepo.updateStatusAndDecision(1L, "approved", "Paola Castro", "Approved in test");
        assertEquals(1, updated);

        Optional<KycCase> updatedCase = caseRepo.findById(1L);
        assertTrue(updatedCase.isPresent());
        assertEquals("approved", updatedCase.get().getStatus());
    }

    @Test
    @DisplayName("Should find bets for user ID 1")
    void testBetRepository() {
        List<Bet> bets = betRepo.findByUserId(1L);
        assertFalse(bets.isEmpty());
        assertEquals("Real Madrid vs Manchester City", bets.get(0).getEventName());
    }

    @Test
    @DisplayName("Should query and mark notifications as read")
    void testNotificationRepository() {
        long unreadBefore = notifRepo.countUnreadByUserId(1L);
        assertTrue(unreadBefore > 0);

        List<Notification> notifs = notifRepo.findByUserId(1L);
        assertFalse(notifs.isEmpty());

        int marked = notifRepo.markAsRead(notifs.get(0).getId());
        assertEquals(1, marked);
    }

    @Test
    @DisplayName("Should find audit logs")
    void testAuditLogRepository() {
        assertFalse(auditRepo.findAll().isEmpty());
    }
}
