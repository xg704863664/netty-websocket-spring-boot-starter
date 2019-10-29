package org.monkey.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Yeauty
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ServerEndpoint {

    @AliasFor("path")
    String value() default "/";

    @AliasFor("value")
    String path() default "/";

    String host() default "0.0.0.0";

    int port() default 80;

    int bossLoopGroupThreads() default 0;

    int workerLoopGroupThreads() default 0;

    boolean useCompressionHandler() default false;

    /**
     * if this property is not empty, means configure with application.properties
     */
    String prefix() default "";

    //------------------------- option -------------------------

    int optionConnectTimeoutMillis() default 30000;

    int optionSoBacklog() default 128;

    //------------------------- childOption -------------------------

    int childOptionWriteSpinCount() default 16;

    int childOptionWriteBufferHighWaterMark() default 64 * 1024;

    int childOptionWriteBufferLowWaterMark() default 32 * 1024;

    int childOptionSoRcvbuf() default -1;

    int childOptionSoSndbuf() default -1;

    boolean childOptionTcpNodelay() default true;

    boolean childOptionSoKeepalive() default false;

    int childOptionSoLinger() default -1;

    boolean childOptionAllowHalfClosure() default false;

    //------------------------- idleEvent -------------------------

    int readerIdleTimeSeconds() default 0;

    int writerIdleTimeSeconds() default 0;

    int allIdleTimeSeconds() default 0;

    //------------------------- handshake -------------------------

    int maxFramePayloadLength() default 65536;

    String subprotocols() default "";

}
