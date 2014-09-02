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
package org.plugination.connect.html;


import java.nio.ByteBuffer;

import org.plugination.connect.html.websocket.WebSocket;
import org.plugination.connect.html.websocket.events.CloseEvent;
import org.plugination.connect.html.websocket.events.MessageEvent;
import org.plugination.connect.html.websocket.events.OpenEvent;

import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.TypedArrays;
import com.google.gwt.typedarrays.shared.Uint8Array;

public class HtmlWebSocket implements org.plugination.connect.core.websocket.WebSocket {
	private WebSocket ws;

	private static ByteArrayBuffer wrapper = (ByteArrayBuffer) ByteBuffer.allocate(42);

	public HtmlWebSocket( String url, final org.plugination.connect.core.websocket.WebSocket.EventListener eventListener) {
		ws = WebSocket.create(url);
		ws.setListener(new WebSocket.EventListener() {
			@Override
			public void onOpen(WebSocket socket, OpenEvent event) {
				eventListener.onOpen();
			}

			@Override
			public void onMessage(WebSocket socket, MessageEvent event) {
				if (event.dataIsText()) {
					eventListener.onTextMessage(event.stringData());
				} else {
					eventListener.onDataMessage(wrapper.wrap(event.bufferData()));
				}
			}

			@Override
			public void onClose(WebSocket socket, CloseEvent event) {
				eventListener.onClose();
			}
		});
	}

	@Override
	public void close() {
		ws.close();
	}

	@Override
	public void send(String data) {
		ws.send(data);
	}

	@Override
	public void send(ByteBuffer data) {
		int limit = data.limit();
		ArrayBuffer buffer = TypedArrays.createArrayBuffer(limit);
		Uint8Array arrayBuffer = TypedArrays.createUint8Array(buffer);
		for (int i = 0; i < limit; i++) {
			arrayBuffer.set(i, data.get(i));
		}
		ws.send(buffer);
	}

	public interface ByteArrayBuffer {
	    java.nio.ByteBuffer wrap(ArrayBuffer arrayBuffer);
	}
}
