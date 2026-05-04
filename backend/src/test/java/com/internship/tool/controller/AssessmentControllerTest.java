package com.internship.tool.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.tool.entity.Assessment;
import com.internship.tool.repository.AssessmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AssessmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AssessmentRepository repo;

    @Autowired
    private ObjectMapper objectMapper;

    private Assessment testData;

    @BeforeEach
    void setup() {
        repo.deleteAll();

        testData = new Assessment();
        testData.setName("Test Assessment");
        testData.setDescription("Test Desc");
        testData.setStatus("PENDING");
        testData.setScore(80);
        testData.setCategory("Security");

        repo.save(testData);
    }

    // ✅ 1. TEST GET ALL
    @Test
    void testGetAll() throws Exception {
        mockMvc.perform(get("/api/assessments/all?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    // ✅ 2. TEST SEARCH
    @Test
    void testSearch() throws Exception {
        mockMvc.perform(get("/api/assessments/search?q=Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ✅ 3. TEST FILTER BY STATUS
    @Test
    void testFilterByStatus() throws Exception {
        mockMvc.perform(get("/api/assessments/status?status=PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    // ✅ 4. TEST UPDATE
    @Test
    void testUpdate() throws Exception {

        testData.setName("Updated Name");

        mockMvc.perform(put("/api/assessments/" + testData.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    // ✅ 5. TEST DELETE (SOFT DELETE)
    @Test
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/assessments/" + testData.getId()))
                .andExpect(status().isOk());
    }

    // ✅ 6. TEST STATS API
    @Test
    void testStats() throws Exception {
        mockMvc.perform(get("/api/assessments/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").exists())
                .andExpect(jsonPath("$.pending").exists());
    }

    // ✅ 7. TEST CSV EXPORT
    @Test
    void testExportCSV() throws Exception {
        mockMvc.perform(get("/api/assessments/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"));
    }
}