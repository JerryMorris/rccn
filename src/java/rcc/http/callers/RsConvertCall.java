// Auto generated code, do not modify
package rcc.http.callers;

import rcc.http.APICall;

public class RsConvertCall extends APICall.Builder<RsConvertCall> {
    private RsConvertCall() {
        super(ApiSpec.rsConvert);
    }

    public static RsConvertCall create() {
        return new RsConvertCall();
    }

    public RsConvertCall account(String account) {
        return param("account", account);
    }

    public RsConvertCall account(long account) {
        return unsignedLongParam("account", account);
    }
}
