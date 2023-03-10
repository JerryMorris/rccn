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

import rcc.BlockchainTest;
import rcc.Constants;
import rcc.http.callers.GetAccountCall;
import rcc.http.callers.LeaseBalanceCall;
import rcc.util.Logger;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class LeaseTest extends BlockchainTest {

    @Test
    public void lease() {
        // #2 & #3 lease their balance to %1
        JSONObject response = LeaseBalanceCall.create().
                secretPhrase(BOB.getSecretPhrase()).
                recipient(ALICE.getStrId()).
                period("2").
                feeNQT(Constants.ONE_rcc).
                build().invokeNoError();

        Logger.logDebugMessage("leaseBalance: " + response);
        response = LeaseBalanceCall.create().
                secretPhrase(CHUCK.getSecretPhrase()).
                recipient(ALICE.getStrId()).
                period("3").
                feeNQT(Constants.ONE_rcc).
                build().invokeNoError();
        Logger.logDebugMessage("leaseBalance: " + response);
        generateBlock();

        // effective balance hasn't changed since lease is not in effect yet
        JSONObject lesseeResponse = GetAccountCall.create().
                account(ALICE.getRsAccount()).
                includeEffectiveBalance(true).
                build().invokeNoError();
        Logger.logDebugMessage("getLesseeAccount: " + lesseeResponse);
        Assert.assertEquals(ALICE.getInitialEffectiveBalance(), lesseeResponse.get("effectiveBalancercc"));

        // lease is registered
        JSONObject leasedResponse1 = GetAccountCall.create().
                account(BOB.getRsAccount()).
                build().invokeNoError();
        Logger.logDebugMessage("getLeasedAccount: " + leasedResponse1);
        Assert.assertEquals(ALICE.getRsAccount(), leasedResponse1.get("currentLesseeRS"));
        Assert.assertEquals((long) (baseHeight + 1 + 1), leasedResponse1.get("currentLeasingHeightFrom"));
        Assert.assertEquals((long) (baseHeight + 1 + 1 + 2), leasedResponse1.get("currentLeasingHeightTo"));
        JSONObject leasedResponse2 = GetAccountCall.create().
                account(CHUCK.getRsAccount()).
                build().invokeNoError();
        Logger.logDebugMessage("getLeasedAccount: " + leasedResponse2);
        Assert.assertEquals(ALICE.getRsAccount(), leasedResponse2.get("currentLesseeRS"));
        Assert.assertEquals((long) (baseHeight + 1 + 1), leasedResponse2.get("currentLeasingHeightFrom"));
        Assert.assertEquals((long) (baseHeight + 1 + 1 + 3), leasedResponse2.get("currentLeasingHeightTo"));
        generateBlock();


        lesseeResponse = GetAccountCall.create().
                account(ALICE.getRsAccount()).
                includeEffectiveBalance(true).
                build().invoke();
        Logger.logDebugMessage("getLesseeAccount: " + lesseeResponse);
        Assert.assertEquals((ALICE.getInitialBalance() + BOB.getInitialBalance() + CHUCK.getInitialBalance()) / Constants.ONE_rcc - 2,
                lesseeResponse.get("effectiveBalancercc"));
        generateBlock();
        generateBlock();
        lesseeResponse = GetAccountCall.create().
                account(ALICE.getRsAccount()).
                includeEffectiveBalance(true).
                build().invoke();
        Logger.logDebugMessage("getLesseeAccount: " + lesseeResponse);
        Assert.assertEquals((ALICE.getInitialBalance() + CHUCK.getInitialBalance()) / Constants.ONE_rcc - 1 /* fees */,
                lesseeResponse.get("effectiveBalancercc"));
        generateBlock();
        lesseeResponse = GetAccountCall.create().
                account(ALICE.getRsAccount()).
                includeEffectiveBalance(true).
                build().invoke();
        Logger.logDebugMessage("getLesseeAccount: " + lesseeResponse);
        Assert.assertEquals((ALICE.getInitialBalance()) / Constants.ONE_rcc,
                lesseeResponse.get("effectiveBalancercc"));
    }
}
