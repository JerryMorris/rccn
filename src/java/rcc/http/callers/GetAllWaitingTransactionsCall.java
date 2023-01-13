// Auto generated code, do not modify
package rcc.http.callers;

import rcc.http.APICall;

public class GetAllWaitingTransactionsCall extends APICall.Builder<GetAllWaitingTransactionsCall> {
    private GetAllWaitingTransactionsCall() {
        super(ApiSpec.getAllWaitingTransactions);
    }

    public static GetAllWaitingTransactionsCall create() {
        return new GetAllWaitingTransactionsCall();
    }

    public GetAllWaitingTransactionsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAllWaitingTransactionsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
