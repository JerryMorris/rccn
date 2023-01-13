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

package rcc.http.twophased;

import rcc.BlockchainTest;
import rcc.Constants;
import rcc.VoteWeighting;
import rcc.http.APICall;
import rcc.util.Convert;
import rcc.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestGetAssetPhasedTransactions extends BlockchainTest {
    private String asset;

    private APICall phasedTransactionsApiCall() {
        return new APICall.Builder("getAssetPhasedTransactions")
                .param("asset", asset)
                .param("firstIndex", 0)
                .param("lastIndex", 10)
                .build();
    }

    private APICall byAssetApiCall() {
        return new TestCreateTwoPhased.TwoPhasedMoneyTransferBuilder()
                .votingModel(VoteWeighting.VotingModel.ASSET.getCode())
                .holding(Convert.parseUnsignedLong(asset))
                .minBalance(1, VoteWeighting.MinBalanceModel.ASSET.getCode())
                .fee(21 * Constants.ONE_rcc)
                .build();
    }

    @Before
    public void setUpTest() {
        JSONObject issueAssetResult = new APICall.Builder("issueAsset").
                param("secretPhrase", BOB.getSecretPhrase()).
                param("name", "AliceAsset").
                param("description", "AliceAssetDescription").
                param("quantityQNT", 1000).
                param("decimals", 0).
                param("feeNQT", 1000 * Constants.ONE_rcc).
                build().invokeNoError();
        generateBlock();

        asset = (String) issueAssetResult.get("transaction");
    }

    @Test
    public void simpleTransactionLookup() {
        APICall apiCall = byAssetApiCall();
        JSONObject transactionJSON = TestCreateTwoPhased.issueCreateTwoPhasedSuccess(apiCall);

        JSONObject response = phasedTransactionsApiCall().invokeNoError();
        Logger.logMessage("getAssetPhasedTransactionsResponse:" + response.toJSONString());
        JSONArray transactionsJson = (JSONArray) response.get("transactions");
        Assert.assertTrue(TwoPhasedSuite.searchForTransactionId(transactionsJson, (String) transactionJSON.get("transaction")));
    }

    @Test
    public void sorting() {
        for (int i = 0; i < 15; i++) {
            APICall apiCall = byAssetApiCall();
            TestCreateTwoPhased.issueCreateTwoPhasedSuccess(apiCall);
        }

        JSONObject response = phasedTransactionsApiCall().invokeNoError();
        Logger.logMessage("getAssetPhasedTransactionsResponse:" + response.toJSONString());
        JSONArray transactionsJson = (JSONArray) response.get("transactions");

        //sorting check
        int prevHeight = Integer.MAX_VALUE;
        for (Object transactionsJsonObj : transactionsJson) {
            JSONObject transactionObject = (JSONObject) transactionsJsonObj;
            int height = ((Long) transactionObject.get("height")).intValue();
            Assert.assertTrue(height <= prevHeight);
            prevHeight = height;
        }
    }
}
