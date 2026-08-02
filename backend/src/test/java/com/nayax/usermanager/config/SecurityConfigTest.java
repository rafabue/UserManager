package com.nayax.usermanager.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("SecurityConfig - separação entre a cadeia da API e a cadeia web")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void documentacaoOpenApi_deveSerPublica() throws Exception {
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    @ParameterizedTest(name = "{0} {1}")
    @CsvSource({
            "GET, /api/users",
            "POST, /api/users"
    })
    void endpointsDeDados_deveRetornar401_quandoSemCredenciais(String metodo, String caminho) throws Exception {
        mockMvc.perform(request(HttpMethod.valueOf(metodo), caminho)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void endpointsDeDados_deveRetornar401_quandoCredenciaisInvalidas() throws Exception {
        mockMvc.perform(get("/api/users").with(httpBasic("admin", "senhaErrada")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void caminhoNaoPrevisto_deveSerNegado() throws Exception {
        mockMvc.perform(get("/caminho-inexistente")).andExpect(status().isForbidden());
    }
}
