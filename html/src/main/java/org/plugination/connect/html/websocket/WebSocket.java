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
package org.plugination.connect.html.websocket;

import org.plugination.connect.html.websocket.events.CloseEvent;
import org.plugination.connect.html.websocket.events.MessageEvent;
import org.plugination.connect.html.websocket.events.OpenEvent;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.ArrayBufferView;

public class WebSocket extends JavaScriptObject {

  public interface EventListener {
    void onClose(WebSocket socket, CloseEvent event);
    void onMessage(WebSocket socket, MessageEvent event);
    void onOpen(WebSocket socket, OpenEvent event);
  }

  protected WebSocket() {
  }

  public final native void close() /*-{
    this.close();
  }-*/;

  public final native int getBufferedAmount() /*-{
    return this.bufferedAmount;
  }-*/;

  public final native short getReadyState() /*-{
    return this.readyState;
  }-*/;

  public final native String getURL() /*-{
    return this.url;
  }-*/;

  public final native boolean send(String data) /*-{
    return this.send(data);
  }-*/;

  public final native boolean send(ArrayBufferView data) /*-{
    return this.send(data);
  }-*/;

  public final native boolean send(ArrayBuffer data) /*-{
    return this.send(data);
  }-*/;

  public final native void setListener(EventListener listener) /*-{
    if (!listener) {
      this.onopen = null;
      this.onclose = null;
      this.onmessage = null;
      return;
    }
    var self = this;
    this.onopen = function(e) {
      listener.@org.plugination.connect.html.websocket.WebSocket.EventListener::onOpen(Lorg/plugination/connect/html/websocket/WebSocket;Lorg/plugination/connect/html/websocket/events/OpenEvent;)(self, e);
    };
    this.onclose = function(e) {
      listener.@org.plugination.connect.html.websocket.WebSocket.EventListener::onClose(Lorg/plugination/connect/html/websocket/WebSocket;Lorg/plugination/connect/html/websocket/events/CloseEvent;)(self, e);
    };
    this.onmessage = function(e) {
      listener.@org.plugination.connect.html.websocket.WebSocket.EventListener::onMessage(Lorg/plugination/connect/html/websocket/WebSocket;Lorg/plugination/connect/html/websocket/events/MessageEvent;)(self, e);
    };
  }-*/;


  public static native WebSocket create(String url) /*-{
    var ws = new WebSocket(url);
    ws.binaryType = 'arraybuffer';
    return ws;
  }-*/;

  public static native WebSocket create(String url, String protocol) /*-{
    return new WebSocket(url, protocol);
  }-*/;
}