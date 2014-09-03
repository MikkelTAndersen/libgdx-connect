package org.plugination.connect.core;

import org.plugination.connect.core.webrtc.RTCPeerConnection;
import org.plugination.connect.core.websocket.WebSocket;

public class Connector {

	protected static ConnectorPlatform platform;

	private Connector() {
	}

	public static WebSocket createWebSocket(String url, WebSocket.EventListener eventListener) {
		return platform.createWebSocket(url,eventListener);
	}

	public static RTCPeerConnection createRTCPeerConnection(RTCPeerConnection.EventListener listener) {
		return platform.createRTCPeerConnection(listener);
	}
}