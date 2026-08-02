package com.nayax.usermanager.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados de entrada para a edição de um usuário existente. Sem campo de senha: troca de senha
 * exige endpoint próprio, com confirmação da senha atual.
 *
 * @param name novo nome completo
 * @param email novo e-mail, que deve continuar único na base
 */
@Schema(name = "UpdateUserRequest", description = "Dados para edição de um usuário existente")
public record UpdateUserRequest(

        @Schema(description = "Nome completo do usuário", example = "Carolina Bueno")
        @NotBlank
        @Size(min = 3, max = 100)
        String name,

        @Schema(description = "E-mail do usuário, único na base", example = "carolina.bueno@exemplo.com")
        @NotBlank
        @Email
        @Size(max = 255)
        String email
) {
}
