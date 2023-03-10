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

import rcc.CurrencyBuyOffer;
import rcc.db.DbIterator;
import rcc.db.DbUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetBuyOffers extends APIServlet.APIRequestHandler {

    static final GetBuyOffers instance = new GetBuyOffers();

    private GetBuyOffers() {
        super(new APITag[] {APITag.MS}, "currency", "account", "availableOnly", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {

        long currencyId = ParameterParser.getUnsignedLong(req, "currency", false);
        long accountId = ParameterParser.getAccountId(req, false);
        if (currencyId == 0 && accountId == 0) {
            return JSONResponses.MISSING_CURRENCY_ACCOUNT;
        }
        boolean availableOnly = "true".equalsIgnoreCase(req.getParameter("availableOnly"));

        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        JSONObject response = new JSONObject();
        JSONArray offerData = new JSONArray();
        response.put("offers", offerData);

        DbIterator<CurrencyBuyOffer> offers= null;
        try {
            if (accountId == 0) {
                offers = CurrencyBuyOffer.getCurrencyOffers(currencyId, availableOnly, firstIndex, lastIndex);
            } else if (currencyId == 0) {
                offers = CurrencyBuyOffer.getAccountOffers(accountId, availableOnly, firstIndex, lastIndex);
            } else {
                CurrencyBuyOffer offer = CurrencyBuyOffer.getOffer(currencyId, accountId);
                if (offer != null) {
                    offerData.add(JSONData.offer(offer));
                }
                return response;
            }
            while (offers.hasNext()) {
                offerData.add(JSONData.offer(offers.next()));
            }
        } finally {
            DbUtils.close(offers);
        }

        return response;
    }

}
