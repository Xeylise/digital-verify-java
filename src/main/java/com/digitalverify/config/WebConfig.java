package com.digitalverify.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuracion de recursos estaticos web para la aplicacion Digital Verify.
 * Mapea las rutas publicas de CSS, JavaScript, imagenes y la carpeta local
 * de carga de documentos KYC para permitir su visualizacion en el navegador.
 *
 * @author Paola Andrea Castro Sarabia
 * @version 1.0.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Directorio fisico local donde se almacenan los documentos subidos por los usuarios.
     */
    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    /**
     * Registra los controladores de recursos estaticos para servir contenido web y documentos locales.
     *
     * @param registry Registro de manejadores de recursos de Spring MVC.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Recursos estaticos empaquetados en classpath (estilos, scripts e imagenes)
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/img/**")
                .addResourceLocations("classpath:/static/img/");

        // Manejador para servir documentos locales subidos (KYC) asegurando ruta absoluta normalizada
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath.toString() + "/");
    }
}
