package odk.malicompetences.backend.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI maliCompetencesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MaliCompétences API")
                        .version("V1")
                        .description(
                                "API backend de la plateforme nationale MaliCompétences."
                        )
                        .contact(new Contact()
                                .name("Équipe MaliCompétences")));
    }
}