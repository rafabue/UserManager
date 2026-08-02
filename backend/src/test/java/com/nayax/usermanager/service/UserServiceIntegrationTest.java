package com.nayax.usermanager.service;

import com.nayax.usermanager.controller.dto.CreateUserRequest;
import com.nayax.usermanager.domain.User;
import com.nayax.usermanager.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

// sem transactional na classe de propósito com a transação do teste envolvendo tudo, o rollback do service ficaria invisível
@DisplayName("UserService - efeitos transacionais contra o banco")
@SpringBootTest
@ActiveProfiles("test")
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private EmailService emailService;

    // limpa antes e depois: o H2 em memória é compartilhado entre as classes de teste
    // e a ordem de execução não é garantida
    @BeforeEach
    @AfterEach
    void limparBase() {
        userRepository.deleteAll();
    }

    @Test
    void create_naoDevePersistirUsuario_quandoEnvioDeEmailFalhar() {
        doThrow(new IllegalStateException("servidor de e-mail indisponível"))
                .when(emailService).sendRegistrationNotification(any(User.class));

        assertThatThrownBy(() -> userService.create(
                new CreateUserRequest("Ana Lima", "ana@exemplo.com", "senhaSegura1")))
                .isInstanceOf(IllegalStateException.class);

        assertThat(userRepository.count()).isZero();
        assertThat(userRepository.existsByEmail("ana@exemplo.com")).isFalse();
    }

    @Test
    void create_devePersistirUsuario_quandoEnvioDeEmailForBemSucedido() {
        User criado = userService.create(
                new CreateUserRequest("Carolina Bueno", "carolina@exemplo.com", "senhaSegura1"));

        assertThat(criado.getId()).isNotNull();
        assertThat(userRepository.findById(criado.getId())).isPresent();
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void list_deveTratarPercentualComoTextoLiteral_quandoNomeContemPercentual() {
        userService.create(new CreateUserRequest("Ana Lima", "ana@exemplo.com", "senhaSegura1"));
        userService.create(new CreateUserRequest("Bruno 100% Silva", "bruno@exemplo.com", "senhaSegura1"));

        Page<User> resultado = userService.list("%", PageRequest.of(0, 10));

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().get(0).getName()).isEqualTo("Bruno 100% Silva");
    }
}
