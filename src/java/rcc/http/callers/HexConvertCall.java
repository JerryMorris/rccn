// Auto generated code, do not modify
package rcc.http.callers;

import rcc.http.APICall;

public class HexConvertCall extends APICall.Builder<HexConvertCall> {
    private HexConvertCall() {
        super(ApiSpec.hexConvert);
    }

    public static HexConvertCall create() {
        return new HexConvertCall();
    }

    public HexConvertCall string(String string) {
        return param("string", string);
    }
}
