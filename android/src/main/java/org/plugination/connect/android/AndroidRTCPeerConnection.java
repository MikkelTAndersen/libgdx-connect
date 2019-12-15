package org.plugination.connect.android;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.plugination.connect.core.webrtc.RTCDataChannelEvent;
import org.plugination.connect.core.webrtc.RTCDataChannelState;
import org.plugination.connect.core.webrtc.RTCPeerConnection;
import org.webrtc.DataChannel;
import org.webrtc.DataChannel.Buffer;
import org.webrtc.DataChannel.Init;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnection.IceConnectionState;
import org.webrtc.PeerConnection.IceGatheringState;
import org.webrtc.PeerConnection.IceServer;
import org.webrtc.PeerConnection.Observer;
import org.webrtc.PeerConnection.SignalingState;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SessionDescription.Type;

import com.badlogic.gdx.Gdx;


public class AndroidRTCPeerConnection implements RTCPeerConnection {
	private DataChannel dataChannel;
	private PeerConnection pc;
	private EventListener listener;
	private PeerConnectionFactory factory = new PeerConnectionFactory();

	public AndroidRTCPeerConnection(final EventListener listener) {
		pc = factory.createPeerConnection(getIceServers(), getConstraints(), new CreatePeerConnectionObserver());
		this.listener = listener;
	}

	private List<IceServer> getIceServers() {
		LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<PeerConnection.IceServer>();
	    iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
	    iceServers.add(new PeerConnection.IceServer("stun:stun1.l.google.com:19302"));
	    iceServers.add(new PeerConnection.IceServer("stun:stun2.l.google.com:19302"));
	    iceServers.add(new PeerConnection.IceServer("stun:stun3.l.google.com:19302"));
	    iceServers.add(new PeerConnection.IceServer("stun:stun4.l.google.com:19302"));
        return iceServers;
	}

	private MediaConstraints getConstraints() {
		MediaConstraints constraints = new MediaConstraints();
		constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
		constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"));
		constraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
		constraints.optional.add(new MediaConstraints.KeyValuePair("RtpDataChannels ", "false"));
		return constraints;
	}

	@Override
	public void close() {
		pc.close();
	}

	public RTCDataChannel createOffer() {
		//TODO figure out how to make it sync with HTML reliable true.
		dataChannel = pc.createDataChannel("sendDataCh", new Init());
		pc.createOffer(new OfferObserver(), getConstraints());
		return new DataChannelEventImpl(dataChannel).getChannel();
	}

	@Override
	public String getLocalDescription() {
		SessionDescription localDescription = pc.getLocalDescription();
		return localDescription.description;
	}

	@Override
	public void createAnswer(String sdp) {
		final SessionDescription sessionDescription = new SessionDescription(Type.OFFER, preferISAC(sdp));
		pc.setRemoteDescription(new RemoteDescriptionObserver(), sessionDescription);
	}

	@Override
	public void addIceCandidate(String candidate, String sdpMid,
			int sdpMLineIndex) {
		if (candidate != null) {
			pc.addIceCandidate(new IceCandidate(sdpMid, sdpMLineIndex, candidate));
		}
	}

	@Override
	public void setLocalDescription(final String sdp) {
		SessionDescription sessionDescription = new SessionDescription(Type.OFFER, preferISAC(sdp));
		pc.setLocalDescription(new SdpObserver() {
			@Override
			public void onSetSuccess() {
			}

			@Override
			public void onSetFailure(String error) {
				Gdx.app.log("Webrtc", "Local Description FAILED : " + error);
			}

			@Override
			public void onCreateSuccess(SessionDescription arg0) {
			}

			@Override
			public void onCreateFailure(String error) {
				Gdx.app.log("Webrtc", "Local Description FAILED : " + error);
			}
		}, sessionDescription);
	}

	@Override
	public String getRemoteDescription() {
		return pc.getRemoteDescription().description;
	}

	@Override
	public void setRemoteDescription(final String sdp) {
		SessionDescription sessionDescription = new SessionDescription(Type.ANSWER, sdp);

		pc.setRemoteDescription(new SdpObserver() {
			@Override
			public void onSetSuccess() {
			}

			@Override
			public void onSetFailure(String error) {
				Gdx.app.log("Webrtc", "Remote Description FAILED : " + error);
			}

			@Override
			public void onCreateSuccess(SessionDescription arg0) {
			}

			@Override
			public void onCreateFailure(String error) {
				Gdx.app.log("Webrtc", "Local Description FAILED : " + error);
			}
		}, sessionDescription);
	}

	private class DataChannelEventImpl implements RTCDataChannelEvent {
		private final DataChannel dataChannel;
		private RTCDataChannel rtcDataChannel;

		public DataChannelEventImpl(DataChannel channel) {
			this.dataChannel = channel;
		    rtcDataChannel = new RTCDataChannel(){
				@Override
				public void addEventListener(final EventListener dataListener) {
					dataChannel.registerObserver(new DataChannel.Observer() {
						@Override
						public void onStateChange() {
						}

						@Override
						public void onMessage(Buffer buffer) {
							byte[] data = new byte[buffer.data.remaining()];
						    buffer.data.get(data);
						    final String dataString = new String(data);
							Gdx.app.postRunnable(new Runnable() {
								@Override
								public void run() {
									dataListener.onMessage(dataString);
								}
							});
						}

						@Override
						public void onBufferedAmountChange (long arg0) {
						}
					});

////					Gdx.app.log("Webrtc", "ADDED LISTENERS " + gwtChannel.getReadyState());
//					dataChannel.addMessageHandler(new Handler() {
//						@Override
//						public void onMessage(MessageEvent event) {
////							Gdx.app.log("Webrtc", "onMessage LISTENERS");
//						}
//					});
//					dataChannel.addCloseHandler(new CloseEvent.Handler() {
//						@Override
//						public void onClose(CloseEvent event) {
//							dataListener.onClose();
//						}
//					});
//					dataChannel.addOpenHandler(new com.seanchenxi.gwt.html.client.event.OpenEvent.Handler(){
//						@Override
//						public void onOpen(com.seanchenxi.gwt.html.client.event.OpenEvent event) {
////							Gdx.app.log("Webrtc", "addOpenHandler LISTENERS");
//							dataListener.onOpen();
//						}
//					});
//					dataChannel.addErrorHandler(new ErrorEvent.Handler<NativeEvent>(){
//						@Override
//						public void onError(ErrorEvent<NativeEvent> event) {
////							Gdx.app.log("Webrtc", "onError LISTENERS " +event.toDebugString());
//							dataListener.onError(event.toDebugString());
//						}});
				}

				@Override
				public void close() {
					dataChannel.close();
				}

				@Override
				public void send(String data) {
					dataChannel.send(new DataChannel.Buffer(ByteBuffer.wrap(data.getBytes()), false));
				}

				@Override
				public RTCDataChannelState getState() {
					switch (dataChannel.state()) {
					case CONNECTING:
						return RTCDataChannelState.CONNECTING;
					case CLOSING:
						return RTCDataChannelState.CLOSING;
					case OPEN:
						return RTCDataChannelState.OPEN;
					case CLOSED:
						return RTCDataChannelState.CLOSED;
					}
					return RTCDataChannelState.CLOSED;
				}
			};
		}

		@Override
		public RTCDataChannel getChannel() {
			return rtcDataChannel;
		}
	}

	private static String preferISAC(String sdpDescription) {
		String[] lines = sdpDescription.split("\r\n");
		int mLineIndex = -1;
		String isac16kRtpMap = null;
		Pattern isac16kPattern = Pattern
				.compile("^a=rtpmap:(\\d+) ISAC/16000[\r]?$");
		for (int i = 0; (i < lines.length)
				&& (mLineIndex == -1 || isac16kRtpMap == null); ++i) {
			if (lines[i].startsWith("m=audio ")) {
				mLineIndex = i;
				continue;
			}
			Matcher isac16kMatcher = isac16kPattern.matcher(lines[i]);
			if (isac16kMatcher.matches()) {
				isac16kRtpMap = isac16kMatcher.group(1);
				continue;
			}
		}
		if (mLineIndex == -1) {
			Gdx.app.log(AndroidRTCPeerConnection.class.getName(), "No m=audio line, so can't prefer iSAC");
			return sdpDescription;
		}
		if (isac16kRtpMap == null) {
			Gdx.app.log(AndroidRTCPeerConnection.class.getName(), "No ISAC/16000 line, so can't prefer iSAC");
			return sdpDescription;
		}
		String[] origMLineParts = lines[mLineIndex].split(" ");
		StringBuilder newMLine = new StringBuilder();
		int origPartIndex = 0;
		newMLine.append(origMLineParts[origPartIndex++]).append(" ");
		newMLine.append(origMLineParts[origPartIndex++]).append(" ");
		newMLine.append(origMLineParts[origPartIndex++]).append(" ");
		newMLine.append(isac16kRtpMap);
		for (; origPartIndex < origMLineParts.length; ++origPartIndex) {
			if (!origMLineParts[origPartIndex].equals(isac16kRtpMap)) {
				newMLine.append(" ").append(origMLineParts[origPartIndex]);
			}
		}
		lines[mLineIndex] = newMLine.toString();
		StringBuilder newSdpDescription = new StringBuilder();
		for (String line : lines) {
			newSdpDescription.append(line).append("\r\n");
		}
		return newSdpDescription.toString();
	}

	class CreatePeerConnectionObserver implements Observer {
		@Override
		public void onAddStream(MediaStream arg0) {
		}

		@Override
		public void onDataChannel(DataChannel dataChannel) {
			AndroidRTCPeerConnection.this.dataChannel = dataChannel;
			listener.onDataChannel(new DataChannelEventImpl(dataChannel));
		}

		@Override
		public void onIceCandidate(IceCandidate iceCandidate) {
			if (iceCandidate.sdp != null && !iceCandidate.sdp.trim().equals("")) {
				listener.onIceCandidate(iceCandidate.sdp, iceCandidate.sdpMid,
						iceCandidate.sdpMLineIndex);
			}
		}

		@Override
		public void onIceConnectionChange(IceConnectionState iceConnectionState) {
		}

		@Override
		public void onIceGatheringChange(IceGatheringState arg0) {
		}

		@Override
		public void onRemoveStream(MediaStream arg0) {
		}

		@Override
		public void onSignalingChange(SignalingState arg0) {
		}

		@Override
		public void onRenegotiationNeeded() {
		}

		@Override
		public void onIceConnectionReceivingChange (boolean arg0) {
		}
	}

	class OfferObserver implements SdpObserver {
		@Override
		public void onSetSuccess() {
		}

		@Override
		public void onSetFailure(String error) {
			Gdx.app.log("Webrtc", "OFFER FAILED : " + error);
		}

		@Override
		public void onCreateSuccess(final SessionDescription sessionDescription) {
			pc.setLocalDescription(new LocalDescriptionObserver(), sessionDescription);
			listener.onSetLocalDescription(sessionDescription.description);
		}

		@Override
		public void onCreateFailure(String error) {
			Gdx.app.log("Webrtc", "OFFER FAILED : " + error);
		}
	}

	class LocalDescriptionObserver implements SdpObserver {
		@Override
		public void onSetSuccess() {
		}

		@Override
		public void onSetFailure(String error) {
			Gdx.app.log("Webrtc", "OFFER FAILED : " + error);
		}

		@Override
		public void onCreateSuccess(SessionDescription sessionDescription) {
		}

		@Override
		public void onCreateFailure(String error) {
			Gdx.app.log("Webrtc", "OFFER FAILED : " + error);
		}
	}

	class RemoteDescriptionObserver implements SdpObserver {
		@Override
		public void onSetSuccess() {
			Gdx.app.log("Webrtc", "setRemoteDescription ANSWER onSetSuccess!!!");
			pc.createAnswer(new CreateAnswerObserver(), getConstraints());
		}

		@Override
		public void onSetFailure(String error) {
			Gdx.app.log("Webrtc", "setRemoteDescription ANSWER FAILED : " + error);
		}

		@Override
		public void onCreateSuccess(SessionDescription arg0) {
			Gdx.app.log("Webrtc", "setRemoteDescription ANSWER onCreateSuccess");
		}

		@Override
		public void onCreateFailure(String error) {
			Gdx.app.log("Webrtc", "setRemoteDescription ANSWER CREATE FAILED : " + error);
		}
	}

	class CreateAnswerObserver implements SdpObserver {
		@Override
		public void onSetSuccess() {
			Gdx.app.log("Webrtc", "createAnswer ANSWER onSetSuccess");
		}

		@Override
		public void onSetFailure(String error) {
			Gdx.app.log("Webrtc", "createAnswer ANSWER FAILED : " + error);
		}

		@Override
		public void onCreateSuccess(final SessionDescription sessionDescription) {
			Gdx.app.log("Webrtc", "createAnswer ANSWER onCreateSuccess");
			pc.setLocalDescription(new SdpObserver() {
				@Override
				public void onSetSuccess() {
					Gdx.app.log("Webrtc", "setLocalDescription ANSWER onSetSuccess");
					listener.onSetRemoteDescription(sessionDescription.description);
				}

				@Override
				public void onSetFailure(String error) {
					Gdx.app.log("Webrtc", "setLocalDescription ANSWER FAILED : " + error);
				}

				@Override
				public void onCreateSuccess(SessionDescription sessionDescription) {
					Gdx.app.log("Webrtc", "setLocalDescription ANSWER onCreateSuccess");
				}

				@Override
				public void onCreateFailure(String error) {
					Gdx.app.log("Webrtc", "setLocalDescription ANSWER FAILED : " + error);
				}
			}, sessionDescription);
		}

		@Override
		public void onCreateFailure(String error) {
			Gdx.app.log("Webrtc", "ANSWER FAILED : " + error);
		}
	}
}
