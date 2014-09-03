package org.plugination.connect.core;

import org.plugination.connect.android.AndroidRTCPeerConnection;
import org.plugination.connect.android.AndroidWebSocket;
import org.plugination.connect.core.webrtc.RTCPeerConnection;
import org.plugination.connect.core.webrtc.RTCPeerConnection.EventListener;
import org.plugination.connect.core.websocket.WebSocket;

public class AndroidConnecterPlatform implements ConnectorPlatform {
	@Override
	public WebSocket createWebSocket(String url, WebSocket.EventListener eventListener) {
		return new AndroidWebSocket(url, eventListener);
	}

	@Override
	public RTCPeerConnection createRTCPeerConnection(EventListener listener) {
		return new AndroidRTCPeerConnection(listener);
	}

	public static void init() {
		Connector.platform = new AndroidConnecterPlatform();
	}
}