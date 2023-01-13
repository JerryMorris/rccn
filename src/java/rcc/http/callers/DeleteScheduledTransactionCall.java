// Auto generated code, do not modify
package rcc.http.callers;

import rcc.http.APICall;

public class DeleteScheduledTransactionCall extends APICall.Builder<DeleteScheduledTransactionCall> {
    private DeleteScheduledTransactionCall() {
        super(ApiSpec.deleteScheduledTransaction);
    }

    public static DeleteScheduledTransactionCall create() {
        return new DeleteScheduledTransactionCall();
    }

    public DeleteScheduledTransactionCall transaction(String transaction) {
        return param("transaction", transaction);
    }

    public DeleteScheduledTransactionCall transaction(long transaction) {
        return unsignedLongParam("transaction", transaction);
    }
}
