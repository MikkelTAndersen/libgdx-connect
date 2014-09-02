package org.plugination.connect.core;

import org.plugination.connect.core.websocket.WebSocket;
import org.plugination.connect.core.websocket.WebSocket.EventListener;

public interface ConnectorPlatform {
	WebSocket createWebSocket(String url, EventListener eventListener);
}