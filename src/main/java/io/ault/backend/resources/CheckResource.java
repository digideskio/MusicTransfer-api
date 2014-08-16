package io.ault.backend.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Path("/check")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CheckResource {

    public CheckResource() {
    }

    @GET
    @Timed
    @ExceptionMetered
    public ObjectNode status() {
        ObjectNode json = new ObjectNode(JsonNodeFactory.instance);
        json.put("status", "ok");
        json.put("service", "MusicTransfer");
        return json;
    }

    @OPTIONS
    @Timed
    @ExceptionMetered
    public void options() {

    }
}
