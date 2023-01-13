// Auto generated code, do not modify
package rcc.http.callers;

import rcc.http.APICall;

public class EventWaitCall extends APICall.Builder<EventWaitCall> {
    private EventWaitCall() {
        super(ApiSpec.eventWait);
    }

    public static EventWaitCall create() {
        return new EventWaitCall();
    }

    public EventWaitCall timeout(long timeout) {
        return param("timeout", timeout);
    }

    @Override
    public boolean isRemoteOnly() {
        return true;
    }
}
