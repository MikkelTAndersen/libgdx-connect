package org.plugination.connect.html.webrtc;

import org.plugination.connect.core.webrtc.RTCDataChannelEvent;
import org.plugination.connect.core.webrtc.RTCDataChannelState;

import com.badlogic.gdx.Gdx;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.NativeEvent;
import com.seanchenxi.gwt.html.client.event.CloseEvent;
import com.seanchenxi.gwt.html.client.event.DataChannelEvent;
import com.seanchenxi.gwt.html.client.event.ErrorEvent;
import com.seanchenxi.gwt.html.client.event.IceCandidateEvent;
import com.seanchenxi.gwt.html.client.event.MessageEvent;
import com.seanchenxi.gwt.html.client.event.MessageEvent.Handler;
import com.seanchenxi.gwt.webrtc.client.Constraint;
import com.seanchenxi.gwt.webrtc.client.Constraints;
import com.seanchenxi.gwt.webrtc.client.WebRTC;
import com.seanchenxi.gwt.webrtc.client.connection.RTCConfiguration;
import com.seanchenxi.gwt.webrtc.client.connection.RTCIceCandidate;
import com.seanchenxi.gwt.webrtc.client.connection.RTCIceCandidateInit;
import com.seanchenxi.gwt.webrtc.client.connection.RTCIceServer;
import com.seanchenxi.gwt.webrtc.client.connection.RTCPeerConnection;
import com.seanchenxi.gwt.webrtc.client.connection.RTCSdpType;
import com.seanchenxi.gwt.webrtc.client.connection.RTCSessionDescription;
import com.seanchenxi.gwt.webrtc.client.connection.RTCSessionDescriptionCallback;
import com.seanchenxi.gwt.webrtc.client.connection.RTCSessionDescriptionInit;
import com.seanchenxi.gwt.webrtc.client.data.DataChannel;

public class HtmlRTCPeerConnection implements org.plugination.connect.core.webrtc.RTCPeerConnection {
	DataChannel dataChannel;
	private RTCPeerConnection pc;
	private EventListener listener;
	private Constraints constraints;

	public HtmlRTCPeerConnection(final EventListener listener) {
        Constraints constraints = getPCConstraints();
        pc = WebRTC.createRTCPeerConnection(getPCConfiguration(), constraints);
		this.listener = listener;
		this.constraints = constraints;
		pc.addDataChannelHandler(new DataChannelEvent.Handler() {
			@Override
			public void onDataChannel(DataChannelEvent event) {
				final DataChannel gwtChannel = event.getChannel();
				HtmlRTCPeerConnection.this.dataChannel = gwtChannel;
				Gdx.app.postRunnable(new Runnable() {
					@Override
					public void run() {
						listener.onDataChannel(new RTCDataChannelEventImpl(gwtChannel));
					}
				});
			}
		});
		pc.addIceCandidateHandler(new IceCandidateEvent.Handler() {
			@Override
			public void onIceCandidate(IceCandidateEvent event) {
				final RTCIceCandidate candidate = event.getCandidate();
				if(candidate != null) {
					Gdx.app.postRunnable(new Runnable() {
						@Override
						public void run() {
							listener.onIceCandidate(candidate.getCandidate(),candidate.getSdpMid(), candidate.getSdpMLineIndex());
						}
					});
				}
			}
		});
	}

    protected RTCConfiguration getPCConfiguration() {
        JsArray<RTCIceServer> iceServers = JavaScriptObject.createArray().cast();
        if (WebRTC.isGecko()) {
            iceServers.push(WebRTC.createRTCIceServer("stun:stun.services.mozilla.com"));
        } else if (WebRTC.isWebkit()) {
        	iceServers.push(WebRTC.createRTCIceServer("stun:stun.l.google.com:19302"));
        	iceServers.push(WebRTC.createRTCIceServer("stun:stun1.l.google.com:19302"));
        	iceServers.push(WebRTC.createRTCIceServer("stun:stun2.l.google.com:19302"));
        	iceServers.push(WebRTC.createRTCIceServer("stun:stun3.l.google.com:19302"));
        	iceServers.push(WebRTC.createRTCIceServer("stun:stun4.l.google.com:19302"));
        }
        return WebRTC.createRTCConfiguration(iceServers);
    }

	protected Constraints getPCConstraints() {
        final Constraints constraints = Constraints.create();
		Constraint dtls = Constraint.create();

		Gdx.app.log("Webrtc", "CONSTRAINT_OPTIONAL_DTLSSRTPKEYAGREEMENT: true");
		dtls.set(com.seanchenxi.gwt.webrtc.client.connection.RTCPeerConnection.CONSTRAINT_OPTIONAL_DTLSSRTPKEYAGREEMENT, true);
        constraints.getOptional().push(dtls);

        Constraint rtpdatachannels = Constraint.create();
        Gdx.app.log("Webrtc", "CONSTRAINT_OPTIONAL_RTPDATACHANNELS: false");
        rtpdatachannels.set(com.seanchenxi.gwt.webrtc.client.connection.RTCPeerConnection.CONSTRAINT_OPTIONAL_RTPDATACHANNELS, false);
        constraints.getOptional().push(rtpdatachannels);

        Constraint mandatory = Constraint.create();
        mandatory.set(com.seanchenxi.gwt.webrtc.client.connection.RTCPeerConnection.CONSTRAINT_MANDATORY_OFFERTORECEIVEAUDIO, true);
        mandatory.set(com.seanchenxi.gwt.webrtc.client.connection.RTCPeerConnection.CONSTRAINT_MANDATORY_OFFERTORECEIVEVIDEO, false);
        constraints.setMandatory(mandatory);
        return constraints;
    }

	@Override
	public void close() {
		pc.close();
	}

	public RTCDataChannel createOffer() {
		dataChannel = pc.createDataChannel("sendDataCh",
				WebRTC.createDataChannelInit(true));
		pc.createOffer(new RTCSessionDescriptionCallback() {
			@Override
			public void onSuccess(final RTCSessionDescription sessionDescription) {
				hackToFixChromeTransmitSizeIssue(sessionDescription);
				Gdx.app.postRunnable(new Runnable() {
					@Override
					public void run() {
						pc.setLocalDescription(sessionDescription);
						listener.onSetLocalDescription(sessionDescription.getSdp());
					}
				});

//				Gdx.app.log("Webrtc", "OFFER COMPLETED");
			}

			@Override
			public void onError(String error) {
				Gdx.app.log("Webrtc", "OFFER FAILED : " + error);
			}
		}, constraints);
		return new RTCDataChannelEventImpl(dataChannel).getChannel();
	}

	@Override
	public String getLocalDescription() {
		RTCSessionDescription localDescription = pc.getLocalDescription();
		return localDescription.getSdp();
	}

	@Override
	public void createAnswer(String sdp) {
		RTCSessionDescriptionInit descriptionInit = WebRTC
				.createRTCSessionDescriptionInit(RTCSdpType.OFFER, sdp);
		final RTCSessionDescription sessionDesc = WebRTC
				.createRTCSessionDescription(descriptionInit);
		pc.setRemoteDescription(sessionDesc);
		Gdx.app.postRunnable(new Runnable() {
			@Override
			public void run() {
				pc.createAnswer(new RTCSessionDescriptionCallback() {
					@Override
					public void onSuccess(RTCSessionDescription sessionDescription) {
						hackToFixChromeTransmitSizeIssue(sessionDescription);
						pc.setLocalDescription(sessionDescription);
						listener.onSetRemoteDescription(sessionDescription.getSdp());
//						Gdx.app.log("Webrtc", "ANSWER COMPLETED");
					}

					@Override
					public void onError(String error) {
						Gdx.app.log("Webrtc", "ANSWER FAILED : " + error);
					}
				}, constraints);
			}
		});
	}

	private void hackToFixChromeTransmitSizeIssue(RTCSessionDescription sessionDescription) {
		String sdp = sessionDescription.getSdp();
		String[] splitted = sdp.split("b=AS:30");
		if(splitted != null && splitted[1] != null) {
			String newSDP = splitted[0] + "b=AS:1638400" + splitted[1];
			sessionDescription.setSdp(newSDP);
		}
	}

	@Override
	public void addIceCandidate(String candidate, String sdpMid,
			int sdpMLineIndex) {
		if (candidate != null) {
//			Gdx.app.log("Webrtc", candidate + " " + sdpMid + " " + sdpMLineIndex);
			try {
				RTCIceCandidateInit iceCandidateInit = WebRTC
					.createRTCIceCandidateInit(candidate, sdpMid, sdpMLineIndex);
				RTCIceCandidate iceCandidate = WebRTC
					.createRTCIceCandidate(iceCandidateInit);
				if (iceCandidate != null) {
					pc.addIceCandidate(iceCandidate);
				}
			} catch(Exception e) {
				Gdx.app.log("Webrtc could not add ice", candidate + " " + sdpMid + " " + sdpMLineIndex);
			}
		}
	}

	@Override
	public void setLocalDescription(String sessionDescription) {
		RTCSessionDescriptionInit descriptionInit = WebRTC
				.createRTCSessionDescriptionInit(RTCSdpType.OFFER,
						sessionDescription);
		RTCSessionDescription sdp = WebRTC.createRTCSessionDescription(descriptionInit);
		pc.setLocalDescription(sdp);
	}

	@Override
	public String getRemoteDescription() {
		return pc.getRemoteDescription().getSdp();
	}

	@Override
	public void setRemoteDescription(String sessionDescription) {
		RTCSessionDescriptionInit descriptionInit = WebRTC
				.createRTCSessionDescriptionInit(RTCSdpType.ANSWER,
						sessionDescription);
		RTCSessionDescription sdp = WebRTC
				.createRTCSessionDescription(descriptionInit);
		pc.setRemoteDescription(sdp);
	}

	private class RTCDataChannelEventImpl implements RTCDataChannelEvent {
		private final DataChannel gwtChannel;
		private RTCDataChannel rtcDataChannel;

		public RTCDataChannelEventImpl(DataChannel channel) {
			this.gwtChannel = channel;
		    rtcDataChannel = new RTCDataChannel(){
				@Override
				public void addEventListener(final EventListener dataListener) {
//					Gdx.app.log("Webrtc", "ADDED LISTENERS " + gwtChannel.getReadyState());
					gwtChannel.addMessageHandler(new Handler() {
						@Override
						public void onMessage(final MessageEvent event) {
//							Gdx.app.log("Webrtc", "onMessage LISTENERS");
							final String data = (String) event.getData();
							Gdx.app.postRunnable(new Runnable() {
								@Override
								public void run() {
									dataListener.onMessage(data);
								}
							});
						}
					});
					gwtChannel.addCloseHandler(new CloseEvent.Handler() {
						@Override
						public void onClose(CloseEvent event) {
							Gdx.app.postRunnable(new Runnable() {
								@Override
								public void run() {
									dataListener.onClose();
								}
							});
						}
					});
					gwtChannel.addOpenHandler(new com.seanchenxi.gwt.html.client.event.OpenEvent.Handler(){
						@Override
						public void onOpen(com.seanchenxi.gwt.html.client.event.OpenEvent event) {
//							Gdx.app.log("Webrtc", "addOpenHandler LISTENERS");
							Gdx.app.postRunnable(new Runnable() {
								@Override
								public void run() {
									dataListener.onOpen();
								}
							});
						}
					});
					gwtChannel.addErrorHandler(new ErrorEvent.Handler<NativeEvent>(){
						@Override
						public void onError(final ErrorEvent<NativeEvent> event) {
//							Gdx.app.log("Webrtc", "onError LISTENERS " +event.toDebugString());
							final String debugString = event.toDebugString();
							Gdx.app.postRunnable(new Runnable() {
								@Override
								public void run() {
									dataListener.onError(debugString);
								}
							});
						}});
				}

				@Override
				public void send(final String data) {
					gwtChannel.send(data);
				}

				@Override
				public void close() {
					gwtChannel.close();
				}

				@Override
				public RTCDataChannelState getState() {
					switch (gwtChannel.getReadyState()) {
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
}
