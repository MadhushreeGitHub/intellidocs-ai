package com.intellidocs.intellidocs_ai.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellidocs.intellidocs_ai.dto.request.RegisterRequest;
import com.intellidocs.intellidocs_ai.repository.DocumentRepository;
import com.intellidocs.intellidocs_ai.repository.TenantRepository;
import com.intellidocs.intellidocs_ai.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.data.domain.Pageable;
import static org.hamcrest.Matchers.*;

@SpringBootTest()
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IngestionPipelineIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    //Shared state across tests in this class
    private static String documentId;
    private static String authToken;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @BeforeAll
    static void cleanupBefore(@Autowired UserRepository userRepo,
                              @Autowired TenantRepository tenantRepo,
                              @Autowired DocumentRepository documentRepository) {

        // Clean up any leftover test data from previous runs
        userRepo.findByEmail("integration-test@test.com")
                .ifPresent(u -> {
                    //delete documents first
                    documentRepository.findByTenantId(u.getTenantId(), Pageable.unpaged())
                            .forEach(document -> documentRepository.delete(document));
                    //delete User
                    userRepo.delete(u);

                    //Then delete tenant
                    tenantRepo.findBySlug("integration-test-co")
                            .ifPresent(tenantRepo::delete);
                });
    }

    @Test
    @Order(1)
    void registerShouldReturn201withToken() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Test User");
        request.setEmail("integration-test@test.com");
        request.setPassword("password123");
        request.setCompanyName("Test Company");

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        authToken = objectMapper.readTree(response)
                .path("data")
                .path("accessToken")
                .asText();
    }

    @Test
    @Order(2)
    void uploadShouldReturn202WithPendingStatus() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-document.txt",
                "text/plain",
                "The quick brown fox jumps over the lazy dog. Java Spring Boot.".getBytes()
        );

        MvcResult result = mockMvc.perform(multipart("/api/v1/documents/upload")
                .file(file)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andReturn();
        String response = result.getResponse().getContentAsString();
        documentId = objectMapper.readTree(response)
                .path("data")
                .path("id")
                .asText();
    }

    @Test
    @Order(3)
    void uploadDuplicateShouldReturn409() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "same-content.txt",
                "text/plain",
                //same bytes as above SHA-256 will match
                "The quick brown fox jumps over the lazy dog. Java Spring Boot.".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/documents/upload")
                .file(file)
                .header("Authorization", "Bearer " + authToken))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.success").value(false))
          .andExpect(jsonPath("$.message").value("This Document already exists"));
    }

    @Test
    @Order(4)
    void getDocumentAfterProcessingShouldBeReady() throws Exception {

        //Give consumer time to process
        Thread.sleep(3000);
        mockMvc.perform(get("/api/v1/documents/" + documentId)
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("READY"));

    }
    @Test
    @Order(5)
    void lexicalSearchShouldReturnChunks() throws Exception {
        mockMvc.perform(get("/api/v1/search/lexical")
                .param("query", "fox")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data",hasSize(greaterThan(0))));


    }

    @Test
    @Order(6)
    void hybridSearchShouldReturnRankedResults() throws Exception {
        mockMvc.perform(get("/api/v1/search/hybrid")
                .param("query", "Java Spring")
                .header("Authorization", "Bearer " + authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].score").isNumber());

    }

    @Test
    @Order(7)
    void getDocument_notFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/v1/documents/00000000-0000-0000-0000-000000000000")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(8)
    void register_withInvalidEmail_shouldReturn400WithFieldErrors() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Test");
        request.setEmail("invalid-email-format");
        request.setPassword("password123");
        request.setCompanyName("Co");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.errors.email").isNotEmpty());
    }

}
