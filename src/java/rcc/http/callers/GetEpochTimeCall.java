// Auto generated code, do not modify
package rcc.http.callers;

import rcc.http.APICall;

public class GetEpochTimeCall extends APICall.Builder<GetEpochTimeCall> {
    private GetEpochTimeCall() {
        super(ApiSpec.getEpochTime);
    }

    public static GetEpochTimeCall create() {
        return new GetEpochTimeCall();
    }

    public GetEpochTimeCall unixtime(String unixtime) {
        return param("unixtime", unixtime);
    }
}
