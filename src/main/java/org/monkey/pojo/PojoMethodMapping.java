package org.monkey.pojo;

import io.netty.handler.codec.http.HttpHeaders;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.monkey.annotation.*;
import org.monkey.exception.DeploymentException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class PojoMethodMapping {

    private final Method onOpen;
    private final Method onClose;
    private final Method onError;
    private final Method onMessage;
    private final Method onBinary;
    private final Method onEvent;
    private final PojoPathParam[] onOpenParams;
    private final PojoPathParam[] onCloseParams;
    private final PojoPathParam[] onErrorParams;
    private final PojoPathParam[] onMessageParams;
    private final PojoPathParam[] onBinaryParams;
    private final PojoPathParam[] onEventParams;
    private final Class pojoClazz;
    private final ApplicationContext applicationContext;
    private boolean hasParameterMap = false;

    public PojoMethodMapping(Class<?> pojoClazz, ApplicationContext context) throws DeploymentException {
        this.applicationContext = context;
        this.pojoClazz = pojoClazz;
        Method open = null;
        Method close = null;
        Method error = null;
        Method message = null;
        Method binary = null;
        Method event = null;
        Method[] pojoClazzMethods = null;
        Class<?> currentClazz = pojoClazz;
        while (!currentClazz.equals(Object.class)) {
            Method[] currentClazzMethods = currentClazz.getDeclaredMethods();
            if (currentClazz == pojoClazz) {
                pojoClazzMethods = currentClazzMethods;
            }
            for (Method method : currentClazzMethods) {
                if (method.getAnnotation(OnOpen.class) != null) {
                    checkPublic(method);
                    if (open == null) {
                        open = method;
                    } else {
                        if (currentClazz == pojoClazz ||
                                !isMethodOverride(open, method)) {
                            // Duplicate annotation
                            throw new DeploymentException(
                                    "pojoMethodMapping.duplicateAnnotation OnOpen");
                        }
                    }
                } else if (method.getAnnotation(OnClose.class) != null) {
                    checkPublic(method);
                    if (close == null) {
                        close = method;
                    } else {
                        if (currentClazz == pojoClazz ||
                                !isMethodOverride(close, method)) {
                            // Duplicate annotation
                            throw new DeploymentException(
                                    "pojoMethodMapping.duplicateAnnotation OnClose");
                        }
                    }
                } else if (method.getAnnotation(OnError.class) != null) {
                    checkPublic(method);
                    if (error == null) {
                        error = method;
                    } else {
                        if (currentClazz == pojoClazz ||
                                !isMethodOverride(error, method)) {
                            // Duplicate annotation
                            throw new DeploymentException(
                                    "pojoMethodMapping.duplicateAnnotation OnError");
                        }
                    }
                } else if (method.getAnnotation(OnMessage.class) != null) {
                    checkPublic(method);
                    if (message == null) {
                        message = method;
                    } else {
                        if (currentClazz == pojoClazz ||
                                !isMethodOverride(message, method)) {
                            // Duplicate annotation
                            throw new DeploymentException(
                                    "pojoMethodMapping.duplicateAnnotation onMessage");
                        }
                    }
                } else if (method.getAnnotation(OnBinary.class) != null) {
                    checkPublic(method);
                    if (binary == null) {
                        binary = method;
                    } else {
                        if (currentClazz == pojoClazz ||
                                !isMethodOverride(binary, method)) {
                            // Duplicate annotation
                            throw new DeploymentException(
                                    "pojoMethodMapping.duplicateAnnotation OnBinary");
                        }
                    }
                } else if (method.getAnnotation(OnEvent.class) != null) {
                    checkPublic(method);
                    if (event == null) {
                        event = method;
                    } else {
                        if (currentClazz == pojoClazz ||
                                !isMethodOverride(event, method)) {
                            // Duplicate annotation
                            throw new DeploymentException(
                                    "pojoMethodMapping.duplicateAnnotation OnEvent");
                        }
                    }
                } else {
                    // Method not annotated
                }
            }
            currentClazz = currentClazz.getSuperclass();
        }
        // If the methods are not on pojoClazz and they are overridden
        // by a non annotated method in pojoClazz, they should be ignored
        if (open != null && open.getDeclaringClass() != pojoClazz) {
            if (isOverridenWithoutAnnotation(pojoClazzMethods, open, OnOpen.class)) {
                open = null;
            }
        }
        if (close != null && close.getDeclaringClass() != pojoClazz) {
            if (isOverridenWithoutAnnotation(pojoClazzMethods, close, OnClose.class)) {
                close = null;
            }
        }
        if (error != null && error.getDeclaringClass() != pojoClazz) {
            if (isOverridenWithoutAnnotation(pojoClazzMethods, error, OnError.class)) {
                error = null;
            }
        }
        if (message != null && message.getDeclaringClass() != pojoClazz) {
            if (isOverridenWithoutAnnotation(pojoClazzMethods, message, OnMessage.class)) {
                message = null;
            }
        }
        if (binary != null && binary.getDeclaringClass() != pojoClazz) {
            if (isOverridenWithoutAnnotation(pojoClazzMethods, binary, OnBinary.class)) {
                binary = null;
            }
        }
        if (event != null && event.getDeclaringClass() != pojoClazz) {
            if (isOverridenWithoutAnnotation(pojoClazzMethods, event, OnEvent.class)) {
                event = null;
            }
        }

        this.onOpen = open;
        this.onClose = close;
        this.onError = error;
        this.onMessage = message;
        this.onBinary = binary;
        this.onEvent = event;
        onOpenParams = getPathParams(onOpen, MethodType.ON_OPEN);
        onCloseParams = getPathParams(onClose, MethodType.ON_CLOSE);
        onErrorParams = getPathParams(onError, MethodType.ON_ERROR);
        onMessageParams = getPathParams(onMessage, MethodType.ON_MESSAGE);
        onBinaryParams = getPathParams(onBinary, MethodType.ON_BINARY);
        onEventParams = getPathParams(onEvent, MethodType.ON_EVENT);

        for (PojoPathParam onOpenParam : onOpenParams) {
            if (ParameterMap.class.equals(onOpenParam.getType())) {
                this.hasParameterMap = true;
                break;
            }
        }
    }

    public boolean hasParameterMap() {
        return hasParameterMap;
    }

    private void checkPublic(Method m) throws DeploymentException {
        if (!Modifier.isPublic(m.getModifiers())) {
            throw new DeploymentException(
                    "pojoMethodMapping.methodNotPublic " + m.getName());
        }
    }

    private boolean isMethodOverride(Method method1, Method method2) {
        return (method1.getName().equals(method2.getName())
                && method1.getReturnType().equals(method2.getReturnType())
                && Arrays.equals(method1.getParameterTypes(), method2.getParameterTypes()));
    }

    private boolean isOverridenWithoutAnnotation(Method[] methods, Method superclazzMethod, Class<? extends Annotation> annotation) {
        for (Method method : methods) {
            if (isMethodOverride(method, superclazzMethod)
                    && (method.getAnnotation(annotation) == null)) {
                return true;
            }
        }
        return false;
    }

    public Object getEndpointInstance() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Object implement = pojoClazz.getDeclaredConstructor().newInstance();
        AutowiredAnnotationBeanPostProcessor postProcessor = applicationContext.getBean(AutowiredAnnotationBeanPostProcessor.class);
        postProcessor.postProcessPropertyValues(null, null, implement, null);
        return implement;
    }

    public Method getOnOpen() {
        return onOpen;
    }

    public Object[] getOnOpenArgs(Session session, HttpHeaders headers, ParameterMap parameterMap) {
        return buildArgs(onOpenParams, session, headers, null, null, null, null, parameterMap);
    }

    public Method getOnClose() {
        return onClose;
    }

    public Object[] getOnCloseArgs(Session session) {
        return buildArgs(onCloseParams, session, null, null, null, null, null, null);
    }

    public Method getOnError() {
        return onError;
    }

    public Object[] getOnErrorArgs(Session session, Throwable throwable) {
        return buildArgs(onErrorParams, session, null, null, null, throwable, null, null);
    }

    public Method getOnMessage() {
        return onMessage;
    }

    public Object[] getOnMessageArgs(Session session, String text) {
        return buildArgs(onMessageParams, session, null, text, null, null, null, null);
    }

    public Method getOnBinary() {
        return onBinary;
    }

    public Object[] getOnBinaryArgs(Session session, byte[] bytes) {
        return buildArgs(onBinaryParams, session, null, null, bytes, null, null, null);
    }

    public Method getOnEvent() {
        return onEvent;
    }

    public Object[] getOnEventArgs(Session session, Object evt) {
        return buildArgs(onEventParams, session, null, null, null, null, evt, null);
    }

    private static PojoPathParam[] getPathParams(Method m, MethodType methodType) throws DeploymentException {
        if (m == null) {
            return new PojoPathParam[0];
        }
        boolean foundThrowable = false;
        Class<?>[] types = m.getParameterTypes();
        PojoPathParam[] result = new PojoPathParam[types.length];
        for (int i = 0; i < types.length; i++) {
            Class<?> type = types[i];
            if (type.equals(Session.class)) {
                result[i] = new PojoPathParam(type, "session");
            } else if (methodType == MethodType.ON_OPEN &&
                    type.equals(HttpHeaders.class)) {
                result[i] = new PojoPathParam(type, "headers");
            } else if (methodType == MethodType.ON_OPEN &&
                    type.equals(ParameterMap.class)) {
                result[i] = new PojoPathParam(type, "parameterMap");
            } else if (methodType == MethodType.ON_ERROR
                    && type.equals(Throwable.class)) {
                foundThrowable = true;
                result[i] = new PojoPathParam(type, "throwable");
            } else if (methodType == MethodType.ON_MESSAGE &&
                    type.equals(String.class)) {
                result[i] = new PojoPathParam(type, "text");
            } else if (methodType == MethodType.ON_BINARY &&
                    type.equals(byte[].class)) {
                result[i] = new PojoPathParam(type, "binary");
            } else if (methodType == MethodType.ON_EVENT &&
                    type.equals(Object.class)) {
                result[i] = new PojoPathParam(type, "event");
            } else if (type.getSimpleName().equals("Session") && !type.equals(Session.class)) {
                throw new DeploymentException(
                        "expect to import org.monkey.pojo.Session not " + type.getName());
            } else if (type.getSimpleName().equals("HttpHeaders") && !type.equals(HttpHeaders.class)) {
                throw new DeploymentException(
                        "expect to import io.netty.handler.codec.http.HttpHeaders not " + type.getName());
            } else if (type.getSimpleName().equals("ParameterMap") && !type.equals(ParameterMap.class)) {
                throw new DeploymentException(
                        "expect to import org.monkey.pojo.ParameterMap not " + type.getName());
            } else {
                throw new DeploymentException(
                        "pojoMethodMapping.paramClassIncorrect");
            }
        }
        if (methodType == MethodType.ON_ERROR && !foundThrowable) {
            throw new DeploymentException(
                    "pojoMethodMapping.onErrorNoThrowable");
        }
        return result;
    }

    private static Object[] buildArgs(PojoPathParam[] pathParams, Session session,
                                      HttpHeaders headers, String text, byte[] bytes,
                                      Throwable throwable, Object evt, ParameterMap parameterMap) {
        Object[] result = new Object[pathParams.length];
        for (int i = 0; i < pathParams.length; i++) {
            Class<?> type = pathParams[i].getType();
            if (type.equals(Session.class)) {
                result[i] = session;
            } else if (type.equals(HttpHeaders.class)) {
                result[i] = headers;
            } else if (type.equals(String.class)) {
                result[i] = text;
            } else if (type.equals(byte[].class)) {
                result[i] = bytes;
            } else if (type.equals(Throwable.class)) {
                result[i] = throwable;
            } else if (type.equals(Object.class)) {
                result[i] = evt;
            } else if (type.equals(ParameterMap.class)) {
                result[i] = parameterMap;
            }
        }
        return result;
    }

    private enum MethodType {
        ON_OPEN,
        ON_CLOSE,
        ON_MESSAGE,
        ON_BINARY,
        ON_EVENT,
        ON_ERROR
    }
}