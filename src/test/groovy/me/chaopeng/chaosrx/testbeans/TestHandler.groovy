package me.chaopeng.chaosrx.testbeans

import me.chaopeng.chaosrx.AbstractHttpHandler
import me.chaopeng.chaosrx.After
import me.chaopeng.chaosrx.Before
import me.chaopeng.chaosrx.HttpBody
import me.chaopeng.chaosrx.Request
import me.chaopeng.chaosrx.Response

import javax.ws.rs.*

/**
 * me.chaopeng.chaosrx.testbeans.TestHandler
 *
 * @author chao
 * @version 1.0 - 2016-07-04
 */
@Path("/")
class TestHandler extends AbstractHttpHandler {

    @Path("/")
    @GET
    Response index() throws Exception {
        return Response.newBuilder().withContent("index").build()
    }

    @Path("/f1")
    Response f1() {
        return Response.newBuilder().withContent("f1").build()
    }

    @Path("/f2/{c}")
    @POST
    Response f2(Request request,
                @QueryParam("a") String a,
                @HeaderParam("b") String b, @PathParam("c") String c, @HttpBody String body) {
        def testbody = jsonDecode(body, TestBody)
        return renderJson(testbody)
    }

    @Path("/f3/{c}")
    Response f3(Request request,
                @QueryParam("a") List<String> a,
                @HeaderParam("b") String b, @PathParam("c") String c, @HttpBody String body) {
        switch (request.method) {
            case io.netty.handler.codec.http.HttpMethod.GET:
                return Response.errorResponse(new NullPointerException())
            case io.netty.handler.codec.http.HttpMethod.POST:
                return Response.errorResponse(new IllegalArgumentException("something wrong"))
            default: // 3
                return Response.errorResponse(new NotAcceptableException())
        }
    }

    @Override
    Response before(Request request) {
        if (request.method == io.netty.handler.codec.http.HttpMethod.GET) {
            return Response.errorResponse(new IllegalArgumentException("get: something wrong"))
        }
        return null
    }

    @Override
    Response after(Request request, Response response) {
        if (request.method == io.netty.handler.codec.http.HttpMethod.POST) {
            return Response.errorResponse(new IllegalArgumentException("post: something wrong"))
        }
        return response
    }

    @Path("/f4/{c}")
    @Before
    @After
    Response f4(Request request,
                @QueryParam("a") int a, @HeaderParam("b") int b, @PathParam("c") int c, @HttpBody TestBody body) {
        return Response.newBuilder().withContent("f4").build()
    }
}