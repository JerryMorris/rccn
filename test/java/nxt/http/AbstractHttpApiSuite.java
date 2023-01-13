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


import rcc.BlockchainProcessor;
import rcc.BlockchainTest;
import rcc.Helper;
import rcc.rcc;
import rcc.SafeShutdownSuite;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

public abstract class AbstractHttpApiSuite extends SafeShutdownSuite {
    @BeforeClass
    public static void init() {
        SafeShutdownSuite.safeSuiteInit();
        BlockchainTest.initrcc();
        rcc.getTransactionProcessor().clearUnconfirmedTransactions();
        rcc.getBlockchainProcessor().addListener(new Helper.BlockListener(), BlockchainProcessor.Event.BLOCK_GENERATED);
        Assert.assertEquals(0, Helper.getCount("unconfirmed_transaction"));
    }

    @AfterClass
    public static void shutdown() {
        Assert.assertEquals(0, Helper.getCount("unconfirmed_transaction"));
        SafeShutdownSuite.safeSuiteShutdown();
    }
}
