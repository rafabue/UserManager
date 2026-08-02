package com.nayax.usermanager.controller;

import com.nayax.usermanager.config.SecurityConfig;
import com.nayax.usermanager.controller.dto.CreateUserRequest;
import com.nayax.usermanager.controller.dto.UpdateUserRequest;
import com.nayax.usermanager.domain.User;
import com.nayax.usermanager.exception.EmailAlreadyRegisteredException;
import com.nayax.usermanager.exception.ResourceNotFoundException;
import com.nayax.usermanager.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("UserController - contrato HTTP")
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void create_deveRetornar201ComLocation_quandoPayloadValido() throws Exception {
        when(userService.create(any(CreateUserRequest.class)))
                .thenReturn(usuarioComId(1L, "Carolina Bueno", "carolina@exemplo.com"));

        mockMvc.perform(post("/api/users").with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Carolina Bueno","email":"carolina@exemplo.com","password":"senhaSegura1"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "http://localhost/api/users/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Carolina Bueno"));
    }

    @Test
    void create_deveRetornar400_quandoPayloadInvalido() throws Exception {
        mockMvc.perform(post("/api/users").with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":" ","email":"email-invalido","password":"123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(hasItems("name", "email", "password")));
    }

    @Test
    void create_deveRetornar400_quandoSenhaUltrapassaLimiteDoBcrypt() throws Exception {
        String senha = "a".repeat(73);

        mockMvc.perform(post("/api/users").with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Carolina Bueno","email":"carolina@exemplo.com","password":"%s"}
                                """.formatted(senha)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(hasItems("password")));
    }

    @Test
    void create_deveRetornar400_quandoSenhaAcentuadaUltrapassaLimiteEmBytes() throws Exception {
        String senha = "á".repeat(40);

        mockMvc.perform(post("/api/users").with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Carolina Bueno","email":"carolina@exemplo.com","password":"%s"}
                                """.formatted(senha)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[*].field").value(hasItems("password")));
    }

    @Test
    void create_deveResponderProblemDetail_quandoCorpoMalformado() throws Exception {
        mockMvc.perform(post("/api/users").with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Carolina\","))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.timestamp").doesNotExist());
    }

    @Test
    void create_deveRetornar409_quandoEmailJaCadastrado() throws Exception {
        when(userService.create(any(CreateUserRequest.class)))
                .thenThrow(new EmailAlreadyRegisteredException("carolina@exemplo.com"));

        mockMvc.perform(post("/api/users").with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Carolina Bueno","email":"carolina@exemplo.com","password":"senhaSegura1"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("E-mail já cadastrado: carolina@exemplo.com"));
    }

    @Test
    void findById_deveRetornar404_quandoIdInexistente() throws Exception {
        when(userService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Usuário não encontrado: 99"));

        mockMvc.perform(get("/api/users/99").with(admin()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Usuário não encontrado: 99"));
    }

    @Test
    void list_naoDeveExporCampoSenha() throws Exception {
        Page<User> pagina = new PageImpl<>(List.of(usuarioComId(1L, "Carolina Bueno", "carolina@exemplo.com")));
        when(userService.list(isNull(), any(Pageable.class))).thenReturn(pagina);

        MvcResult resultado = mockMvc.perform(get("/api/users").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("carolina@exemplo.com"))
                .andReturn();

        assertThat(resultado.getResponse().getContentAsString())
                .doesNotContain("password").doesNotContain("senha");
    }

    @Test
    void findById_naoDeveExporCampoSenha() throws Exception {
        when(userService.findById(1L)).thenReturn(usuarioComId(1L, "Carolina Bueno", "carolina@exemplo.com"));

        MvcResult resultado = mockMvc.perform(get("/api/users/1").with(admin()))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(resultado.getResponse().getContentAsString())
                .doesNotContain("password").doesNotContain("senha");
    }

    @Test
    void list_deveSerializarPaginacaoNoFormatoEstavel() throws Exception {
        Page<User> pagina = new PageImpl<>(
                List.of(usuarioComId(1L, "Carolina Bueno", "carolina@exemplo.com")), PageRequest.of(0, 10), 1);
        when(userService.list(eq("silva"), any(Pageable.class))).thenReturn(pagina);

        mockMvc.perform(get("/api/users").param("name", "silva").with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Carolina Bueno"))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.pageable").doesNotExist());
    }

    @Test
    void update_deveRetornar200_quandoPayloadValido() throws Exception {
        when(userService.update(eq(1L), any(UpdateUserRequest.class)))
                .thenReturn(usuarioComId(1L, "Carolina Souza", "carolina.souza@exemplo.com"));

        mockMvc.perform(put("/api/users/1").with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Carolina Souza","email":"carolina.souza@exemplo.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Carolina Souza"));
    }

    @Test
    void update_deveRetornar409_quandoEmailPertenceAOutroUsuario() throws Exception {
        when(userService.update(eq(1L), any(UpdateUserRequest.class)))
                .thenThrow(new EmailAlreadyRegisteredException("ana@exemplo.com"));

        mockMvc.perform(put("/api/users/1").with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Carolina Bueno","email":"ana@exemplo.com"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("E-mail já cadastrado: ana@exemplo.com"));
    }

    private static RequestPostProcessor admin() {
        return httpBasic("admin", "admin123");
    }

    private User usuarioComId(Long id, String name, String email) {
        User user = new User(name, email, "$2a$10$hashFake");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
