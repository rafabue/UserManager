package com.nayax.usermanager.service;

import com.nayax.usermanager.domain.User;

/**
 * Envio de notificações por e-mail decorrentes das operações de cadastro.
 *
 * <p>A implementação sinaliza falha lançando exceção: a propagação é o que dispara o
 * rollback da transação no {@link UserService}.</p>
 */
public interface EmailService {

    /**
     * @param user usuário recém-cadastrado
     */
    void sendRegistrationNotification(User user);

    /**
     * @param user usuário atualizado
     */
    void sendUpdateNotification(User user);
}
