package com.nayax.usermanager.repository;

import com.nayax.usermanager.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Acesso à persistência de {@link User}.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * @param email a verificar
     * @return {@code true} se o e-mail já estiver em uso
     */
    boolean existsByEmail(String email);

    /**
     * Verificação usada na edição, para que o usuário possa manter o próprio e-mail.
     *
     * @param email a verificar
     * @param id do usuário que está sendo editado
     * @return {@code true} se o e-mail já pertencer a outro usuário
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Busca paginada por qualquer parte do nome, sem diferenciar maiúsculas de minúsculas.
     *
     * @param pattern padrão LIKE completo, com curingas já neutralizados pelo
     * {@code UserService.buildSearchPattern}
     * @param pageable página e ordenação
     * @return página de usuários cujo nome contém o termo
     */
    @Query("select u from User u where upper(u.name) like upper(:pattern) escape '\\'")
    Page<User> searchByNamePart(@Param("pattern") String pattern, Pageable pageable);
}
