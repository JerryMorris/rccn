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

package rcc;

import rcc.crypto.Crypto;
import rcc.util.Convert;
import rcc.util.Logger;
import rcc.util.Time;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.util.Properties;

public abstract class BlockchainTest extends AbstractBlockchainTest {

    @Rule
    public final TestRule watchman = new TestWatcher() {
        @Override
        protected void starting(Description description) {
            Logger.logMessage("Starting test " + description.toString());
        }

        @Override
        protected void finished(Description description) {
            Logger.logMessage("Finished test " + description.toString());
        }
    };

    protected static Tester FORGY;
    protected static Tester ALICE;
    protected static Tester BOB;
    protected static Tester CHUCK;
    protected static Tester DAVE;

    protected static int baseHeight;

    private static String forgerSecretPhrase = "aSykrgKGZNlSVOMDxkZZgbTvQqJPGtsBggb";
    private static final String forgerPublicKey = Convert.toHexString(Crypto.getPublicKey(forgerSecretPhrase));

    public static final String aliceSecretPhrase = "hope peace happen touch easy pretend worthless talk them indeed wheel state";
    private static final String bobSecretPhrase2 = "rshw9abtpsa2";
    private static final String chuckSecretPhrase = "eOdBVLMgySFvyiTy8xMuRXDTr45oTzB7L5J";
    private static final String daveSecretPhrase = "t9G2ymCmDsQij7VtYinqrbGCOAtDDA3WiNr";

    private static boolean isrccInitialized = false;
    private static boolean isRunInSuite = false;

    public static void initrcc() {
        if (!isrccInitialized) {
            Properties properties = ManualForgingTest.newTestProperties();
            properties.setProperty("rcc.isTestnet", "true");
            properties.setProperty("rcc.isOffline", "true");
            properties.setProperty("rcc.enableFakeForging", "true");
            properties.setProperty("rcc.fakeForgingPublicKeys", forgerPublicKey);
            properties.setProperty("rcc.timeMultiplier", "1");
            properties.setProperty("rcc.testnetGuaranteedBalanceConfirmations", "1");
            properties.setProperty("rcc.testnetLeasingDelay", "1");
            properties.setProperty("rcc.disableProcessTransactionsThread", "true");
            properties.setProperty("rcc.deleteFinishedShufflings", "false");
            properties.setProperty("rcc.disableSecurityPolicy", "true");
            properties.setProperty("rcc.disableAdminPassword", "true");
            properties.setProperty("rcc.testDbDir", "./rcc_unit_test_db/rcc");
            properties.setProperty("rcc.isAutomatedTest", "true");
            properties.setProperty("rcc.addOns", "rcc.addons.JPLSnapshot");
            AbstractBlockchainTest.init(properties);
            Logger.logMessage("Initialized rcc for unit testing.");
            isrccInitialized = true;
        }
    }
    
    @BeforeClass
    public static void init() {
        initrcc();
        rcc.setTime(new Time.CounterTime(rcc.getEpochTime()));

        baseHeight = blockchain.getHeight();
        Logger.logMessage("baseHeight: " + baseHeight);
    }

    @Before
    public final void setUp() {
        Logger.logMessage("Creating test accounts.");
        final long amountNQT = Constants.ONE_rcc * 1000000;
        FORGY = Tester.createAndAdd(forgerSecretPhrase, amountNQT);
        ALICE = Tester.createAndAdd(aliceSecretPhrase, amountNQT);
        BOB =   Tester.createAndAdd(bobSecretPhrase2, amountNQT);
        CHUCK = Tester.createAndAdd(chuckSecretPhrase, amountNQT);
        DAVE =  Tester.createAndAdd(daveSecretPhrase, amountNQT);

        Logger.logMessage("Created test accounts.");
    }

    public static void setIsRunInSuite(boolean isRunInSuite) {
        BlockchainTest.isRunInSuite = isRunInSuite;
    }

    @AfterClass
    public static void afterClass() {
        if (!isRunInSuite) {
            Logger.logMessage("@AfterClass - rcc.shutdown()");
            rcc.shutdown();
        }
    }

    @After
    public void tearDown() {
        Logger.logMessage("@After - clearing unconfirmed transactions and pop off to height " + baseHeight);
        TransactionProcessorImpl.getInstance().clearUnconfirmedTransactions();
        blockchainProcessor.popOffTo(baseHeight);
    }

    public static void generateBlock() {
        try {
            blockchainProcessor.generateBlock(forgerSecretPhrase, rcc.getEpochTime());
        } catch (BlockchainProcessor.BlockNotAcceptedException e) {
            throw new AssertionError(e);
        }
    }

    public static void generateBlocks(int howMany) {
        for (int i = 0; i < howMany; i++) {
            generateBlock();
        }
    }
}
