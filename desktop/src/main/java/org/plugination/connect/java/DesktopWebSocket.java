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
package org.plugination.connect.java;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.plugination.connect.core.websocket.WebSocket;

import com.badlogic.gdx.Gdx;

public class DesktopWebSocket implements WebSocket {

	private final WebSocketClient socket;

	public DesktopWebSocket(String uri, final EventListener listener) {
		try {
			URI javaURI = new URI(uri);
			socket = new WebSocketClient(javaURI) {
				@Override
				public void onMessage(final ByteBuffer messageBuffer) {
					Gdx.app.postRunnable(new Runnable() {
						public void run() {
							listener.onDataMessage(messageBuffer);
						}
					});
				}

				@Override
				public void onMessage(final String message) {
					Gdx.app.postRunnable(new Runnable() {
						public void run() {
							listener.onTextMessage(message);
						}
					});
				}

				@Override
				public void onError(final Exception exception) {
					Gdx.app.postRunnable(new Runnable() {
						public void run() {
							listener.onError(exception.getMessage());
						}
					});
				}

				@Override
				public void onClose(int arg0, String arg1, boolean arg2) {
					Gdx.app.postRunnable(new Runnable() {
						public void run() {
							listener.onClose();
						}
					});
				}

				@Override
				public void onOpen(ServerHandshake handshake) {
					Gdx.app.postRunnable(new Runnable() {
						@Override
						public void run() {
							listener.onOpen();
						}
					});
				}
			};
			socket.connect();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		socket.close();
	}

	@Override
	public void send(String data) {
		try {
			socket.getConnection().send(data);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void send(ByteBuffer data) {
		try {
			socket.getConnection().send(data);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
}