// Auto generated code, do not modify
package rcc.http.callers;

import rcc.http.APICall;

public class GetAllBroadcastedTransactionsCall extends APICall.Builder<GetAllBroadcastedTransactionsCall> {
    private GetAllBroadcastedTransactionsCall() {
        super(ApiSpec.getAllBroadcastedTransactions);
    }

    public static GetAllBroadcastedTransactionsCall create() {
        return new GetAllBroadcastedTransactionsCall();
    }

    public GetAllBroadcastedTransactionsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAllBroadcastedTransactionsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
