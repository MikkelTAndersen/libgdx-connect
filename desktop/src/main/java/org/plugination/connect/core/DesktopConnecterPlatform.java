/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.plugination.connect.core;

import org.plugination.connect.core.Connector;
import org.plugination.connect.core.ConnectorPlatform;
import org.plugination.connect.core.webrtc.RTCPeerConnection;
import org.plugination.connect.core.webrtc.RTCPeerConnection.EventListener;
import org.plugination.connect.core.websocket.WebSocket;
import org.plugination.connect.java.DesktopRTCPeerConnection;
import org.plugination.connect.java.DesktopWebSocket;

public class DesktopConnecterPlatform implements ConnectorPlatform {
	@Override
	public WebSocket createWebSocket(String url, WebSocket.EventListener eventListener) {
		return new DesktopWebSocket(url, eventListener);
	}

	@Override
	public RTCPeerConnection createRTCPeerConnection(EventListener listener) {
		return new DesktopRTCPeerConnection(listener);
	}

	public static void init() {
		Connector.platform = new DesktopConnecterPlatform();
	}
}