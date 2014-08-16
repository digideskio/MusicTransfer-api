package io.ault.backend.resources;

import io.ault.backend.core.Tag;
import io.ault.backend.db.FileDAO;
import io.ault.backend.logging.Log;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.id3.ID3v24Tag;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.jersey.core.header.ContentDisposition;
import com.sun.jersey.multipart.FormDataMultiPart;

@Path("/files")
public class FileResource {

    private final FileDAO dao;

    public FileResource(FileDAO dao) {
        this.dao = dao;
    }

    @POST
    @Timed
    @ExceptionMetered
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public String uploadFile(FormDataMultiPart formData) {
        InputStream uploadedInputStream = formData.getField("file")
                .getEntityAs(InputStream.class);
        ContentDisposition fileDetail = formData.getField("file")
                .getContentDisposition();

        String fileName = dao.create(uploadedInputStream, fileDetail);

        // Build the JSON response object.
        JsonNodeFactory factory = JsonNodeFactory.instance;
        ObjectNode entity = new ObjectNode(factory);
        entity.put("fileName", fileName);

        return entity.toString();
    }

    @POST
    @Path("/tags")
    @Timed
    @ExceptionMetered
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String uploadFile(@Valid Tag tag) throws Exception {
        File file = new File(dao.dir + "/" + tag.getFileName());
        if (!file.exists()) {
            Log.logAndThrow(Response.status(Response.Status.NOT_FOUND)
                    .entity("File with name " + tag.getFileName() + " does not exist")
                    .build());
        }
        MP3File mp3File = (MP3File) AudioFileIO.read(file);
        ID3v24Tag id3Tag = new ID3v24Tag();
        id3Tag.setField(FieldKey.ARTIST, tag.getArtist());
        id3Tag.setField(FieldKey.TITLE, tag.getTitle());
        id3Tag.setField(FieldKey.ALBUM, tag.getAlbum());
        id3Tag.setField(FieldKey.TRACK_TOTAL, tag.getTrackCount());
        id3Tag.setField(FieldKey.TRACK, tag.getTrackNumber());
        id3Tag.setField(FieldKey.GENRE, tag.getGenre());
        id3Tag.setField(FieldKey.RATING, tag.getRating());
        mp3File.setID3v2TagOnly(id3Tag);
        mp3File.commit();
        //update the id3 tags
        return "{}";
    }

    @GET
    @Timed
    @ExceptionMetered
    @Path("/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response retrieveFile(@PathParam("fileName") String fileName) {

        final InputStream objectContent = dao.retrieve(fileName);

        // Streams the temp file back to the client
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream out) throws IOException {
                try {
                    // Write the contents to a temp file
                    // TODO Better implementation will use memory for small
                    // files
                    DataInputStream in = new DataInputStream(objectContent);
                    byte[] bytes = new byte[4096];
                    int bytesRead = in.read(bytes);
                    while (bytesRead > 0) {
                        out.write(bytes, 0, bytesRead);
                        bytesRead = in.read(bytes);
                    }
                    in.close();
                    out.close();
                } catch (IOException ioe) {
                    throw ioe;
                }
            }
        };
        return Response.ok(stream).build();
    }

    @DELETE
    @Timed
    @ExceptionMetered
    @Path("/{fileName}")
    @Produces(MediaType.APPLICATION_JSON)
    public String delete(@PathParam("fileName") String fileName) {
        dao.delete(fileName);
        return "{}";
    }

    @OPTIONS
    @Timed
    @ExceptionMetered
    public void optionsFiles() {

    }

    @OPTIONS
    @Path("/{fileName}")
    @Timed
    @ExceptionMetered
    public void optionsWithFileName() {

    }
}
