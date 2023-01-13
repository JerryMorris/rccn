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
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetECBlock extends APIServlet.APIRequestHandler {

    static final GetECBlock instance = new GetECBlock();

    private GetECBlock() {
        super(new APITag[] {APITag.BLOCKS}, "timestamp");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws rccException {
        int timestamp = ParameterParser.getTimestamp(req);
        if (timestamp == 0) {
            timestamp = rcc.getEpochTime();
        }
        Block ecBlock = rcc.getBlockchain().getECBlock(timestamp);
        JSONObject response = new JSONObject();
        response.put("ecBlockId", ecBlock.getStringId());
        response.put("ecBlockHeight", ecBlock.getHeight());
        response.put("timestamp", timestamp);
        return response;
    }

}