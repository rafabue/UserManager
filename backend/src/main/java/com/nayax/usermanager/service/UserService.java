package com.nayax.usermanager.service;

import com.nayax.usermanager.controller.dto.CreateUserRequest;
import com.nayax.usermanager.controller.dto.UpdateUserRequest;
import com.nayax.usermanager.domain.User;
import com.nayax.usermanager.exception.EmailAlreadyRegisteredException;
import com.nayax.usermanager.exception.ResourceNotFoundException;
import com.nayax.usermanager.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regra de negócio dos usuários.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       EmailService emailService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Cadastra um novo usuário e notifica por e-mail.
     *
     * @param request dados validados do novo usuário
     * @return usuário persistido, com id atribuído
     * @throws EmailAlreadyRegisteredException se o e-mail estiver em uso
     */
    @Transactional
    public User create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyRegisteredException(request.email());
        }

        User user = userRepository.save(
                new User(request.name(), request.email(), passwordEncoder.encode(request.password())));

        emailService.sendRegistrationNotification(user);
        return user;
    }

    /**
     * Atualiza nome e e-mail de um usuário existente e notifica por e-mail.
     *
     * @param id do usuário
     * @param request novos dados validados
     * @return usuário atualizado
     * @throws ResourceNotFoundException se não existir usuário com o id informado
     * @throws EmailAlreadyRegisteredException se o e-mail já pertencer a outro usuário
     */
    @Transactional
    public User update(Long id, UpdateUserRequest request) {
        User user = findEntity(id);

        if (userRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new EmailAlreadyRegisteredException(request.email());
        }

        user.updateData(request.name(), request.email());
        User updated = userRepository.save(user);

        emailService.sendUpdateNotification(updated);
        return updated;
    }

    /**
     * Lista os usuários de forma paginada, opcionalmente filtrando por parte do nome.
     *
     * @param name termo de busca; quando nulo ou em branco, nenhum filtro e aplicado
     * @param pageable página e ordenação solicitadas
     * @return página de usuários
     */
    @Transactional(readOnly = true)
    public Page<User> list(String name, Pageable pageable) {
        if (name == null || name.isBlank()) {
            return userRepository.findAll(pageable);
        }
        return userRepository.searchByNamePart(buildSearchPattern(name), pageable);
    }

    /**
     * Busca um usuário pelo identificador.
     *
     * @param id identificador do usuário
     * @return usuário encontrado
     * @throws ResourceNotFoundException se não existir usuário com o id informado
     */
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return findEntity(id);
    }

    /**
     * Monta o padrão LIKE da busca por parte do nome, neutralizando os curingas do SQL.
     *
     * @param name termo digitado pelo usuário
     * @return padrão no formato {@code %termo%}, com {@code \}, {@code %} e {@code _} escapados
     */
    private String buildSearchPattern(String name) {
        String term = name.trim()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + term + "%";
    }

    /**
     * Recupera a entidade ou falha.
     *
     * @param id identificador do usuário
     * @return entidade encontrada
     * @throws ResourceNotFoundException se não existir usuário com o id informado
     */
    private User findEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
    }
}
