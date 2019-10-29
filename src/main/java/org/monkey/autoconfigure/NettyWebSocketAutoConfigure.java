package org.monkey.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.monkey.annotation.EnableWebSocket;
import org.monkey.standard.ServerEndpointExporter;

@EnableWebSocket
@ConditionalOnMissingBean(ServerEndpointExporter.class)
public class NettyWebSocketAutoConfigure {
}
