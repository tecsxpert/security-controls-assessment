package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
public class ReportController {

    @GetMapping("/api/report-stream")
    public SseEmitter streamReport() {

        SseEmitter emitter = new SseEmitter();

        new Thread(() -> {
            try {

                emitter.send("Generating report...");
                Thread.sleep(1000);

                emitter.send("Loading dashboard data...");
                Thread.sleep(1000);

                emitter.send("AI processing started...");
                Thread.sleep(1000);

                emitter.send("Finalizing report...");
                Thread.sleep(1000);

                emitter.send("Report completed!");

                emitter.complete();

            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }
}