package me.chaopeng.chaosrx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * me.chaopeng.chaosrx.JsonUtils
 *
 * @author chao
 * @version 1.0 - 2016-07-04
 */
class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T decode(String s, Class<T> clazz) throws IOException {
        return objectMapper.readValue(s, clazz);
    }

    public static String encode(Object o) throws JsonProcessingException {
        return objectMapper.writeValueAsString(o);
    }

}
