package ar.com.ultrafibra.intranet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;


@Configuration
public class ApplicationConfig {

    // CONFIGURACION PARA EL MANEJO DE OBJETOS Json
    @Bean
    public MappingJackson2HttpMessageConverter jsonHttpMessageConverter() {
        return new MappingJackson2HttpMessageConverter();
    }

    

}
