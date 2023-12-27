package ru.vsu.csf.mynotes.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.vsu.csf.mynotes.exception.*;
import ru.vsu.csf.mynotes.model.dto.ErrorResponse;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlerController {

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class, InternalErrorException.class})
    public Mono<ErrorResponse> internalServerErrorHandler(Exception e) {
        log.error(e.getMessage(), e);
        return Mono.just(new ErrorResponse(e.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    public Mono<ErrorResponse> badRequestExceptionHandler(Exception e) {
        return Mono.just(new ErrorResponse(e.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Mono<ErrorResponse> methodArgumentNotValidExceptionHandler(BindException e) {
        return Mono.just(new ErrorResponse(
                e.getBindingResult().getAllErrors().stream()
                        .map(error -> (FieldError) error)
                        .collect(Collectors.toMap(
                                FieldError::getField,
                                DefaultMessageSourceResolvable::getDefaultMessage,
                                (message1, message2) -> message1 + ", " + message2
                        ))
                        .toString()
        ));
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({UnauthorizedException.class, WebClientResponseException.Unauthorized.class})
    public Mono<ErrorResponse> unauthorizedExceptionHandler(Exception e) {
        log.error(e.getMessage());
        return Mono.just(new ErrorResponse(e.getMessage()));
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({WebClientResponseException.Forbidden.class, ForbiddenException.class})
    public Mono<ErrorResponse> forbiddenExceptionHandler(Exception e) {
        return Mono.just(new ErrorResponse(e.getMessage()));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public Mono<ErrorResponse> notFoundExceptionHandler(Exception e) {
        return Mono.just(new ErrorResponse(e.getMessage()));
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConflictException.class)
    public Mono<ErrorResponse> conflictExceptionHandler(Exception e) {
        return Mono.just(new ErrorResponse(e.getMessage()));
    }
}
