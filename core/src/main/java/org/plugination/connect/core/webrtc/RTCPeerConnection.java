package org.plugination.connect.core.webrtc;

public interface RTCPeerConnection {
	interface EventListener {
		void onDataChannel(RTCDataChannelEvent event);

		void onSetLocalDescription(String sessionDescription);

		void onSetRemoteDescription(String sessionDescription);

		void onIceCandidate(String candidate, String sdpMid, int sdpMLineIndex);
	}

	void close();

	RTCDataChannel createOffer();



	public interface RTCDataChannel {
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

	String getLocalDescription();

	void setLocalDescription(String sessionDescription);

	String getRemoteDescription();

	void setRemoteDescription(String sessionDescription);

	void createAnswer(String sdp);

	void addIceCandidate(String candidate, String sdpMid, int sdpMLineIndex);

}
