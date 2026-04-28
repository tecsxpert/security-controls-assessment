package com.internship.tool.scheduler;

import com.internship.tool.entity.Assessment;
import com.internship.tool.repository.AssessmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final AssessmentRepository repo;

    // ✅ 1. DAILY REMINDER (every day at 9 AM)
    @Scheduled(cron = "0 0 9 * * ?")
    public void dailyReminder() {

        List<Assessment> pending = repo.findByStatus("PENDING");

        System.out.println("🔔 DAILY REMINDER");
        System.out.println("Pending assessments: " + pending.size());
    }


    // ✅ 2. 7-DAY DEADLINE ALERT
    @Scheduled(cron = "0 0 10 * * ?")
    public void deadlineAlert() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextWeek = now.plusDays(7);

        List<Assessment> upcoming = repo.findByDateRange(now, nextWeek);

        System.out.println("⏰ DEADLINE ALERT (Next 7 Days)");
        System.out.println("Upcoming assessments: " + upcoming.size());
    }


    // ✅ 3. WEEKLY SUMMARY (every Monday at 8 AM)
    @Scheduled(cron = "0 0 8 ? * MON")
    public void weeklySummary() {

        long total = repo.count();
        long completed = repo.findByStatus("COMPLETED").size();
        long pending = repo.findByStatus("PENDING").size();

        System.out.println("📊 WEEKLY SUMMARY");
        System.out.println("Total: " + total);
        System.out.println("Completed: " + completed);
        System.out.println("Pending: " + pending);
    }
}
