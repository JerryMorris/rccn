// Auto generated code, do not modify
package rcc.http.callers;

import rcc.http.APICall;

public class GetLinkedPhasedTransactionsCall extends APICall.Builder<GetLinkedPhasedTransactionsCall> {
    private GetLinkedPhasedTransactionsCall() {
        super(ApiSpec.getLinkedPhasedTransactions);
    }

    public static GetLinkedPhasedTransactionsCall create() {
        return new GetLinkedPhasedTransactionsCall();
    }

    public GetLinkedPhasedTransactionsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetLinkedPhasedTransactionsCall linkedFullHash(String linkedFullHash) {
        return param("linkedFullHash", linkedFullHash);
    }

    public GetLinkedPhasedTransactionsCall linkedFullHash(byte[] linkedFullHash) {
        return param("linkedFullHash", linkedFullHash);
    }

    public GetLinkedPhasedTransactionsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
