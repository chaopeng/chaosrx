package me.chaopeng.chaosrx;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

/**
 * AbstractHttpHandler
 *
 * @author chao
 * @version 1.0 - 11/26/14
 */
public abstract class AbstractHttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(AbstractHttpHandler.class);

    /**
     * will do before the method invoke if the method have @Before annotation
     *
     * @see Before
     */
    public Response before(Request request) {
        return null;
    }

    /**
     * will do after the method invoke in finally step if the method have @After annotation
     *
     * @see After
     */
    public Response after(Request request, Response response) {
        return response;
    }

    /**
     * render json for response
     *
     * @param object
     * @return
     */
    protected final Response renderJson(Object object) {
        try {
            return Response.newBuilder()
                    .withHttpResponseStatus(HttpResponseStatus.OK)
                    .withContent(JsonUtils.encode(object))
                    .withMediaType(MediaType.APPLICATION_JSON_TYPE)
                    .build();
        } catch (IOException e) {
            logger.error("render json error", e);
            throw new InternalServerErrorException("render json error");
        }
    }

    protected final <T> T jsonDecode(String s, Class<T> cls) throws IOException {
        return JsonUtils.decode(s, cls);
    }

}
