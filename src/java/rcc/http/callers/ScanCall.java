// Auto generated code, do not modify
package rcc.http.callers;

import rcc.http.APICall;

public class ScanCall extends APICall.Builder<ScanCall> {
    private ScanCall() {
        super(ApiSpec.scan);
    }

    public static ScanCall create() {
        return new ScanCall();
    }

    public ScanCall numBlocks(String numBlocks) {
        return param("numBlocks", numBlocks);
    }

    public ScanCall validate(boolean validate) {
        return param("validate", validate);
    }

    public ScanCall height(int height) {
        return param("height", height);
    }
}
