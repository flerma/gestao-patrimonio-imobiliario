package com.techup.gestao_patrimonio_imobiliario.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gestaoPatrimonioOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API - Gestão de Patrimônio Imobiliário")
                        .description("API REST para gestão de usuários, imóveis, inquilinos e contratos de locação.")
                        .version("v1")
                        .contact(new Contact().name("TechUp").email("contato@techup.com"))
                        .license(new License().name("Proprietária")));
    }
}
