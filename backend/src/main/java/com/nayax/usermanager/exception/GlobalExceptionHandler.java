package com.nayax.usermanager.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Traduz as exceções da aplicação em respostas {@link ProblemDetail}.
 *
 * <p>Precedência necessária porque o handler embutido do Spring, ativado por
 * {@code spring.mvc.problemdetails.enabled}, também trata falha de validação e sem a lista de
 * campos reprovados.</p>
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata falhas de validação do corpo da requisição.
     *
     * @param ex exceção lançada pelo Bean Validation
     * @return 400 com a lista de campos inválidos na propriedade {@code errors}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        List<FieldError> errors = ex.getFieldErrors().stream()
                .map(error -> new FieldError(error.getField(), error.getDefaultMessage()))
                .toList();

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Dados inválidos na requisição");
        problem.setTitle("Erro de validação");
        problem.setProperty("errors", errors);
        return problem;
    }

    /**
     * Trata a consulta a um recurso inexistente.
     *
     * @param ex exceção de domínio
     * @return 404 com a descrição do recurso não encontrado
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Recurso não encontrado");
        return problem;
    }

    /**
     * Trata a tentativa de cadastrar um e-mail já em uso.
     *
     * @param ex exceção de domínio
     * @return 409 indicando o conflito
     */
    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ProblemDetail handleEmailAlreadyRegistered(EmailAlreadyRegisteredException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Conflito de dados");
        return problem;
    }

    /**
     * Trata as violações de restrição pegas pelo banco.
     *
     * @param ex exceção traduzida pelo Spring Data
     * @return 409 indicando o conflito
     * @implNote Cobre a disputa entre o {@code existsByEmail} e o insert: sem o handler,
     * as requisições como são concorrentes e com o mesmo e-mail iria retornar 500 na segunda.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, "A operação viola uma restrição de integridade dos dados");
        problem.setTitle("Conflito de dados");
        return problem;
    }

    /**
     * Detalhe de um campo reprovado na validação.
     *
     * @param field   nome do campo inválido
     * @param message motivo da reprovação
     */
    public record FieldError(String field, String message) {
    }
}
