package com.nayax.usermanager.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Config da documentação do OpenAPI exposta pelo Swagger.
 */
@Configuration
public class OpenApiConfig {

    private static final String BASIC_AUTH = "basicAuth";

    /**
     * Descreve a API e registra o esquema de autenticação.
     *
     * @return definição OpenAPI da aplicação
     * @implNote O esquema HTTP Basic registrado aqui é o que habilita o botão Authorize do
     * Swagger UI.
     */
    @Bean
    public OpenAPI userManagerOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Manager API")
                        .description("API REST de gerenciamento de usuários. "
                                + "Todos os endpoints sob /api exigem autenticação HTTP Basic.")
                        .version("1.0.0")
                        .contact(new Contact().name("Rafael Bueno")))
                .components(new Components().addSecuritySchemes(BASIC_AUTH,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("basic")
                                .description("Usuário técnico configurado em APP_ADMIN_USERNAME e "
                                        + "APP_ADMIN_PASSWORD")))
                .addSecurityItem(new SecurityRequirement().addList(BASIC_AUTH));
    }
}
