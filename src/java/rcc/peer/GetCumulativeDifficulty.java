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

package rcc.peer;

import rcc.Block;
import rcc.rcc;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

final class GetCumulativeDifficulty extends PeerServlet.PeerRequestHandler {

    static final GetCumulativeDifficulty instance = new GetCumulativeDifficulty();

    private GetCumulativeDifficulty() {}


    @Override
    JSONStreamAware processRequest(JSONObject request, Peer peer) {

        JSONObject response = new JSONObject();

        Block lastBlock = rcc.getBlockchain().getLastBlock();
        response.put("cumulativeDifficulty", lastBlock.getCumulativeDifficulty().toString());
        response.put("blockchainHeight", lastBlock.getHeight());
        return response;
    }

    @Override
    boolean rejectWhileDownloading() {
        return true;
    }

}
