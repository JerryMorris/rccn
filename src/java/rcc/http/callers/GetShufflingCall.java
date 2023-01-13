// Auto generated code, do not modify
package rcc.http.callers;

import rcc.http.APICall;

public class GetShufflingCall extends APICall.Builder<GetShufflingCall> {
    private GetShufflingCall() {
        super(ApiSpec.getShuffling);
    }

    public static GetShufflingCall create() {
        return new GetShufflingCall();
    }

    public GetShufflingCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetShufflingCall includeHoldingInfo(boolean includeHoldingInfo) {
        return param("includeHoldingInfo", includeHoldingInfo);
    }

    public GetShufflingCall shuffling(String shuffling) {
        return param("shuffling", shuffling);
    }

    public GetShufflingCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
