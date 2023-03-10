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

package rcc.addons;

import rcc.http.APITag;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import java.io.BufferedReader;

public class FundingMonitorsEncryptedConfig extends AbstractEncryptedConfig {
    @Override
    protected String getAPIRequestName() {
        return "FundingMonitors";
    }

    @Override
    protected APITag getAPITag() {
        return APITag.ACCOUNTS;
    }

    @Override
    protected String getDataParameter() {
        return "monitors";
    }

    @Override
    protected JSONStreamAware processDecrypted(BufferedReader reader) {
        JSONArray monitors = StartFundingMonitors.startFundingMonitors(JO.parse(reader));
        JSONObject response = new JSONObject();
        response.put("monitors", monitors);
        return response;
    }
}