// Auto generated code, do not modify
package rcc.http.callers;

import rcc.http.APICall;

public class GetLogCall extends APICall.Builder<GetLogCall> {
    private GetLogCall() {
        super(ApiSpec.getLog);
    }

    public static GetLogCall create() {
        return new GetLogCall();
    }

    public GetLogCall count(String count) {
        return param("count", count);
    }
}
