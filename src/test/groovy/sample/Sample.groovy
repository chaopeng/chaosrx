package sample

import me.chaopeng.chaosrx.HttpService
import me.chaopeng.chaosrx.testbeans.TestHandler

/**
 * sample.Sample
 *
 * @author chao
 * @version 1.0 - 2016-07-05
 */
class Sample {

    public static void main(String[] args) {
        new HttpService(8888, [new TestHandler()])
    }
}
