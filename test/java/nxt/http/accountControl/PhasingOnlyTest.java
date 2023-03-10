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

import rcc.BlockchainTest;
import rcc.Constants;
import rcc.rcc;
import rcc.PhasingParams;
import rcc.VoteWeighting.MinBalanceModel;
import rcc.VoteWeighting.VotingModel;
import rcc.http.APICall;
import rcc.http.APICall.Builder;
import rcc.http.accountControl.ACTestUtils.AssetBuilder;
import rcc.http.callers.SendMoneyCall;
import rcc.util.Convert;
import rcc.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static rcc.http.callers.ApiSpec.approveTransaction;
import static rcc.http.callers.ApiSpec.setPhasingOnlyControl;

public class PhasingOnlyTest extends BlockchainTest {
    @Test
    public void testSetAndGet() throws Exception {
        assertNoPhasingOnlyControl();
        setPhasingOnlyControl(VotingModel.ACCOUNT, null, 1L, null, null, new long[]{BOB.getId()},
                10 * Constants.ONE_rcc, 5, 1440);
        assertPhasingOnly(new PhasingParams(VotingModel.ACCOUNT.getCode(), 0L, 1L, 0L, (byte) 0, new long[]{BOB.getId()}),
                10 * Constants.ONE_rcc, 5, 1440);
    }

    @Test
    public void testAccountVoting() throws Exception {
        //all transactions must be approved either by BOB or CHUCK
        setPhasingOnlyControl(VotingModel.ACCOUNT, null, 1L, null, null, new long[]{BOB.getId(), CHUCK.getId()}, 0, 0, 0);

        Builder builder = SendMoneyCall.create()
                .secretPhrase(ALICE.getSecretPhrase())
                .feeNQT(0)
                .recipient(BOB.getId())
                .amountNQT(Constants.ONE_rcc);

        //no phasing - block
        ACTestUtils.assertTransactionBlocked(builder);

        //correct phasing
        setTransactionPhasingParams(builder, 20, VotingModel.ACCOUNT, null, 1L, null, null, new long[]{BOB.getId(), CHUCK.getId()});
        ACTestUtils.assertTransactionSuccess(builder);

        //subset of the voters should also be blocked
        setTransactionPhasingParams(builder, 20, VotingModel.ACCOUNT, null, 1L, null, null, new long[]{BOB.getId()});
        ACTestUtils.assertTransactionBlocked(builder);

        //incorrect quorum - even if more restrictive, should also be blocked
        setTransactionPhasingParams(builder, 20, VotingModel.ACCOUNT, null, 2L, null, null, new long[]{BOB.getId(), CHUCK.getId()});
        ACTestUtils.assertTransactionBlocked(builder);

        //remove the phasing control
        builder = new ACTestUtils.Builder(setPhasingOnlyControl, ALICE.getSecretPhrase());

        setControlPhasingParams(builder, VotingModel.NONE, null, null, null, null, null, 0, 0, 0);

        setTransactionPhasingParams(builder, 3, VotingModel.ACCOUNT, null, 1L, null, null, new long[]{BOB.getId(), CHUCK.getId()});

        JSONObject removePhasingOnlyJSON = ACTestUtils.assertTransactionSuccess(builder);
        generateBlock();

        assertPhasingOnly(new PhasingParams(VotingModel.ACCOUNT.getCode(), 0L, 1L, 0L, (byte) 0, new long[]{BOB.getId(), CHUCK.getId()}), 0, 0, 0);

        String fullHash = (String) removePhasingOnlyJSON.get("fullHash");

        //approve the remove
        builder = new ACTestUtils.Builder(approveTransaction, BOB.getSecretPhrase())
                .param("transactionFullHash", fullHash);
        ACTestUtils.assertTransactionSuccess(builder);

        generateBlock();

        assertNoPhasingOnlyControl();
    }

    @Test
    public void testExtraRestrictions() throws Exception {
        //all transactions must be approved either by BOB or CHUCK, total fees 5 rcc, min duration 4, max duration 100
        setPhasingOnlyControl(VotingModel.ACCOUNT, null, 1L, null, null, new long[]{BOB.getId(), CHUCK.getId()},
                5 * Constants.ONE_rcc, 4, 100);

        SendMoneyCall builder = SendMoneyCall.create().secretPhrase(ALICE.getSecretPhrase())
                .recipient(BOB.getId())
                .amountNQT(Constants.ONE_rcc)
                .feeNQT(7 * Constants.ONE_rcc);
        // fee too high
        setTransactionPhasingParams(builder, 20, VotingModel.ACCOUNT, null, 1L, null, null, new long[]{BOB.getId(), CHUCK.getId()});
        ACTestUtils.assertTransactionBlocked(builder);

        // fee at the limit
        builder.feeNQT(5 * Constants.ONE_rcc);
        JSONObject response = ACTestUtils.assertTransactionSuccess(builder);
        String fullHash = (String) response.get("fullHash");

        generateBlock();

        // not yet approved, another transaction at max fee should fail
        ACTestUtils.assertTransactionBlocked(builder);

        //approve
        Builder approveBuilder = new ACTestUtils.Builder(approveTransaction, BOB.getSecretPhrase())
                .param("transactionFullHash", fullHash);
        ACTestUtils.assertTransactionSuccess(approveBuilder);
        generateBlock();

        //now can submit next transaction
        response = ACTestUtils.assertTransactionSuccess(builder);
        fullHash = (String) response.get("fullHash");
        generateBlock();

        //approve
        approveBuilder.param("transactionFullHash", fullHash);
        ACTestUtils.assertTransactionSuccess(approveBuilder);
        generateBlock();

        //too long or too short periods should fail
        builder.param("phasingFinishHeight", rcc.getBlockchain().getHeight() + 200);
        ACTestUtils.assertTransactionBlocked(builder);
        builder.param("phasingFinishHeight", rcc.getBlockchain().getHeight() + 3);
        ACTestUtils.assertTransactionBlocked(builder);
        builder.param("phasingFinishHeight", rcc.getBlockchain().getHeight() + 4);
        ACTestUtils.assertTransactionSuccess(builder);

    }

    @Test
    public void testRejectingPendingTransaction() throws Exception {

        Builder builder = SendMoneyCall.create()
                .secretPhrase(ALICE.getSecretPhrase())
                .recipient(BOB.getId())
                .amountNQT(Constants.ONE_rcc)
                .feeNQT(0);

        setTransactionPhasingParams(builder, 4, VotingModel.ACCOUNT, null, 1L, null, null, new long[]{BOB.getId(), CHUCK.getId()});
        JSONObject sendMoneyJSON = ACTestUtils.assertTransactionSuccess(builder);
        generateBlock();

        builder = new ACTestUtils.Builder(setPhasingOnlyControl, ALICE.getSecretPhrase());

        setControlPhasingParams(builder, VotingModel.ACCOUNT, null, 1L, null, null, new long[]{DAVE.getId()}, 0, 0, 0);

        ACTestUtils.assertTransactionSuccess(builder);

        generateBlock();

        long balanceBeforeTransactionRejection = ACTestUtils.getAccountBalance(ALICE.getId(), "unconfirmedBalanceNQT");

        String fullHash = (String) sendMoneyJSON.get("fullHash");

        //approve the pending transaction
        builder = new ACTestUtils.Builder(approveTransaction, BOB.getSecretPhrase())
                .param("transactionFullHash", fullHash);
        ACTestUtils.assertTransactionSuccess(builder);

        generateBlock();

        //the sendMoney finish height
        generateBlock();

        //Transaction is approved - since commit 8b44767 account control is not checked at finish height
        Assert.assertEquals(balanceBeforeTransactionRejection,
                ACTestUtils.getAccountBalance(ALICE.getId(), "unconfirmedBalanceNQT"));
    }

    @Test
    public void testBalanceVoting() {
        setPhasingOnlyControl(VotingModel.NQT, null, 100 * Constants.ONE_rcc, null, null, null, 0, 0, 0);

        Builder builder = SendMoneyCall.create()
                .secretPhrase(ALICE.getSecretPhrase())
                .recipient(BOB.getId())
                .amountNQT(Constants.ONE_rcc)
                .feeNQT(0);

        //no phasing - block
        ACTestUtils.assertTransactionBlocked(builder);

        setTransactionPhasingParams(builder, 20, VotingModel.NQT, null, 100 * Constants.ONE_rcc, null, null, new long[]{DAVE.getId()});
        ACTestUtils.assertTransactionBlocked(builder);

        setTransactionPhasingParams(builder, 20, VotingModel.ACCOUNT, null, 1L, null, null, new long[]{BOB.getId(), CHUCK.getId()});
        ACTestUtils.assertTransactionBlocked(builder);

        setTransactionPhasingParams(builder, 20, VotingModel.NQT, null, 100 * Constants.ONE_rcc + 1, null, null, null);
        ACTestUtils.assertTransactionBlocked(builder);

        builder = SendMoneyCall.create()
                .secretPhrase(ALICE.getSecretPhrase())
                .recipient(BOB.getId())
                .amountNQT(Constants.ONE_rcc)
                .feeNQT(0);

        setTransactionPhasingParams(builder, 20, VotingModel.NQT, null, 100 * Constants.ONE_rcc, null, null, null);
        ACTestUtils.assertTransactionSuccess(builder);
    }

    @Test
    public void testAssetVoting() {
        AssetBuilder assetCallBuilder = new AssetBuilder(ALICE.getSecretPhrase(), "TestAsset");
        String assetId = (String) ACTestUtils.assertTransactionSuccess(assetCallBuilder.getBuilder()).get("transaction");
        generateBlock();

        assetCallBuilder = new AssetBuilder(ALICE.getSecretPhrase(), "TestAsset2");
        String asset2Id = (String) ACTestUtils.assertTransactionSuccess(assetCallBuilder.getBuilder()).get("transaction");
        generateBlock();

        setPhasingOnlyControl(VotingModel.ASSET, assetId, 100L, null, null, null, 0, 0, 0);

        SendMoneyCall builder = SendMoneyCall.create()
                .secretPhrase(ALICE.getSecretPhrase())
                .recipient(BOB.getId())
                .amountNQT(Constants.ONE_rcc)
                .feeNQT(0);
        ACTestUtils.assertTransactionBlocked(builder);

        setTransactionPhasingParams(builder, 20, VotingModel.ASSET, asset2Id, 100L, null, null, null);
        ACTestUtils.assertTransactionBlocked(builder);

        setTransactionPhasingParams(builder, 20, VotingModel.ASSET, assetId, 100L, null, null, null);
        ACTestUtils.assertTransactionSuccess(builder);
    }

    @Test
    public void testCurrencyVoting() {
        Builder builder = new ACTestUtils.CurrencyBuilder().naming("testa", "TESTA", "Test AC");
        String currencyId = (String) ACTestUtils.assertTransactionSuccess(builder).get("transaction");
        generateBlock();

        builder = new ACTestUtils.CurrencyBuilder().naming("testb", "TESTB", "Test AC");
        String currency2Id = (String) ACTestUtils.assertTransactionSuccess(builder).get("transaction");
        generateBlock();

        setPhasingOnlyControl(VotingModel.CURRENCY, currencyId, 100L, null, null, null, 0, 0, 0);

        builder = SendMoneyCall.create()
                .secretPhrase(ALICE.getSecretPhrase())
                .recipient(BOB.getId())
                .amountNQT(Constants.ONE_rcc)
                .feeNQT(0);
        ACTestUtils.assertTransactionBlocked(builder);

        setTransactionPhasingParams(builder, 20, VotingModel.CURRENCY, currency2Id, 100L, null, null, null);
        ACTestUtils.assertTransactionBlocked(builder);

        setTransactionPhasingParams(builder, 20, VotingModel.CURRENCY, currencyId, 100L, null, null, null);
        ACTestUtils.assertTransactionSuccess(builder);
    }


    private void assertNoPhasingOnlyControl() {
        Builder builder = new APICall.Builder("getPhasingOnlyControl")
                .param("account", Long.toUnsignedString(ALICE.getId()));

        JSONObject response = builder.build().invoke();
        Assert.assertTrue(response.isEmpty());
    }

    private void assertPhasingOnly(PhasingParams expected, long maxFees, int minDuration, int maxDuration) {
        Builder builder = new APICall.Builder("getPhasingOnlyControl")
                .param("account", Long.toUnsignedString(ALICE.getId()));

        JSONObject response = builder.build().invoke();
        Logger.logMessage("getPhasingOnlyControl response: " + response.toJSONString());
        Assert.assertEquals(expected.getVoteWeighting().getVotingModel().getCode(), ((Long) response.get("votingModel")).byteValue());
        Assert.assertEquals(expected.getQuorum(), Convert.parseLong(response.get("quorum")));
        Assert.assertEquals(expected.getWhitelist().length, ((JSONArray) response.get("whitelist")).size());
        Assert.assertEquals(expected.getVoteWeighting().getHoldingId(), Convert.parseUnsignedLong((String) response.get("holding")));
        Assert.assertEquals(expected.getVoteWeighting().getMinBalance(), Convert.parseLong(response.get("minBalance")));
        Assert.assertEquals(expected.getVoteWeighting().getMinBalanceModel().getCode(), ((Long) response.get("minBalanceModel")).byteValue());
        Assert.assertEquals(maxFees, Convert.parseLong(response.get("maxFees")));
        Assert.assertEquals(minDuration, ((Long) response.get("minDuration")).shortValue());
        Assert.assertEquals(maxDuration, ((Long) response.get("maxDuration")).shortValue());
    }

    private void setPhasingOnlyControl(VotingModel votingModel, String holdingId, Long quorum,
                                       Long minBalance, MinBalanceModel minBalanceModel, long[] whitelist,
                                       long maxFees, int minDuration, int maxDuration) {

        Builder builder = new ACTestUtils.Builder(setPhasingOnlyControl, ALICE.getSecretPhrase());

        setControlPhasingParams(builder, votingModel, holdingId, quorum,
                minBalance, minBalanceModel, whitelist, maxFees, minDuration, maxDuration);

        APICall apiCall = builder.build();
        JSONObject response = apiCall.invoke();
        Logger.logMessage("setPhasingOnlyControl response: " + response.toJSONString());

        String result = (String) response.get("transaction");
        Assert.assertNotNull(result);

        generateBlock();
    }

    private void setControlPhasingParams(Builder builder,
                                         VotingModel votingModel, String holdingId, Long quorum,
                                         Long minBalance, MinBalanceModel minBalanceModel, long[] whitelist,
                                         long maxFees, int minDuration, int maxDuration) {
        if (votingModel != null) {
            builder.param("controlVotingModel", votingModel.getCode());
        }

        if (holdingId != null) {
            builder.param("controlHolding", holdingId);
        }

        if (quorum != null) {
            builder.param("controlQuorum", quorum);
        }

        if (minBalance != null) {
            builder.param("controlMinBalance", minBalance);
        }

        if (minBalanceModel != null) {
            builder.param("controlMinBalanceModel", minBalanceModel.getCode());
        }

        if (whitelist != null) {
            builder.param("controlWhitelisted", Arrays.stream(whitelist).mapToObj(l -> Long.toUnsignedString(l)).toArray(String[]::new));
        }

        if (maxFees > 0) {
            builder.param("controlMaxFees", maxFees);
        }

        if (minDuration > 0) {
            builder.param("controlMinDuration", minDuration);
        }

        if (maxDuration > 0) {
            builder.param("controlMaxDuration", maxDuration);
        }

    }

    private void setTransactionPhasingParams(Builder builder, int finishAfter, VotingModel votingModel, String holdingId, Long quorum,
                                             Long minBalance, MinBalanceModel minBalanceModel, long[] whitelist) {

        builder.param("phased", "true");

        builder.param("phasingVotingModel", votingModel.getCode());

        builder.param("phasingFinishHeight", rcc.getBlockchain().getHeight() + finishAfter);

        if (holdingId != null) {
            builder.param("phasingHolding", holdingId);
        }

        if (quorum != null) {
            builder.param("phasingQuorum", quorum);
        }

        if (minBalance != null) {
            builder.param("phasingMinBalance", minBalance);
        }

        if (minBalanceModel != null) {
            builder.param("phasingMinBalanceModel", minBalanceModel.getCode());
        }

        if (whitelist != null) {
            builder.param("phasingWhitelisted", Arrays.stream(whitelist).mapToObj(l -> Long.toUnsignedString(l)).toArray(String[]::new));
        }
    }
}
