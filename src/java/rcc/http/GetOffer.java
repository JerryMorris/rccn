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
import rcc.CurrencySellOffer;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetOffer extends APIServlet.APIRequestHandler {

    static final GetOffer instance = new GetOffer();

    private GetOffer() {
        super(new APITag[] {APITag.MS}, "offer");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        JSONObject response = new JSONObject();
        CurrencyBuyOffer buyOffer = ParameterParser.getBuyOffer(req);
        CurrencySellOffer sellOffer = ParameterParser.getSellOffer(req);
        response.put("buyOffer", JSONData.offer(buyOffer));
        response.put("sellOffer", JSONData.offer(sellOffer));
        return response;
    }

}
