package me.chaopeng.chaosrx

import com.google.common.collect.Lists
import io.netty.handler.codec.http.HttpMethod
import me.chaopeng.chaosrx.testbeans.TestBody
import me.chaopeng.chaosrx.testbeans.TestHandler
import spock.lang.Specification

import javax.ws.rs.ForbiddenException

import static io.netty.handler.codec.http.HttpResponseStatus.*
import static javax.ws.rs.core.MediaType.*

/**
 * me.chaopeng.chaosrx.HttpRouterTest
 *
 * @author chao
 * @version 1.0 - 2016-07-04
 */
class HttpRouterTest extends Specification {

    HttpRouter router
    Request request
    String testBodyStr

    def setup(){
        router = new HttpRouter([new TestHandler()])

        TestBody testBody = new TestBody(aa: 1, bb: "bb")

        testBodyStr = JsonUtils.encode(testBody)

        Map<String, String> pathParams = new HashMap<>()

        request = Mock()
        request.getQueryParameters(_) >> ["1", "1"]
        request.getQueryParameter(_) >> "1"
        request.getHeaderParameter(_) >> "2"
        request.getPathParameters() >> pathParams
        request.getPathParameter(_) >> "3"
        request.getHttpBody() >> testBodyStr.bytes
        request.getUTF8Body() >> testBodyStr

    }

    def "invoke index"() {

        request.getPath() >> "/"
        request.getMethod() >> HttpMethod.GET

        expect:
        router.invoke(request).body == "index"
    }

    def "invoke index: not found"(){

        request.getPath() >> "/"
        request.getMethod() >> HttpMethod.POST

        when:
        router.invoke(request)

        then:
        thrown(ForbiddenException.class)
    }

    def "invoke f1::Get"(){
        request.getPath() >> "/f1"
        request.getMethod() >> HttpMethod.GET

        expect:
        router.invoke(request).body == "f1"
    }

    def "invoke f1::Post"(){
        request.getPath() >> "/f1"
        request.getMethod() >> HttpMethod.POST

        expect:
        router.invoke(request).body == "f1"
    }

    def "invoke f2::Post"(){
        request.getPath() >> "/f2/3"
        request.getMethod() >> HttpMethod.POST

        expect:
        def response = router.invoke(request)
        response.body == testBodyStr
        response.httpResponseStatus == OK
        response.mediaType == APPLICATION_JSON_TYPE
    }

    def "invoke f3:NullPointerException"(){
        request.getPath() >> "/f3/1"
        request.getMethod() >> HttpMethod.GET

        expect:
        def response = router.invoke(request)
        response.body == "NullPointerException：null"
        response.httpResponseStatus == BAD_REQUEST
        response.mediaType == TEXT_PLAIN_TYPE
    }

    def "invoke f3:IllegalArgumentException"(){
        request.getPath() >> "/f3/2"
        request.getMethod() >> HttpMethod.POST

        expect:
        def response = router.invoke(request)
        response.body == "something wrong"
        response.httpResponseStatus == BAD_REQUEST
    }

    def "invoke f3:NotAcceptableException"(){
        request.getPath() >> "/f3/3"
        request.getMethod() >> HttpMethod.PUT

        expect:
        def response = router.invoke(request)
        response.body == "HTTP 406 Not Acceptable"
        response.httpResponseStatus == NOT_ACCEPTABLE
    }

    def "invoke f4"(){
        request.getPath() >> "/f4/1"
        request.getMethod() >> HttpMethod.PUT

        expect:
        def response = router.invoke(request)
        response.body == "f4"
    }

    def "invoke f4:before"(){
        request.getPath() >> "/f4/1"
        request.getMethod() >> HttpMethod.GET

        expect:
        def response = router.invoke(request)
        response.body == "get: something wrong"
    }

    def "invoke f4:after"(){
        request.getPath() >> "/f4/1"
        request.getMethod() >> HttpMethod.POST

        expect:
        def response = router.invoke(request)
        response.body == "post: something wrong"
    }
}


