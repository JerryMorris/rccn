// Auto generated code, do not modify
package rcc.http.callers;

import rcc.http.APICall;

public class ManagePeersNetworkingCall extends APICall.Builder<ManagePeersNetworkingCall> {
    private ManagePeersNetworkingCall() {
        super(ApiSpec.managePeersNetworking);
    }

    public static ManagePeersNetworkingCall create() {
        return new ManagePeersNetworkingCall();
    }

    public ManagePeersNetworkingCall operation(String operation) {
        return param("operation", operation);
    }
}
