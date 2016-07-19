package me.chaopeng.chaosrx;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Request
 *
 * @author chao
 * @version 1.0 - 11/26/14
 */
public class Request {

    private final ChannelHandlerContext channelHandlerContext;
    private final HttpRequest httpRequest;
    private final ByteBuf httpContent;
    private final String path;
    private final Map<String, List<String>> queryParameters;
    private final Map<String, String> pathParameters = new HashMap<>();
    private final Map<String, String> headerParameters = new HashMap<>();

    public Request(ChannelHandlerContext channelHandlerContext, HttpRequest httpRequest, HttpContent httpContent) throws MalformedURLException, UnsupportedEncodingException {
        this.channelHandlerContext = channelHandlerContext;
        this.httpRequest = httpRequest;
        this.httpContent = httpContent == null ? null : httpContent.content();

        String[] ss = httpRequest.uri().split("\\?");
        
        path = UrlUtils.path(ss[0]);

        queryParameters = UrlUtils.querys(ss[1]);

        httpRequest.headers().forEach(entry -> headerParameters.put(entry.getKey(), entry.getValue()));
    }

    public String getIp() {
        // for nginx proxy
        if (httpRequest.headers().contains("X-Real-IP")) {
            return httpRequest.headers().get("X-Real-IP");
        }
        // for use it direct
        return ((InetSocketAddress) channelHandlerContext.channel().remoteAddress()).getAddress().getHostAddress();
    }

    public HttpMethod getMethod() {
        return httpRequest.method();
    }

    public String getPath() {
        return path;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    /**
     *
     * @return
     */
    public byte[] getHttpBody() {
        return httpContent != null ? httpContent.copy().array() : new byte[0];
    }

    public String getUTF8Body() {
        return httpContent != null ? httpContent.toString(Charsets.UTF_8) : "";
    }

    public Map<String, List<String>> getQueryParameters() {
        return queryParameters;
    }

    public Map<String, String> getPathParameters() {
        return pathParameters;
    }

    public Map<String, String> getHeaderParameters() {
        return headerParameters;
    }

    public List<String> getQueryParameters(String name) {
        return queryParameters.get(name);
    }

    public String getQueryParameter(String name) {
        List<String> param = queryParameters.get(name);
        return param == null || param.isEmpty() ? null : param.get(0);
    }

    public String getPathParameter(String name) {
        return pathParameters.get(name);
    }

    public String getHeaderParameter(String name) {
        return headerParameters.get(name);
    }
}
