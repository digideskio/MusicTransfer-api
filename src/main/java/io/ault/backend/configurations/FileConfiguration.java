package io.ault.backend.configurations;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileConfiguration {

    @JsonProperty
    private String fileDir;

    public String getFileDir() {
        return fileDir;
    }
    
    public void setFileDir(String fileDir) {
        this.fileDir = fileDir;
    }

}
