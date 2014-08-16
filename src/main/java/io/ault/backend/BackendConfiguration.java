package io.ault.backend;

import io.ault.backend.configurations.FileConfiguration;

import javax.validation.Valid;

import io.dropwizard.Configuration;
import io.dropwizard.server.SimpleServerFactory;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Links the yaml configuration to the framework.
 */
public class BackendConfiguration extends Configuration {

    @Valid
    @JsonProperty
    private final FileConfiguration files = new FileConfiguration();

    public BackendConfiguration() {
        setServerFactory(new SimpleServerFactory());
    }

    public FileConfiguration getFiles() {
        return files;
    }
}
