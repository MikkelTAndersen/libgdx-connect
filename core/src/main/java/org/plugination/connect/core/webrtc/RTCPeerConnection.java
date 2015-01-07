package org.plugination.connect.core.webrtc;

/**
 * This is meant for webrtc data channel only (audio and video is not implemented).
 *
 * To fully understand Webrtc's datachannel - please read the Javascript of this: <a href="http://www.simpl.info/rtcdatachannel">link</a>
 *
 * @author mikkelandersen
 * @see <a href="http://w3c.github.io/webrtc-pc/#rtcdatachannel">Webrtc datachannel spec</a>
 */
public interface RTCPeerConnection {
	interface EventListener {
		void onDataChannel(RTCDataChannelEvent event);

		void onSetLocalDescription(String sessionDescription);

		void onSetRemoteDescription(String sessionDescription);

		void onIceCandidate(String candidate, String sdpMid, int sdpMLineIndex);
	}

	void close();

	RTCDataChannel createOffer();

	void setLocalDescription(String sessionDescription);

	String getLocalDescription();

	void setRemoteDescription(String sessionDescription);

	String getRemoteDescription();

	void createAnswer(String sdp);

	void addIceCandidate(String candidate, String sdpMid, int sdpMLineIndex);

	interface RTCDataChannel {
		void send(String data);

		void close();

		RTCDataChannelState getState();

		void addEventListener(EventListener listener);

		interface EventListener {
			void onMessage(String data);

			void onClose();

			void onError(String reason);

			void onOpen();
		}
	}
}