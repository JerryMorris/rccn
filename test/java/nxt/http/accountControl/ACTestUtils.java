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

package rcc.http.accountControl;

import rcc.http.APICall;
import rcc.http.callers.ApiSpec;
import rcc.http.callers.CreateTransactionCallBuilder;
import rcc.http.callers.IssueAssetCall;
import rcc.http.monetarysystem.TestCurrencyIssuance;
import rcc.util.Logger;
import org.json.simple.JSONObject;
import org.junit.Assert;

public class ACTestUtils {

    public static class Builder extends CreateTransactionCallBuilder<Builder> {
        public Builder(ApiSpec requestType, String secretPhrase) {
            super(requestType);
            secretPhrase(secretPhrase);
            feeNQT(0);
        }
    }
    
    public static class CurrencyBuilder extends TestCurrencyIssuance.Builder {
        public CurrencyBuilder() {
            params.remove("minReservePerUnitNQT");
            params.remove("minDifficulty");
            params.remove("maxDifficulty");
            params.remove("algorithm");
        }
    }

    public static class AssetBuilder {
        private final IssueAssetCall builder = IssueAssetCall.create();

        public AssetBuilder(String secretPhrase, String assetName) {
            builder.param("name", assetName);
            builder.param("description", "Unit tests asset");
            builder.param("quantityQNT", 10000);
            builder.param("decimals", 4);
            builder.secretPhrase(secretPhrase);
            builder.feeNQT(0);
        }

        public IssueAssetCall getBuilder() {
            return builder;
        }
    }
    
    public static JSONObject assertTransactionSuccess(APICall.Builder builder) {
        JSONObject response = builder.build().invoke();
        
        Logger.logMessage(builder.getParam("requestType") + " response: " + response.toJSONString());
        Assert.assertNull(response.get("error"));
        String result = (String) response.get("transaction");
        Assert.assertNotNull(result);
        return response;
    }
    
    public static void assertTransactionBlocked(APICall.Builder builder) {
        JSONObject response = builder.build().invoke();
        
        Logger.logMessage(builder.getParam("requestType") + " response: " + response.toJSONString());
        
        //Assert.assertNotNull("Transaction wasn't even created", response.get("transaction"));
        
        String errorMsg = (String) response.get("error");
        Assert.assertNotNull("Transaction should fail, but didn't", errorMsg);
        Assert.assertTrue(errorMsg.contains("rcc.rccException$AccountControlException"));
    }
    
    public static long getAccountBalance(long account, String balance) {
        APICall.Builder builder = new APICall.Builder("getBalance").param("account", Long.toUnsignedString(account));
        JSONObject response = builder.build().invoke();
        
        Logger.logMessage("getBalance response: " + response.toJSONString());
        
        return Long.parseLong(((String)response.get(balance)));
    }
}
