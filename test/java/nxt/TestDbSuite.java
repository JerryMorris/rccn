/*
 * Copyright © 2013-2016 The rcc Core Developers.
 * Copyright © 2016-2022 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package rcc;

import rcc.addons.AddonsSuite;
import rcc.http.PaymentAndMessagesSuite;
import rcc.http.accountControl.AccountControlSuite;
import rcc.http.accountproperties.AccountPropertiesSuite;
import rcc.http.alias.CreateAliasTest;
import rcc.http.assetexchange.AssetExchangeSuite;
import rcc.http.monetarysystem.CurrencySuite;
import rcc.http.shuffling.ShufflingSuite;
import rcc.http.twophased.TwoPhasedSuite;
import rcc.http.votingsystem.VotingSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TokenTest.class,
        GeneratorTest.class,
        CurrencySuite.class,
        PaymentAndMessagesSuite.class,
        VotingSuite.class,
        TwoPhasedSuite.class,
        ShufflingSuite.class,
        AccountControlSuite.class,
        AccountPropertiesSuite.class,
        AssetExchangeSuite.class,
        CreateAliasTest.class,
        AddonsSuite.class
})
public class TestDbSuite extends SafeShutdownSuite {
}
