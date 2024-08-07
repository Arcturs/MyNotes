package ru.vsu.csf.mynotes.configuration.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ru.vsu.csf.my-note")
public class ApplicationProperties {

    private int maxFileAmount;
    private int maxFileSizeMb;

}
