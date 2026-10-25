package odk.malicompetences.backend.common.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "BearerAuth";

    @Bean
    public OpenAPI maliCompetencesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MaliCompétences API v1")
                        .version("v1")
                        .description(
                                "API backend de la plateforme nationale MaliCompétences."
                        )
                        .contact(new Contact()
                                .name("Équipe MaliCompétences")))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Jeton JWT"))
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Jeton JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH).addList("bearerAuth"));
    }
}
