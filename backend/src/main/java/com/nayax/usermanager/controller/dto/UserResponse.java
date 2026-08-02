package com.nayax.usermanager.controller.dto;

import com.nayax.usermanager.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Representação pública de um usuário. O campo de senha não existe aqui: a garantia de que o
 * hash nunca sai numa resposta é estrutural, não depende de anotação.
 *
 * @param id identificador do usuário
 * @param name nome completo
 * @param email e-mail cadastrado
 */
@Schema(name = "UserResponse", description = "Dados públicos de um usuário")
public record UserResponse(

        @Schema(description = "Identificador do usuário", example = "1")
        Long id,

        @Schema(description = "Nome completo do usuário", example = "Carolina Bueno")
        String name,

        @Schema(description = "E-mail do usuário", example = "carolina.bueno@exemplo.com")
        String email
) {

    /**
     * @param user entidade de origem
     * @return DTO correspondente, sem dado sensível
     */
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
