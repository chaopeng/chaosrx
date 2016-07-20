package me.chaopeng.chaosrx

import me.chaopeng.chaosrx.testbeans.TestBody
import me.chaopeng.chaosrx.testbeans.TestHandler
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import spock.lang.Specification

/**
 * me.chaopeng.chaosrx.HttpServiceTest
 *
 * @author chao
 * @version 1.0 - 2016-07-05
 */
class HttpServiceTest extends Specification {

    static def JSON = MediaType.parse("application/json; charset=utf-8")

    def setupSpec(){
        def router = new HttpRouter([new TestHandler()])
        def handler = new HttpServiceHandler()
        handler.setHttpRouter(router)
        new HttpService(8888, handler).start()
    }

    def index(){
        def client = new OkHttpClient()

        def request = new okhttp3.Request.Builder()
                .url("http://127.0.0.1:8888/")
                .get()
                .build()

        def response = client.newCall(request).execute()

        expect:
        response.body().string() == "index"
    }

    def f1(){
        def client = new OkHttpClient()

        def request = new okhttp3.Request.Builder()
                .url("http://127.0.0.1:8888/f1")
                .get()
                .build()

        def response = client.newCall(request).execute()

        expect:
        response.body().string() == "f1"
    }

    def f2(){

        TestBody testBody = new TestBody(aa: 1, bb: "bb")
        def body = JsonUtils.encode(testBody)

        def client = new OkHttpClient()

        def request = new okhttp3.Request.Builder()
                .url("http://127.0.0.1:8888/f2/3?a=1")
                .addHeader("b", "2")
                .post(RequestBody.create(JSON, body))
                .build()

        def response = client.newCall(request).execute()

        expect:
        response.body().string() == body
    }

    def f3NullPointerException(){

        def client = new OkHttpClient()

        def request = new okhttp3.Request.Builder()
                .url("http://127.0.0.1:8888/f3/3?a=1")
                .addHeader("b", "2")
                .get()
                .build()

        def response = client.newCall(request).execute()

        expect:
        response.code() == 400
        response.body().string() == "NullPointerException：null"
    }


    def f3IllegalArgumentException(){

        TestBody testBody = new TestBody(aa: 1, bb: "bb")
        def body = JsonUtils.encode(testBody)

        def client = new OkHttpClient()

        def request = new okhttp3.Request.Builder()
                .url("http://127.0.0.1:8888/f3/3?a=1")
                .addHeader("b", "2")
                .post(RequestBody.create(JSON, body))
                .build()

        def response = client.newCall(request).execute()

        expect:
        response.code() == 400
        response.body().string() == "something wrong"
    }

    def f3NotAcceptableException(){

        TestBody testBody = new TestBody(aa: 1, bb: "bb")
        def body = JsonUtils.encode(testBody)

        def client = new OkHttpClient()

        def request = new okhttp3.Request.Builder()
                .url("http://127.0.0.1:8888/f3/3?a=1")
                .addHeader("b", "2")
                .put(RequestBody.create(JSON, body))
                .build()

        def response = client.newCall(request).execute()

        expect:
        response.code() == 406
        response.body().string() == "HTTP 406 Not Acceptable"
    }





}
