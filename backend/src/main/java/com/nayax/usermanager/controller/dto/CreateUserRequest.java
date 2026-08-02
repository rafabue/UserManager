package com.nayax.usermanager.controller.dto;

import com.nayax.usermanager.validation.MaxBytes;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados de entrada para o cadastro de um novo usuário.
 *
 * @param name nome completo, entre 3 e 100 caracteres
 * @param email e-mail válido e ainda não cadastrado
 * @param password senha em texto puro, com no mínimo 8 caracteres e no máximo 72 bytes UTF-8,
 * limite imposto pelo BCrypt; é codificada pelo service antes, e sem retorno na resposta.
 */
@Schema(name = "CreateUserRequest", description = "Dados para cadastro de um novo usuário")
public record CreateUserRequest(

        @Schema(description = "Nome completo do usuário", example = "Carolina Bueno")
        @NotBlank
        @Size(min = 3, max = 100)
        String name,

        @Schema(description = "E-mail do usuário, único na base", example = "carolina.bueno@exemplo.com")
        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @Schema(description = "Senha de no mínimo 8 caracteres e no máximo 72 bytes UTF-8",
                example = "senhaSegura123")
        @NotBlank
        @Size(min = 8)
        @MaxBytes(value = 72, message = "não pode ultrapassar 72 bytes")
        String password
) {
}
