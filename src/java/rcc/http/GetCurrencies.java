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

import rcc.Currency;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static rcc.http.JSONResponses.UNKNOWN_CURRENCY;

public final class GetCurrencies extends APIServlet.APIRequestHandler {

    static final GetCurrencies instance = new GetCurrencies();

    private GetCurrencies() {
        super(new APITag[] {APITag.MS}, "currencies", "currencies", "currencies", "includeCounts"); // limit to 3 for testing
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        long[] currencyIds = ParameterParser.getUnsignedLongs(req, "currencies");
        boolean includeCounts = "true".equalsIgnoreCase(req.getParameter("includeCounts"));
        JSONObject response = new JSONObject();
        JSONArray currenciesJSONArray = new JSONArray();
        response.put("currencies", currenciesJSONArray);
        for (long currencyId : currencyIds) {
            Currency currency = Currency.getCurrency(currencyId);
            if (currency == null) {
                return UNKNOWN_CURRENCY;
            }
            currenciesJSONArray.add(JSONData.currency(currency, includeCounts));
        }
        return response;
    }

}
