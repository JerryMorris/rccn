// Auto generated code, do not modify
package rcc.http.callers;

import rcc.http.APICall;

public class DecodeQRCodeCall extends APICall.Builder<DecodeQRCodeCall> {
    private DecodeQRCodeCall() {
        super(ApiSpec.decodeQRCode);
    }

    public static DecodeQRCodeCall create() {
        return new DecodeQRCodeCall();
    }

    public DecodeQRCodeCall qrCodeBase64(String qrCodeBase64) {
        return param("qrCodeBase64", qrCodeBase64);
    }
}
