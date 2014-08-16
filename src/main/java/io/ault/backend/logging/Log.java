package io.ault.backend.logging;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.api.core.HttpContext;

public class Log {

    public static final Logger resourceLogger =
            Logger.getLogger(Log.class.getCanonicalName());

    public static void log(HttpContext c, String message) {
        // Log the HTTP Method - URL - ip address/referrer - app id - user id - body
        String httpMethod = c.getRequest().getMethod();
        String requestURL = c.getRequest().getRequestUri().toString();
        String body = c.getRequest().getEntity(String.class);
        String messageString = message == null ? "" : message;
        resourceLogger.info(String.format("%s - %s - %s - %s", httpMethod, requestURL, body, messageString));
    }

    public static void log(HttpContext c) {
        log(c, "");
    }

    public static void logAndThrow(Response response) {
        throw loggedException(response);
    }

    public static WebApplicationException loggedException(Response response) {
        return loggedException(null, response);
    }

    public static WebApplicationException loggedException(Throwable cause, Response response) {
        if (response == null) {
            resourceLogger.error(500 + " - ");
            throw new WebApplicationException(cause, Response.serverError().build());
        }
        int status = response.getStatus();
        JSONObject jsonError = null;
        Object entity = response.getEntity();
        String message = null;
        if (entity != null) {
            // try to turn the response into a JSON object
            message = entity.toString();
            try {
                JSONObject givenEntity = new JSONObject(message);
                if (givenEntity.has("errors")) {
                    // it is already in the right format
                    jsonError = givenEntity;
                }
            } catch (JSONException ignored) {
            }
        }
        if (jsonError == null) {
            // otherwise lets make the error in the correct format
            jsonError = exceptionToJSONError(cause, status, message);
        }
        return new WebApplicationException(cause, responseForJSONError(cause, status, jsonError));
    }

    private static Response responseForJSONError(Throwable cause, int status, JSONObject jsonError) {
        String string = jsonError.toString();
        if (cause != null) {
            resourceLogger.error(status + " - " + string + " - " + cause, cause);
        }
        else {
            resourceLogger.error(status + " - " + string);
        }
        return Response.status(status).entity(string).build();
    }

    private static JSONObject exceptionToJSONError(Throwable cause, int status, String message) {
        try {
            JSONObject jsonError = new JSONObject();
            JSONArray errorsArray = new JSONArray();
            JSONObject errorObject = new JSONObject();
            errorObject.put("code", status);
            if (message == null && cause != null) {
                message = cause.getMessage();
            }
            errorObject.put("message", message);
            errorsArray.put(errorObject);
            jsonError.put("errors", errorsArray);
            return jsonError;
        } catch (JSONException e) {
            throw new WebApplicationException(cause, Response.serverError()
                    .entity("Could not make errors into JSON format")
                    .build());
        }
    }

}
