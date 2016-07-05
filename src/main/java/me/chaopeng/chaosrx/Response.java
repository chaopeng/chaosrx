package me.chaopeng.chaosrx;

import io.netty.handler.codec.http.HttpResponseStatus;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;

/**
 * Response
 *
 * @author chao
 * @version 1.0 - 11/26/14
 */
public class Response {

    private HttpResponseStatus httpResponseStatus; // default OK
    private String body; // default ""
    private MediaType mediaType; // default TEXT_PLAIN_TYPE

    private Response() {
    }

    private Response(Builder builder) {
        httpResponseStatus = builder.httpResponseStatus;
        body = builder.content;
        mediaType = builder.mediaType;
    }

    public static Response errorResponse(Throwable ex) {
        if (ex instanceof WebApplicationException) {
            WebApplicationException e = (WebApplicationException) ex;
            return errorResponse(HttpResponseStatus.valueOf(e.getResponse().getStatus()), ex);
        }
        return errorResponse(BAD_REQUEST, ex);
    }

    public static Response errorResponse(HttpResponseStatus httpResponseStatus, Throwable ex) {
        Response response = new Response();
        response.mediaType = TEXT_PLAIN_TYPE;

        if (ex instanceof WebApplicationException) {
            WebApplicationException e = (WebApplicationException) ex;
            response.body = e.getMessage();
        } else if (ex instanceof IllegalArgumentException) {
            response.body = ex.getMessage();
        } else {
            response.body = ex.getClass().getSimpleName() + "：" + ex.getMessage();
        }
        response.httpResponseStatus = httpResponseStatus;

        return response;
    }

    public HttpResponseStatus getHttpResponseStatus() {
        return httpResponseStatus;
    }

    public String getBody() {
        return body;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    public static final class Builder {
        private HttpResponseStatus httpResponseStatus = OK;
        private String content = "";
        private MediaType mediaType = TEXT_PLAIN_TYPE;

        private Builder() {
        }

        public Builder withHttpResponseStatus(HttpResponseStatus val) {
            httpResponseStatus = val;
            return this;
        }

        public Builder withContent(String val) {
            content = val;
            return this;
        }

        public Builder withMediaType(MediaType val) {
            mediaType = val;
            return this;
        }

        public Response build() {
            return new Response(this);
        }
    }
}
