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

import rcc.CurrencyExchangeOffer;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAvailableToBuy extends APIServlet.APIRequestHandler {

    static final GetAvailableToBuy instance = new GetAvailableToBuy();

    private GetAvailableToBuy() {
        super(new APITag[] {APITag.MS}, "currency", "units");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {

        long currencyId = ParameterParser.getUnsignedLong(req, "currency", true);
        long units = ParameterParser.getLong(req, "units", 1L, Long.MAX_VALUE, true);
        CurrencyExchangeOffer.AvailableOffers availableOffers = CurrencyExchangeOffer.getAvailableToBuy(currencyId, units);
        return JSONData.availableOffers(availableOffers);
    }

}
