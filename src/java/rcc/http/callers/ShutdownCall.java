// Auto generated code, do not modify
package rcc.http.callers;

import rcc.http.APICall;

public class ShutdownCall extends APICall.Builder<ShutdownCall> {
    private ShutdownCall() {
        super(ApiSpec.shutdown);
    }

    public static ShutdownCall create() {
        return new ShutdownCall();
    }

    public ShutdownCall scan(String scan) {
        return param("scan", scan);
    }
}
