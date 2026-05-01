package com.internship.tool.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.tool.entity.Assessment;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AssessmentIntegrationTest {

    // ✅ PostgreSQL Container
    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("testdb")
                    .withUsername("postgres")
                    .withPassword("password");

    // ✅ Redis Container
    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>("redis:7")
                    .withExposedPorts(6379);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String baseUrl;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
    }

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port + "/api/assessments";
    }

    // ✅ FULL CRUD FLOW TEST
    @Test
    void testFullCrudFlow() throws Exception {

        // 🔹 CREATE
        Assessment newAssessment = new Assessment();
        newAssessment.setName("Integration Test");
        newAssessment.setDescription("Full Flow");
        newAssessment.setStatus("PENDING");
        newAssessment.setScore(75);
        newAssessment.setCategory("Test");

        ResponseEntity<Assessment> createRes =
                restTemplate.postForEntity(baseUrl, newAssessment, Assessment.class);

        assertEquals(HttpStatus.OK, createRes.getStatusCode());
        assertNotNull(createRes.getBody());

        Long id = createRes.getBody().getId();

        // 🔹 GET ALL
        ResponseEntity<String> getRes =
                restTemplate.getForEntity(baseUrl + "/all?page=0&size=5", String.class);

        assertEquals(HttpStatus.OK, getRes.getStatusCode());
        assertTrue(getRes.getBody().contains("Integration Test"));

        // 🔹 UPDATE
        newAssessment.setName("Updated Integration");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request =
                new HttpEntity<>(objectMapper.writeValueAsString(newAssessment), headers);

        ResponseEntity<Assessment> updateRes =
                restTemplate.exchange(baseUrl + "/" + id,
                        HttpMethod.PUT, request, Assessment.class);

        assertEquals(HttpStatus.OK, updateRes.getStatusCode());
        assertEquals("Updated Integration", updateRes.getBody().getName());

        // 🔹 DELETE (SOFT)
        restTemplate.delete(baseUrl + "/" + id);

        // 🔹 VERIFY DELETE
        ResponseEntity<String> afterDelete =
                restTemplate.getForEntity(baseUrl + "/all?page=0&size=5", String.class);

        assertTrue(afterDelete.getBody().contains("DELETED"));
    }
}