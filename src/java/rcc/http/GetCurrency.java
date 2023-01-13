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
import rcc.rccException;
import rcc.util.Convert;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static rcc.http.JSONResponses.MISSING_CURRENCY;
import static rcc.http.JSONResponses.UNKNOWN_CURRENCY;

public final class GetCurrency extends APIServlet.APIRequestHandler {

    static final GetCurrency instance = new GetCurrency();

    private GetCurrency() {
        super(new APITag[] {APITag.MS}, "currency", "code", "includeCounts");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws rccException {
        boolean includeCounts = "true".equalsIgnoreCase(req.getParameter("includeCounts"));
        long currencyId = ParameterParser.getUnsignedLong(req, "currency", false);
        Currency currency;
        if (currencyId == 0) {
            String currencyCode = Convert.emptyToNull(req.getParameter("code"));
            if (currencyCode == null) {
                return MISSING_CURRENCY;
            }
            currency = Currency.getCurrencyByCode(currencyCode);
        } else {
            currency = Currency.getCurrency(currencyId);
        }
        if (currency == null) {
            throw new ParameterException(UNKNOWN_CURRENCY);
        }
        return JSONData.currency(currency, includeCounts);
    }

}
