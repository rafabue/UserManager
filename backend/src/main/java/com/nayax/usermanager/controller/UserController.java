package com.nayax.usermanager.controller;

import com.nayax.usermanager.controller.dto.CreateUserRequest;
import com.nayax.usermanager.controller.dto.UpdateUserRequest;
import com.nayax.usermanager.controller.dto.UserResponse;
import com.nayax.usermanager.domain.User;
import com.nayax.usermanager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Endpoints REST de gerenciamento dos usuários, sob autenticação HTTP Basic.
 *
 * <p>A descrição de cada operação está nas anotações OpenAPI, publicadas no Swagger UI.</p>
 */
@Tag(name = "Usuários", description = "Cadastro e consulta de usuários")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Cadastra um novo usuário",
            description = "Persiste o usuário e envia a notificação de cadastro por e-mail. "
                    + "Uma falha no envio desfaz a persistência.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado", content = @Content)
    })
    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody @Valid CreateUserRequest request,
                                               UriComponentsBuilder uriBuilder) {
        User user = userService.create(request);
        URI location = uriBuilder.path("/api/users/{id}").buildAndExpand(user.getId()).toUri();
        return ResponseEntity.created(location).body(UserResponse.from(user));
    }

    @Operation(summary = "Lista usuários",
            description = "Retorna os usuários de forma paginada. O parâmetro name filtra por "
                    + "qualquer parte do nome, sem diferenciar maiúsculas de minúsculas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de usuários"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    @GetMapping
    public Page<UserResponse> list(
            @Parameter(description = "Parte do nome a filtrar", example = "carolina")
            @RequestParam(required = false) String name,
            @PageableDefault(size = 10, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return userService.list(name, pageable).map(UserResponse::from);
    }

    @Operation(summary = "Busca usuário por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id) {
        return UserResponse.from(userService.findById(id));
    }

    @Operation(summary = "Atualiza um usuário",
            description = "Altera nome e e-mail e envia a notificação de atualização por e-mail. "
                    + "A senha não é alterável por este endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado", content = @Content)
    })
    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id,
                               @RequestBody @Valid UpdateUserRequest request) {
        return UserResponse.from(userService.update(id, request));
    }
}
