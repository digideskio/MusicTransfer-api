package io.ault.backend.db;

import io.ault.backend.configurations.FileConfiguration;
import io.ault.backend.logging.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.Response;

import com.sun.jersey.core.header.ContentDisposition;

public class FileDAO {

    public String dir = null;

    public FileDAO(FileConfiguration config) {
        this.dir = config.getFileDir();
    }
    
    public String create(InputStream uploadedInputStream, ContentDisposition fileDetail) {
        try {

            File fakeBucket = new File(dir);
            fakeBucket.mkdirs();

            File fakeFile = new File(fakeBucket.getAbsolutePath() + "/" + fileDetail.getFileName());

            DataOutputStream out = new DataOutputStream(new FileOutputStream(fakeFile));
            byte[] bytes = new byte[4096];
            int bytesRead = uploadedInputStream.read(bytes);
            while (bytesRead > 0) {
                out.write(bytes, 0, bytesRead);
                bytesRead = uploadedInputStream.read(bytes);
            }
            out.close();

        } catch (IOException e) {
            Log.logAndThrow(Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error on file storage backend: " + e.getMessage())
                    .build());
        }
        return fileDetail.getFileName();
    }

    public InputStream retrieve(String fileName) {

        File fakeBucket = new File(dir);
        fakeBucket.mkdirs();
        
        File fakeFile = new File(fakeBucket.getAbsolutePath() + "/" + fileName);

        if (!fakeFile.exists()) {
            Log.logAndThrow(Response.status(Response.Status.NOT_FOUND)
                    .entity("File with name " + fileName + " does not exist.")
                    .build());
        }

        try {
            return new FileInputStream(fakeFile);
        } catch (IOException e) {
            Log.logAndThrow(Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error on file retrieval file with name "
                            + fileName + ".").build());
        }
        return null;
    }

    public void delete(String fileName) {
        File fakeBucket = new File(dir);
        File fakeFile = new File(fakeBucket.getAbsolutePath() + "/" + fileName);

        fakeFile.delete();
    }
}
