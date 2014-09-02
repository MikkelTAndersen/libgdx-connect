package org.plugination.connect.android;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.plugination.connect.core.websocket.WebSocket;

import com.badlogic.gdx.Gdx;

public class AndroidWebSocket implements WebSocket {

	private final WebSocketClient socket;

	public AndroidWebSocket(String uri, final EventListener listener) {
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