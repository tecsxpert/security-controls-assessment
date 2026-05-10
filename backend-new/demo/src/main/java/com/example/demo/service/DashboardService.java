package com.example.demo.service;

import com.example.demo.dto.DashboardDTO;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    public DashboardDTO getStats() {

        return new DashboardDTO(
                100,
                80,
                20,
                35
        );
    }
}