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

import rcc.Asset;
import rcc.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAssetIds extends APIServlet.APIRequestHandler {

    static final GetAssetIds instance = new GetAssetIds();

    private GetAssetIds() {
        super(new APITag[] {APITag.AE}, "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {

        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        JSONArray assetIds = new JSONArray();
        try (DbIterator<Asset> assets = Asset.getAllAssets(firstIndex, lastIndex)) {
            while (assets.hasNext()) {
                assetIds.add(Long.toUnsignedString(assets.next().getId()));
            }
        }
        JSONObject response = new JSONObject();
        response.put("assetIds", assetIds);
        return response;
    }

}
