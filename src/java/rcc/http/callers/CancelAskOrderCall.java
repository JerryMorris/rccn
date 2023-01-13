// Auto generated code, do not modify
package rcc.http.callers;

public class CancelAskOrderCall extends CreateTransactionCallBuilder<CancelAskOrderCall> {
    private CancelAskOrderCall() {
        super(ApiSpec.cancelAskOrder);
    }

    public static CancelAskOrderCall create() {
        return new CancelAskOrderCall();
    }

    public CancelAskOrderCall order(String order) {
        return param("order", order);
    }

    public CancelAskOrderCall order(long order) {
        return unsignedLongParam("order", order);
    }
}
