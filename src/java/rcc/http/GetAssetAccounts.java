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

import rcc.Account;
import rcc.rccException;
import rcc.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAssetAccounts extends APIServlet.APIRequestHandler {

    static final GetAssetAccounts instance = new GetAssetAccounts();

    private GetAssetAccounts() {
        super(new APITag[] {APITag.AE}, "asset", "height", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws rccException {

        long assetId = ParameterParser.getUnsignedLong(req, "asset", true);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        int height = ParameterParser.getHeight(req);

        JSONArray accountAssets = new JSONArray();
        try (DbIterator<Account.AccountAsset> iterator = Account.getAssetAccounts(assetId, height, firstIndex, lastIndex)) {
            while (iterator.hasNext()) {
                Account.AccountAsset accountAsset = iterator.next();
                accountAssets.add(JSONData.accountAsset(accountAsset, true, false));
            }
        }

        JSONObject response = new JSONObject();
        response.put("accountAssets", accountAssets);
        return response;

    }

}
