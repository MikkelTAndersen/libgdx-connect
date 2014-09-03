package org.plugination.connect.core.webrtc;

import org.plugination.connect.core.webrtc.RTCPeerConnection.RTCDataChannel;

public interface RTCDataChannelEvent {
	RTCDataChannel getChannel();
}