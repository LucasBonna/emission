package br.com.contafacil.bonnarotec.emission;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableFeignClients
@EntityScan({
		"br.com.contafacil.shared.bonnarotec.toolslib.domain",
		"br.com.contafacil.bonnarotec.emission"
})
@EnableJpaRepositories({
		"br.com.contafacil.shared.bonnarotec.toolslib.domain",
		"br.com.contafacil.bonnarotec.emission"
})
@EnableJpaAuditing
public class EmissionApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmissionApplication.class, args);
	}

}
