/*
 * Copyright © 2013-2016 The rcc Core Developers.
 * Copyright © 2016-2022 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of the rcc software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package rcc.http;

import rcc.util.Convert;
import rcc.util.JSON;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;

public final class LongConvert extends APIServlet.APIRequestHandler {

    static final LongConvert instance = new LongConvert();

    private LongConvert() {
        super(new APITag[] {APITag.UTILS}, "id");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {
        String id = Convert.emptyToNull(req.getParameter("id"));
        if (id == null) {
            return JSON.emptyJSON;
        }
        JSONObject response = new JSONObject();
        BigInteger bigInteger = new BigInteger(id);
        if (bigInteger.signum() < 0) {
            if (bigInteger.negate().compareTo(Convert.two64) > 0) {
                return JSONResponses.OVERFLOW;
            } else {
                response.put("stringId", bigInteger.add(Convert.two64).toString());
                response.put("longId", String.valueOf(bigInteger.longValue()));
            }
        } else {
            if (bigInteger.compareTo(Convert.two64) >= 0) {
                return JSONResponses.OVERFLOW;
            } else {
                response.put("stringId", bigInteger.toString());
                response.put("longId", String.valueOf(bigInteger.longValue()));
            }
        }
        return response;
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

    @Override
    protected boolean requireBlockchain() {
        return false;
    }

}
