package org.plugination.connect.android;

import org.plugination.connect.core.ConnectorPlatform;
import org.plugination.connect.core.websocket.WebSocket;

public class AndroidConnecterPlatform implements ConnectorPlatform {
	@Override
	public WebSocket createWebSocket(String url, WebSocket.EventListener eventListener) {
		return new AndroidWebSocket(url, eventListener);
	}
}