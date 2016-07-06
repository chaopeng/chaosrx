# Chaosrx

Chaosrx is a webservices framework base on Netty and JAX-RS. It is fast and simple. 

## how to get it

```maven
<dependency>
  <groupId>me.chaopeng.chaosrx</groupId>
  <artifactId>chaosrx</artifactId>
  <version>1.0.0</version>
</dependency>
```

```gradle
'me.chaopeng.chaosrx:chaosrx:1.0.0'
```

## Example

### handler

```java
@Path("/")
public class Handler extends AbstractHttpHandler {

    @Path("/")
    Response index() {
        return Response.newBuilder().withContent("index").build();
    }

    @Path("/f2/{c}")
    @POST
    Response f2(Request request, @QueryParam("a") String a, @HeaderParam("b") String b,  @PathParam("c") String c, @HttpBody String body){
        TestBody testbody = jsonDecode(body, TestBody.class);
        return renderJson(testbody);
    }
}
```

### start a server

```java
HttpRouter router = new HttpRouter(Lists.newArrayList(
	new Handler()
));
HttpServiceHandler serviceHandler = new HttpServiceHandler();
serviceHandler.setHttpRouter(router);
new HttpService(8888, serviceHandler).start();
```

you can read test for more example.
