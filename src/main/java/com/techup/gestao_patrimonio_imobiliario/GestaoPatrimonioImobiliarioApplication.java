package com.techup.gestao_patrimonio_imobiliario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class GestaoPatrimonioImobiliarioApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestaoPatrimonioImobiliarioApplication.class, args);
	}

}
