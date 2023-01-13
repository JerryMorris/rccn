// Auto generated code, do not modify
package rcc.http.callers;

import rcc.http.APICall;

public class DecodeFileTokenCall extends APICall.Builder<DecodeFileTokenCall> {
    private DecodeFileTokenCall() {
        super(ApiSpec.decodeFileToken);
    }

    public static DecodeFileTokenCall create() {
        return new DecodeFileTokenCall();
    }

    public DecodeFileTokenCall token(String token) {
        return param("token", token);
    }

    public DecodeFileTokenCall file(byte[] b) {
        return parts("file", b);
    }
}
