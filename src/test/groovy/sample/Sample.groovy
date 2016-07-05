package sample

import me.chaopeng.chaosrx.HttpRouter
import me.chaopeng.chaosrx.HttpService
import me.chaopeng.chaosrx.HttpServiceHandler
import me.chaopeng.chaosrx.testbeans.TestHandler

/**
 * sample.Sample
 *
 * @author chao
 * @version 1.0 - 2016-07-05
 */
class Sample {

    public static void main(String[] args) {
        HttpServiceHandler serviceHandler = new HttpServiceHandler()
        serviceHandler.setHttpRouter(new HttpRouter([new TestHandler()]))
        new HttpService(8888, serviceHandler).start()
    }
}
