package org.plugination.connect.core.websocket;

import java.nio.ByteBuffer;

public interface WebSocket {

	interface EventListener {
      void onOpen();

      void onTextMessage(String message);

      void onDataMessage(ByteBuffer message);

      void onClose();

      void onError(String error);
    }

    void close();

    void send(String message);

    void send(ByteBuffer message);
}