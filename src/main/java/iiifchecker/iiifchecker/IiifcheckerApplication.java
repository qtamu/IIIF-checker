package iiifchecker.iiifchecker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class IiifcheckerApplication {

	public static void main(String[] args) {
		SpringApplication.run(IiifcheckerApplication.class, args);
	}

}
