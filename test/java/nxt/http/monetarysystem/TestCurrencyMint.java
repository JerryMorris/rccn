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

package rcc.http.monetarysystem;

import rcc.BlockchainTest;
import rcc.Constants;
import rcc.CurrencyMinting;
import rcc.CurrencyType;
import rcc.crypto.HashFunction;
import rcc.http.APICall;
import rcc.http.callers.CurrencyMintCall;
import rcc.http.callers.GetCurrencyCall;
import rcc.http.callers.GetMintingTargetCall;
import rcc.util.Convert;
import rcc.util.Logger;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class TestCurrencyMint extends BlockchainTest {

    @Test
    public void mint() {
        APICall apiCall = new TestCurrencyIssuance.Builder().
                type(CurrencyType.MINTABLE.getCode() | CurrencyType.EXCHANGEABLE.getCode()).
                maxSupply(10000000).
                initialSupply(0).
                issuanceHeight(0).
                minting((byte) 2, (byte) 8, HashFunction.SHA256.getId()).
                build();

        String currencyId = TestCurrencyIssuance.issueCurrencyApi(apiCall);
        mintCurrency(currencyId);
    }

    private void mintCurrency(String currencyId) {
        // Failed attempt to mint
        APICall apiCall = CurrencyMintCall.create().
                secretPhrase(ALICE.getSecretPhrase()).
                feeNQT(Constants.ONE_rcc).
                currency(currencyId).
                nonce("123456").
                units(1000).
                counter(1).
                build();
        JSONObject mintResponse = apiCall.invoke();
        Logger.logDebugMessage("mintResponse: " + mintResponse);
        generateBlock();
        apiCall = GetCurrencyCall.create().
//                feeNQT(Constants.ONE_rcc).
        currency(currencyId).
                        build();
        JSONObject getCurrencyResponse = apiCall.invoke();
        Logger.logDebugMessage("getCurrencyResponse: " + getCurrencyResponse);
        Assert.assertEquals("0", getCurrencyResponse.get("currentSupply"));

        // Successful attempt
        long units = 10;
        long algorithm = (Long) getCurrencyResponse.get("algorithm");
        long nonce;
        for (nonce = 0; nonce < Long.MAX_VALUE; nonce++) {
            if (CurrencyMinting.meetsTarget(CurrencyMinting.getHash((byte) algorithm, nonce, Convert.parseUnsignedLong(currencyId), units, 1, ALICE.getId()),
                    CurrencyMinting.getTarget(2, 8, units, 0, 100000))) {
                break;
            }
        }
        Logger.logDebugMessage("nonce: " + nonce);
        apiCall = CurrencyMintCall.create().
                secretPhrase(ALICE.getSecretPhrase()).
                feeNQT(Constants.ONE_rcc).
                currency(currencyId).
                nonce("" + nonce).
                units(units).
                counter(1).
                build();
        mintResponse = apiCall.invoke();
        Logger.logDebugMessage("mintResponse: " + mintResponse);
        generateBlock();
        apiCall = GetCurrencyCall.create().
                currency(currencyId).
                build();
        getCurrencyResponse = apiCall.invoke();
        Logger.logDebugMessage("getCurrencyResponse: " + getCurrencyResponse);
        Assert.assertEquals("" + units, getCurrencyResponse.get("currentSupply"));

        apiCall = GetMintingTargetCall.create().
                currency(currencyId).
                account(ALICE.getId()).
                units(1000).
                build();
        JSONObject getMintingTargetResponse = apiCall.invoke();
        Logger.logDebugMessage("getMintingTargetResponse: " + getMintingTargetResponse);
        Assert.assertEquals("4000", getMintingTargetResponse.get("difficulty"));
        Assert.assertEquals("a9f1d24d62105839b4c876be9f1a2fdd24068195438b6ce7fba9f1d24d621000", getMintingTargetResponse.get("targetBytes"));
    }
}
