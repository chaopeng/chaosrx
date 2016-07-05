package me.chaopeng.chaosrx;

import com.google.common.base.Charsets;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * UrlUtils
 *
 * @author chao
 * @version 1.0 - 12/29/14
 */
class UrlUtils {

    private static Pattern MULTI_SLASH = Pattern.compile("/+");

    public static String path(URL url) {
        return path(url.getPath());
    }

    public static String path(String path) {
        path = MULTI_SLASH.matcher(path).replaceAll("/");
        path = path.toLowerCase();

        if (path.isEmpty()) {
            path = "/";
        }

        return path.length() > 1 && path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    public static Map<String, List<String>> querys(URL url) throws UnsupportedEncodingException {
        return querys(url.getQuery());
    }

    public static Map<String, List<String>> querys(String query) throws UnsupportedEncodingException {
        Map<String, List<String>> params = new HashMap<>();

        if (query == null || query.isEmpty()) {
            return params;
        }

        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            String key = URLDecoder.decode(pair[0], Charsets.UTF_8.name());
            String value = "";
            if (pair.length > 1) {
                value = URLDecoder.decode(pair[1], Charsets.UTF_8.name());
            }

            // skip ?& and &&
            if ("".equals(key) && pair.length == 1) {
                continue;
            }

            List<String> values = params.get(key);
            if (values == null) {
                values = new ArrayList<>();
                params.put(key, values);
            }
            values.add(value);
        }

        return params;
    }

}
