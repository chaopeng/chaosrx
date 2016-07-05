package me.chaopeng.chaosrx;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

/**
 * HttpRouter
 *
 * @author chao
 * @version 1.0 - 11/26/14
 */
public class HttpRouter {

    private static final Logger logger = LoggerFactory.getLogger(HttpRouter.class);

    private UrlTreeNode root = new UrlTreeNode();
    private Map<String, HttpAction> registerPath = new HashMap<>();


    public HttpRouter(Collection<AbstractHttpHandler> handlers) {
        for (AbstractHttpHandler handler : handlers) {
            Class<? extends AbstractHttpHandler> clazz = handler.getClass();
            Path path = clazz.getAnnotation(Path.class);
            Preconditions.checkArgument(path != null, clazz.getName() + " does not have a @Path annotation.");
            String p = path.value();

            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.getReturnType().equals(Response.class)) {

                    Path path0 = method.getAnnotation(Path.class);

                    if (path0 != null) {
                        addAction(handler, method, UrlUtils.path(p + "/" + path0.value()));
                    }
                }
            }

        }

    }

    private void addAction(AbstractHttpHandler handler, Method method, String path) {

        Set<String> mset = methodSet(method);

        HttpAction action = new HttpAction(handler, method, path);
        String p = pathRemoveVar(path);

        if (path.equals("/")) {
            if (mset.isEmpty()) {
                Preconditions.checkArgument(!registerPath.containsKey(p), action.methodStr + "is conflict");
                registerPath.put(p, action);
                root.defaultAction = action;
                logger.info("register path: " + p + " +++ " + action.methodStr);
            }
            for (String m : mset) {
                String p1 = m + "::" + p;
                Preconditions.checkArgument(!registerPath.containsKey(p1), action.methodStr + "is conflict");
                registerPath.put(p1, action);
                root.actions.put(m, action);
                logger.info("register path: " + p1 + " +++ " + action.methodStr);
            }
        } else {
            String[] words = path.split("/");
            UrlTreeNode ptr = root;
            for (String word : words) {
                if (VAR_PATTERN.matcher(word).matches()) {
                    String var = word.substring(1, word.length() - 1);
                    if (!ptr.varNext.containsKey(var)) {
                        ptr.varNext.put(var, new UrlTreeNode());

                    }
                    ptr = ptr.varNext.get(var);
                } else {
                    if (!ptr.strNext.containsKey(word)) {
                        ptr.strNext.put(word, new UrlTreeNode());

                    }
                    ptr = ptr.strNext.get(word);
                }
            }

            if (mset.isEmpty()) {
                Preconditions.checkArgument(!registerPath.containsKey(p), action.methodStr + "is conflict");
                registerPath.put(p, action);
                ptr.defaultAction = action;

                logger.info("register path: " + p + " +++ " + action.methodStr);
            }

            for (String m : mset) {
                String p1 = m + "::" + p;
                Preconditions.checkArgument(!registerPath.containsKey(p1), action.methodStr + "is conflict");
                registerPath.put(p1, action);
                ptr.actions.put(m, action);
                logger.info("register path: " + p1 + " +++ " + action.methodStr);
            }
        }
    }

    private static final Pattern VAR_PATTERN = Pattern.compile("\\{[^\\}/]*\\}");

    private String pathRemoveVar(String s) {
        return VAR_PATTERN.matcher(s).replaceAll("*");
    }

    private Set<String> methodSet(Method method) {
        Set<String> set = new HashSet<>();
        if (method.getAnnotation(GET.class) != null) {
            set.add(HttpMethod.GET);
        }
        if (method.getAnnotation(POST.class) != null) {
            set.add(HttpMethod.POST);
        }
        if (method.getAnnotation(PUT.class) != null) {
            set.add(HttpMethod.PUT);
        }
        if (method.getAnnotation(DELETE.class) != null) {
            set.add(HttpMethod.DELETE);
        }
        return set;
    }

    private HttpAction findAction(Request request) {

        HttpAction action = null;

        if (request.getPath().equals("/")) {
            action = root.actions.get(request.getMethod().name());
            if (action == null) {
                action = root.defaultAction;
            }
        } else {
            String[] ss = request.getPath().split("/");

            UrlTreeNode node = dfsTree(root, ss, 0, request.getPathParameters());
            if (node != null) {
                action = node.actions.get(request.getMethod().name());
                if (action == null) {
                    action = node.defaultAction;
                }
            }
        }
        return action;
    }

    private UrlTreeNode dfsTree(UrlTreeNode ptr, String[] ss, int index, Map<String, String> vars) {

        String s = ss[index];
        UrlTreeNode node = null;

        if (ptr.strNext.containsKey(s)) {
            if (index == ss.length - 1) {
                return ptr.strNext.get(s);
            }
            node = dfsTree(ptr.strNext.get(s), ss, index + 1, vars);
        }

        if (node == null) {
            for (Map.Entry<String, UrlTreeNode> entry : ptr.varNext.entrySet()) {
                if (index == ss.length - 1) {
                    vars.put(entry.getKey(), s);
                    return entry.getValue();
                }

                node = dfsTree(entry.getValue(), ss, index + 1, vars);
                if (node != null) {
                    vars.put(entry.getKey(), s);
                    break;
                }
            }
        }

        return node;
    }

    public Response invoke(Request request) throws InvocationTargetException, IllegalAccessException {
        HttpAction httpAction = findAction(request);

        if (httpAction == null) {
            throw new ForbiddenException("API not found: [" + request.getMethod().toString() + "] " + request.getPath());
        }

        logger.debug(request.toString());
        logger.debug("found match action {}", httpAction.methodStr);

        long begin = System.currentTimeMillis();

        Response response = null;

        try {
            if (httpAction.needBefore) {
                response = httpAction.handler.before(request);
                if (response != null) {
                    return response;
                }
            }

            Object[] params = ParamsHelper.getParams(httpAction.method, request);

            response = (Response) httpAction.method.invoke(httpAction.handler, params);

            if (httpAction.needAfter) {
                response = httpAction.handler.after(request, response);
            }
        } finally {

            long used = System.currentTimeMillis() - begin;

            logger.debug("{} finish,  use {}ms. ", httpAction.methodStr, used);

            if (used > 1000) {
                logger.info("{} finish but slow,  use {}ms. ", httpAction.methodStr, used);
            }
        }

        return response;
    }

    protected static class ParamsHelper {
        public static Object[] getParams(Method method, Request request) {

            Class<?>[] parameterTypes = method.getParameterTypes();
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();

            int length = parameterTypes.length;

            Object[] res = new Object[length];

            for (int i = 0; i < length; i++) {
                Class<?> type = parameterTypes[i];
                if (type == Request.class) {
                    res[i] = request;
                }

                for (Annotation annotation : parameterAnnotations[i]) {
                    if (annotation instanceof QueryParam) {
                        QueryParam queryParam = (QueryParam) annotation;

                        String name = queryParam.value();

                        if (type == List.class) {
                            res[i] = request.getQueryParameters(name);
                        } else {
                            res[i] = convert(request.getQueryParameter(name), type);
                        }
                    } else if (annotation instanceof HeaderParam) {
                        HeaderParam headerParam = (HeaderParam) annotation;
                        String name = headerParam.value();
                        res[i] = convert(request.getHeaderParameter(name), type);
                    } else if (annotation instanceof PathParam) {
                        PathParam pathParam = (PathParam) annotation;
                        String name = pathParam.value();
                        res[i] = convert(request.getPathParameter(name), type);
                    } else if (annotation instanceof HttpBody) {
                        if (type == byte[].class) {
                            res[i] = request.getHttpBody();
                        } else if (type == String.class) {
                            res[i] = request.getUTF8Body();
                        } else {
                            String s = request.getUTF8Body();
                            if (!s.isEmpty()) {
                                try {
                                    res[i] = JsonUtils.decode(s, type);
                                } catch (IOException e) {
                                    logger.error("json decode failed. err={}, str={}", e.getMessage(), s);
                                }
                            }
                        }
                    } else {
                        continue;
                    }
                    break;
                }
            }

            return res;
        }

        public static Object convert(String s, Class type) {
            if (type == String.class || type == Object.class) {
                return s;
            } else if (type == int.class || type == Integer.class) {
                return Integer.valueOf(s);
            }

            return null;
        }
    }

    protected static class HttpAction {

        private AbstractHttpHandler handler;
        private Method method;
        private boolean needBefore;
        private boolean needAfter;
        private String pathStr;
        private String methodStr;

        public HttpAction(AbstractHttpHandler handler, Method method, String pathStr) {
            this.handler = handler;
            this.method = method;
            this.pathStr = pathStr;
            this.needBefore = method.getAnnotation(Before.class) != null;
            this.needAfter = method.getAnnotation(After.class) != null;
            this.methodStr = handler.getClass().getName() + "." + method.getName() + "()";
        }

        @Override
        public String toString() {
            return "path=" + pathStr + ", method=" + methodStr;
        }
    }

    protected static class UrlTreeNode {

        private Map<String, UrlTreeNode> strNext = new HashMap<>();
        private Map<String, UrlTreeNode> varNext = new HashMap<>();
        private Map<String, HttpAction> actions = new HashMap<>();
        private HttpAction defaultAction = null;

    }
}
