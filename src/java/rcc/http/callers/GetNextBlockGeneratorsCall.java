// Auto generated code, do not modify
package rcc.http.callers;

import rcc.http.APICall;

public class GetNextBlockGeneratorsCall extends APICall.Builder<GetNextBlockGeneratorsCall> {
    private GetNextBlockGeneratorsCall() {
        super(ApiSpec.getNextBlockGenerators);
    }

    public static GetNextBlockGeneratorsCall create() {
        return new GetNextBlockGeneratorsCall();
    }

    public GetNextBlockGeneratorsCall limit(String limit) {
        return param("limit", limit);
    }
}
