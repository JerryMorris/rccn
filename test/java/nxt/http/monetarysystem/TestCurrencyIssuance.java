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
import rcc.CurrencyType;
import rcc.http.APICall;
import rcc.http.callers.CreateTransactionCallBuilder;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import static rcc.http.callers.ApiSpec.issueCurrency;

public class TestCurrencyIssuance extends BlockchainTest {

    @Test
    public void issueCurrency() {
        APICall apiCall = new Builder().build();
        issueCurrencyApi(apiCall);
    }

    @Test
    public void issueMultipleCurrencies() {
        APICall apiCall = new Builder().naming("axc", "AXC", "Currency A").build();
        issueCurrencyApi(apiCall);
        apiCall = new Builder().naming("bXbx", "BXBX", "Currency B").feeNQT(1000 * Constants.ONE_rcc).build();
        issueCurrencyApi(apiCall);
        apiCall = new Builder().naming("ccXcc", "CCCXC", "Currency C").feeNQT(40 * Constants.ONE_rcc).build();
        issueCurrencyApi(apiCall);
        apiCall = new APICall.Builder("getCurrency").param("code", "BXBX").build();
        JSONObject response = apiCall.invoke();
        Assert.assertEquals("bXbx", response.get("name"));
    }

    public static String issueCurrencyApi(APICall apiCall) {
        JSONObject issueCurrencyResponse = apiCall.invokeNoError();
        String currencyId = (String) issueCurrencyResponse.get("transaction");
        generateBlock();

        apiCall = new APICall.Builder("getCurrency").param("currency", currencyId).build();
        JSONObject getCurrencyResponse = apiCall.invokeNoError();
        Assert.assertEquals(currencyId, getCurrencyResponse.get("currency"));
        return currencyId;
    }

    public static class Builder extends CreateTransactionCallBuilder {

        public Builder() {
            super(issueCurrency);
            secretPhrase(ALICE.getSecretPhrase());
            feeNQT(0L);
            //feeNQT(25000 * Constants.ONE_rcc);
            param("name", "Test1");
            param("code", "TSXXX");
            param("description", "Test Currency 1");
            param("type", CurrencyType.EXCHANGEABLE.getCode());
            param("maxSupply", 100000);
            param("initialSupply", 100000);
            param("issuanceHeight", 0);
            param("algorithm", (byte)0);
        }

        public Builder naming(String name, String code, String description) {
            param("name", name);
            param("code", code).
            param("description", description);
            return this;
        }

        public Builder type(int type) {
            param("type", type);
            return this;
        }

        public Builder maxSupply(long maxSupply) {
            param("maxSupply", maxSupply);
            return this;
        }

        public Builder reserveSupply(long reserveSupply) {
            param("reserveSupply", reserveSupply);
            return this;
        }

        public Builder initialSupply(long initialSupply) {
            param("initialSupply", initialSupply);
            return this;
        }

        public Builder issuanceHeight(int issuanceHeight) {
            param("issuanceHeight", issuanceHeight);
            return this;
        }

        public Builder minReservePerUnitNQT(long minReservePerUnitNQT) {
            param("minReservePerUnitNQT", minReservePerUnitNQT);
            return this;
        }

        public Builder minting(byte minDifficulty, byte maxDifficulty, byte algorithm) {
            param("minDifficulty", minDifficulty);
            param("maxDifficulty", maxDifficulty);
            param("algorithm", algorithm);
            return this;
        }

    }
}
