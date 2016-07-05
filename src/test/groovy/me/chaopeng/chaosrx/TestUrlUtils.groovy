package me.chaopeng.chaosrx

import spock.lang.Specification

/**
 * me.chaopeng.chaosrx.TestUrlUtils
 *
 * @author chao
 * @version 1.0 - 2016-06-27
 */
class TestUrlUtils extends Specification {

    def "url convert"() {

        expect:
        URL url = new URL(urlStr)
        url.protocol == protocol
        url.userInfo == userInfo
        url.host == host
        url.port == port
        UrlUtils.path(url) == path
        UrlUtils.querys(url).get("q") == q

        where:
        urlStr                                         | protocol | userInfo | host         | port | path   | q
        'http://google.com'                            | 'http'   | null     | 'google.com' | -1   | "/"    | null
        'http://google.com/'                           | 'http'   | null     | 'google.com' | -1   | "/"    | null
        'https://google.com:80/'                       | 'https'  | null     | 'google.com' | 80   | "/"    | null
        'https://google.com/aaa'                       | 'https'  | null     | 'google.com' | -1   | "/aaa" | null
        'https://google.com/aaa/'                      | 'https'  | null     | 'google.com' | -1   | "/aaa" | null
        'https://google.com/?q=chaosrx'                | 'https'  | null     | 'google.com' | -1   | "/"    | ["chaosrx"]
        'https://a:a@google.com/?q=chaosrx'            | 'https'  | "a:a"    | 'google.com' | -1   | "/"    | ["chaosrx"]
        'https://a:a@google.com/?q=chaosrx&q=aaa'      | 'https'  | "a:a"    | 'google.com' | -1   | "/"    | ["chaosrx", "aaa"]
        'https://a:a@google.com/?q=%E5%82%BB%E9%80%BC' | 'https'  | "a:a"    | 'google.com' | -1   | "/"    | ["傻逼"]

    }


}
