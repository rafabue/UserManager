package com.nayax.usermanager.service;

import com.nayax.usermanager.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Implementação de {@link EmailService} que registra o ‘output’.
 */
@Service
public class ConsoleEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(ConsoleEmailService.class);

    @Override
    public void sendRegistrationNotification(User user) {
        log.info("E-mail enviado para {}: cadastro realizado com sucesso.", user.getEmail());
    }

    @Override
    public void sendUpdateNotification(User user) {
        log.info("E-mail enviado para {}: dados atualizados com sucesso.", user.getEmail());
    }
}
