package ru.vsu.csf.mynotes;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.web.reactive.config.EnableWebFlux;

@EnableWebFlux
@SpringBootApplication
@EnableR2dbcRepositories
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")
@OpenAPIDefinition(
        info = @Info(
                title = "MyNotes API",
                description = "Описание всех эндпоинтов сервиса \"MyNotes\"",
                contact = @Contact(name = "Анастасия Сашина", email = "sashina@cs.vsu.ru"),
                version = "1.0.0"),
        security = @SecurityRequirement(name = "basicAuth"))
public class MyNotesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyNotesApplication.class, args);
    }

}
