package com.zestindia.assignment.productapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "application.security.jwt.secret=VGhpc0lzQVN0cm9uZ0Rldk9ubHlTZWNyZXRLZXlGb3JKV1RUMjAyNg==",
        "application.security.jwt.access-token-expiration-ms=900000",
        "application.security.jwt.refresh-token-expiration-ms=604800000"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousUserCannotViewProducts() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCanViewProducts() throws Exception {
        mockMvc.perform(
                        get("/api/v1/products")
                                .with(user("normal-user").roles("USER"))
                )
                .andExpect(status().isOk());
    }

    @Test
    void userCannotCreateProduct() throws Exception {
        mockMvc.perform(
                        post("/api/v1/products")
                                .with(user("normal-user").roles("USER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "productName": "Keyboard"
                                        }
                                        """)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateProduct() throws Exception {
        mockMvc.perform(
                        post("/api/v1/products")
                                .with(user("aekansh").roles("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "productName": "Keyboard"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("Keyboard"))
                .andExpect(jsonPath("$.createdBy").value("aekansh"));
    }
}