package me.chaopeng.chaosrx

import me.chaopeng.chaosrx.testbeans.TestBody
import me.chaopeng.chaosrx.testbeans.TestHandler
import spock.lang.Specification

import java.lang.reflect.Method

/**
 * me.chaopeng.chaosrx.ParamsHelperTest
 *
 * @author chao
 * @version 1.0 - 2016-07-04
 */
class ParamsHelperTest extends Specification {


    def "get params"() {
        Method f1 = TestHandler.methods.find { it.name == "f1" }
        Method f2 = TestHandler.methods.find { it.name == "f2" }
        Method f3 = TestHandler.methods.find { it.name == "f3" }
        Method f4 = TestHandler.methods.find { it.name == "f4" }

        TestBody testBody = new TestBody(aa: 1, bb: "bb")

        setup:
        Request request = Mock()

        request.getQueryParameters(_) >> ["1", "1"]
        request.getQueryParameter(_) >> "1"
        request.getHeaderParameter(_) >> "2"
        request.getPathParameter(_) >> "3"
        request.getHttpBody() >> JsonUtils.encode(testBody).bytes
        request.getUTF8Body() >> JsonUtils.encode(testBody)

        expect:
        HttpRouter.ParamsHelper.getParams(f1, request).toList() == []
        HttpRouter.ParamsHelper.getParams(f2, request).toList() == [request, "1", "2", "3", JsonUtils.encode(testBody)]
        HttpRouter.ParamsHelper.getParams(f3, request).toList() == [request, ["1", "1"], "2", "3", JsonUtils.encode(testBody)]
        HttpRouter.ParamsHelper.getParams(f4, request).toList() == [request, 1, 2, 3, testBody]

    }

    def "convert"() {

        expect:
        HttpRouter.ParamsHelper.convert(str, type) == target

        where:

        str | type          | target
        "1" | String.class  | "1"
        "1" | int.class     | 1
        "1" | Integer.class | 1
        "1" | Object.class  | "1"
        "1" | Request.class | null
    }
}
