package com.nayax.usermanager.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Usuário cadastrado no sistema.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String password;

    protected User() {
    }

    /**
     * Cria um novo usuário.
     *
     * @param name nome completo
     * @param email e-mail na base
     * @param password senha codificada
     */
    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    /**
     * @return identificador gerado pelo banco, ou {@code null} enquanto não persistido
     */
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    /**
     * @return hash da senha
     */
    public String getPassword() {
        return password;
    }

    /**
     * Atualiza os dados editáveis do usuário.
     *
     * @param name novo nome
     * @param email novo e-mail
     */
    public void updateData(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
