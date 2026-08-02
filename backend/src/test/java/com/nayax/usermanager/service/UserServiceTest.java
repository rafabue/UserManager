package com.nayax.usermanager.service;

import com.nayax.usermanager.controller.dto.CreateUserRequest;
import com.nayax.usermanager.controller.dto.UpdateUserRequest;
import com.nayax.usermanager.domain.User;
import com.nayax.usermanager.exception.EmailAlreadyRegisteredException;
import com.nayax.usermanager.exception.ResourceNotFoundException;
import com.nayax.usermanager.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("UserService - regras de negócio isoladas")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    void create_devePersistirComSenhaCriptografadaENotificar_quandoEmailDisponivel() {
        CreateUserRequest request = new CreateUserRequest("Carolina Bueno", "carolina@exemplo.com", "senhaSegura1");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("$2a$10$hashFake");
        when(userRepository.save(any(User.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        userService.create(request);

        verify(userRepository).save(userCaptor.capture());
        User salvo = userCaptor.getValue();
        assertThat(salvo.getName()).isEqualTo("Carolina Bueno");
        assertThat(salvo.getEmail()).isEqualTo("carolina@exemplo.com");
        assertThat(salvo.getPassword()).isNotEqualTo("senhaSegura1").isEqualTo("$2a$10$hashFake");

        InOrder ordem = Mockito.inOrder(userRepository, emailService);
        ordem.verify(userRepository).save(any(User.class));
        ordem.verify(emailService).sendRegistrationNotification(any(User.class));
    }

    @Test
    void create_deveLancarEmailJaCadastrado_quandoEmailExistente() {
        CreateUserRequest request = new CreateUserRequest("Joao Souza", "joao@exemplo.com", "senhaSegura1");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(EmailAlreadyRegisteredException.class);

        verify(userRepository, never()).save(any());
        verifyNoInteractions(emailService, passwordEncoder);
    }

    @Test
    void create_devePropagarExcecao_quandoEnvioDeEmailFalhar() {
        CreateUserRequest request = new CreateUserRequest("Ana Lima", "ana@exemplo.com", "senhaSegura1");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashFake");
        when(userRepository.save(any(User.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
        Mockito.doThrow(new IllegalStateException("servidor de e-mail indisponível"))
                .when(emailService).sendRegistrationNotification(any(User.class));

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void update_devePersistirAntesDeNotificar_quandoDadosValidos() {
        User existente = new User("Carolina Bueno", "carolina@exemplo.com", "$2a$10$hashFake");
        UpdateUserRequest request = new UpdateUserRequest("Carolina Souza", "carolina.souza@exemplo.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(userRepository.existsByEmailAndIdNot(request.email(), 1L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        User atualizado = userService.update(1L, request);

        assertThat(atualizado.getName()).isEqualTo("Carolina Souza");
        assertThat(atualizado.getEmail()).isEqualTo("carolina.souza@exemplo.com");
        assertThat(atualizado.getPassword()).isEqualTo("$2a$10$hashFake");
        verifyNoInteractions(passwordEncoder);

        InOrder ordem = Mockito.inOrder(userRepository, emailService);
        ordem.verify(userRepository).save(any(User.class));
        ordem.verify(emailService).sendUpdateNotification(any(User.class));
    }

    @Test
    void update_deveLancarRecursoNaoEncontrado_quandoIdInexistente() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(99L, new UpdateUserRequest("Novo Nome", "novo@exemplo.com")))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    @Test
    void update_deveLancarEmailJaCadastrado_quandoEmailPertenceAOutroUsuario() {
        User existente = new User("Carolina Bueno", "carolina@exemplo.com", "$2a$10$hashFake");
        UpdateUserRequest request = new UpdateUserRequest("Carolina Bueno", "ana@exemplo.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(userRepository.existsByEmailAndIdNot(request.email(), 1L)).thenReturn(true);

        assertThatThrownBy(() -> userService.update(1L, request))
                .isInstanceOf(EmailAlreadyRegisteredException.class);

        assertThat(existente.getEmail()).isEqualTo("carolina@exemplo.com");
        verify(userRepository, never()).save(any());
        verifyNoInteractions(emailService);
    }

    @ParameterizedTest(name = "nome = \"{0}\"")
    @NullSource
    @ValueSource(strings = {"", "   "})
    void list_deveUsarConsultaSemFiltro_quandoNomeNuloOuEmBranco(String nome) {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> pagina = new PageImpl<>(List.of(new User("Ana Lima", "ana@exemplo.com", "hash")));
        when(userRepository.findAll(pageable)).thenReturn(pagina);

        assertThat(userService.list(nome, pageable)).isSameAs(pagina);

        verify(userRepository, never()).searchByNamePart(anyString(), any());
    }

    @Test
    void list_deveFiltrarPorParteDoNome_quandoNomeInformado() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> pagina = new PageImpl<>(List.of(new User("Ana Lima", "ana@exemplo.com", "hash")));
        when(userRepository.searchByNamePart("%li%", pageable)).thenReturn(pagina);

        assertThat(userService.list("li", pageable)).isSameAs(pagina);

        verify(userRepository).searchByNamePart(eq("%li%"), eq(pageable));
        verify(userRepository, never()).findAll(any(Pageable.class));
    }

    @ParameterizedTest(name = "nome = \"{0}\" gera o padrão \"{1}\"")
    @MethodSource("termosComCuringas")
    void list_deveEscaparCuringasDoLike_quandoNomeContemCaractereEspecial(String nome, String padraoEsperado) {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.searchByNamePart(anyString(), eq(pageable))).thenReturn(new PageImpl<>(List.of()));

        userService.list(nome, pageable);

        verify(userRepository).searchByNamePart(eq(padraoEsperado), eq(pageable));
    }

    private static Stream<Arguments> termosComCuringas() {
        return Stream.of(
                Arguments.of("%", "%\\%%"),
                Arguments.of("a_b", "%a\\_b%"),
                Arguments.of("a\\b", "%a\\\\b%"));
    }

    @Test
    void findById_deveRetornarUsuario_quandoIdExistente() {
        User existente = new User("Ana Lima", "ana@exemplo.com", "$2a$10$hashFake");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existente));

        assertThat(userService.findById(1L)).isSameAs(existente);
    }

    @Test
    void findById_deveLancarRecursoNaoEncontrado_quandoIdInexistente() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");
    }
}
