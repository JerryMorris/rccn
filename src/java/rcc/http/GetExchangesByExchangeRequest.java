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

import rcc.Exchange;
import rcc.db.DbIterator;
import rcc.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static rcc.http.JSONResponses.MISSING_TRANSACTION;

public final class GetExchangesByExchangeRequest extends APIServlet.APIRequestHandler {

    static final GetExchangesByExchangeRequest instance = new GetExchangesByExchangeRequest();

    private GetExchangesByExchangeRequest() {
        super(new APITag[] {APITag.MS}, "transaction", "includeCurrencyInfo");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {
        String transactionIdString = Convert.emptyToNull(req.getParameter("transaction"));
        if (transactionIdString == null) {
            return MISSING_TRANSACTION;
        }
        long transactionId = Convert.parseUnsignedLong(transactionIdString);
        boolean includeCurrencyInfo = "true".equalsIgnoreCase(req.getParameter("includeCurrencyInfo"));
        JSONObject response = new JSONObject();
        JSONArray exchangesData = new JSONArray();
        try (DbIterator<Exchange> exchanges = Exchange.getExchanges(transactionId)) {
            while (exchanges.hasNext()) {
                exchangesData.add(JSONData.exchange(exchanges.next(), includeCurrencyInfo));
            }
        }
        response.put("exchanges", exchangesData);
        return response;
    }

}
