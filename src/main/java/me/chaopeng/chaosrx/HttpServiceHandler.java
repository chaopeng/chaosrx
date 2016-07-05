package me.chaopeng.chaosrx;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import java.lang.reflect.InvocationTargetException;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * HttpServiceHandler
 *
 * @author chao
 * @version 1.0 - 11/25/14
 */
@ChannelHandler.Sharable
public class HttpServiceHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HttpServiceHandler.class);

    private HttpRouter httpRouter;

    public HttpServiceHandler() {
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) {


        try {
            Request request = new Request(ctx, fullHttpRequest, fullHttpRequest);

            if (HttpUtil.is100ContinueExpected(request.getHttpRequest())) {
                send100Continue(ctx);
            }
            writeResponse(ctx, fullHttpRequest, httpRouter.invoke(request));
        } catch (InvocationTargetException e) {
            Throwable e1 = e.getTargetException();
            if (!(e1 instanceof WebApplicationException) && !(e1 instanceof IllegalArgumentException)) {
                logger.error("InvocationTargetException Exception", e1);
            }
            writeResponse(ctx, fullHttpRequest, Response.errorResponse(e1));
        } catch (Exception e) {
            if (!(e instanceof WebApplicationException) && !(e instanceof IllegalArgumentException)) {
                logger.error("Unkown Exception", e);
            }
            writeResponse(ctx, fullHttpRequest, Response.errorResponse(e));
        }
    }


    private void writeResponse(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest, Response res) {
        boolean keepAlive = HttpUtil.isKeepAlive(fullHttpRequest);
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1
                , res.getHttpResponseStatus()
                , Unpooled.copiedBuffer(res.getBody(), CharsetUtil.UTF_8));

        response.headers().set(CONTENT_TYPE, res.getMediaType().toString());
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());

        if (keepAlive) {
            response.headers().set(CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        ctx.writeAndFlush(response);

        if (!keepAlive) {
            ctx.close();
        }
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.write(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Unkown Exception", cause);
        ctx.close();
    }

    public void setHttpRouter(HttpRouter httpRouter) {
        this.httpRouter = httpRouter;
    }
}
