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

package rcc.util;

import rcc.Constants;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class EpochTimeTest {

    private static final int testTime = 1333920;

    @Test
    public void testFromEpochTimeProd() {
        Assume.assumeFalse(Constants.isTestnet);

        long time = Convert.fromEpochTime(testTime);
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Assert.assertEquals("16/01/2018 10:31:59", dateFormat.format(new Date(time)));
    }

    @Test
    public void testFromToEpochTimeRoundTrip() {
        long time = Convert.fromEpochTime(testTime);
        Assert.assertEquals(testTime, Convert.toEpochTime(time));
    }

    @Test
    public void testFromEpochTimeTestNet() {
        Assume.assumeTrue(Constants.isTestnet);

        long time = Convert.fromEpochTime(testTime);
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Assert.assertEquals("09/12/2013 10:31:59", dateFormat.format(new Date(time)));
    }
}
