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

import rcc.Block;
import rcc.rcc;
import rcc.rccException;
import rcc.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetBlocks extends APIServlet.APIRequestHandler {

    static final GetBlocks instance = new GetBlocks();

    private GetBlocks() {
        super(new APITag[] {APITag.BLOCKS}, "firstIndex", "lastIndex", "timestamp", "includeTransactions", "includeExecutedPhased");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws rccException {

        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        final int timestamp = ParameterParser.getTimestamp(req);
        boolean includeTransactions = "true".equalsIgnoreCase(req.getParameter("includeTransactions"));
        boolean includeExecutedPhased = "true".equalsIgnoreCase(req.getParameter("includeExecutedPhased"));

        JSONArray blocks = new JSONArray();
        try (DbIterator<? extends Block> iterator = rcc.getBlockchain().getBlocks(firstIndex, lastIndex)) {
            while (iterator.hasNext()) {
                Block block = iterator.next();
                if (block.getTimestamp() < timestamp) {
                    break;
                }
                blocks.add(JSONData.block(block, includeTransactions, includeExecutedPhased));
            }
        }

        JSONObject response = new JSONObject();
        response.put("blocks", blocks);

        return response;
    }

}
