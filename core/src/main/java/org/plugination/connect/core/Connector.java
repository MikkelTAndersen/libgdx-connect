package org.plugination.connect.core;

import org.plugination.connect.core.websocket.WebSocket;

public class Connector {

	protected static ConnectorPlatform platform;

	private Connector() {
	}

	public static WebSocket createWebSocket(String url, WebSocket.EventListener eventListener) {
		return platform.createWebSocket(url,eventListener);
	}
}