package ar.com.ultrafibra.intranet;

import ar.com.ultrafibra.intranet.config.JksProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties(JksProperties.class)
public class MsvcIntranetWebultrafibraApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsvcIntranetWebultrafibraApplication.class, args);
	}
}
