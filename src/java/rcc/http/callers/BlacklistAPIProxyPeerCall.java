// Auto generated code, do not modify
package rcc.http.callers;

import rcc.http.APICall;

public class BlacklistAPIProxyPeerCall extends APICall.Builder<BlacklistAPIProxyPeerCall> {
    private BlacklistAPIProxyPeerCall() {
        super(ApiSpec.blacklistAPIProxyPeer);
    }

    public static BlacklistAPIProxyPeerCall create() {
        return new BlacklistAPIProxyPeerCall();
    }

    public BlacklistAPIProxyPeerCall peer(String peer) {
        return param("peer", peer);
    }
}
