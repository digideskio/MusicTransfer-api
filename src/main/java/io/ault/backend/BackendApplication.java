package io.ault.backend;

import io.ault.backend.configurations.FileConfiguration;
import io.ault.backend.db.FileDAO;
import io.ault.backend.resources.CheckResource;
import io.ault.backend.resources.FileResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.io.IOException;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

import org.eclipse.jetty.servlets.CrossOriginFilter;

public class BackendApplication extends Application<BackendConfiguration> {

    public static void main(String[] args) throws Exception {
        new BackendApplication().run(args);
    }

    @Override
    public String getName() {
        return "MusicTransfer";
    }

    @Override
    public void initialize(Bootstrap<BackendConfiguration> bootstrap) {
    }

    @Override
    public void run(BackendConfiguration configuration, Environment environment) throws ClassNotFoundException, IOException {

        FileConfiguration fileConfiguration = configuration.getFiles();


        // File upload & download
        FileDAO fileDAO = new FileDAO(fileConfiguration);
        environment.jersey().register(new FileResource(fileDAO));
        environment.jersey().register(new CheckResource());

        /*
         * CORS Filter
         */
        FilterRegistration.Dynamic filter = environment.servlets().addFilter("CORS", CrossOriginFilter.class);
        filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
        filter.setInitParameter("allowedOrigins", "*");
        filter.setInitParameter("allowedHeaders", "Authorization,Content-Type,X-Api-Key,Accept,Origin");
        filter.setInitParameter("allowedMethods", "GET,POST,PUT,DELETE,OPTIONS");
        filter.setInitParameter("preflightMaxAge", "5184000"); // 2 months
        filter.setInitParameter("allowCredentials", "true");
    }

}
