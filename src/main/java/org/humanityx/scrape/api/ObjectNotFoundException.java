package org.humanityx.scrape.api;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Indicate that an object was not found in the database.
 * @author Arvid Halma
 * @version 10-5-2015 - 15:44
 */
public class ObjectNotFoundException extends WebApplicationException {

    public ObjectNotFoundException() {
        super(Response.Status.NOT_FOUND);
    }

    public ObjectNotFoundException(String message) {
        super(Response.status(Response.Status.NOT_FOUND).
                entity(message).type("text/plain").build());
    }

}