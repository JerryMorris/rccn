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

package rcc.addons;

import rcc.Block;
import rcc.BlockchainProcessor;
import rcc.rcc;
import rcc.util.Listener;
import rcc.util.Logger;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public final class DownloadTimer implements AddOn {

    private PrintWriter writer = null;

    @Override
    public void init() {

        try {

            writer = new PrintWriter((new BufferedWriter(new OutputStreamWriter(new FileOutputStream("downloadtime.csv")))), true);
            writer.println("height,time,dtime,bps,transations,dtransactions,tps");
            rcc.getBlockchainProcessor().addListener(new Listener<Block>() {

                private final int interval = 10000;
                private final long startTime = System.currentTimeMillis();
                private long previousTime = 0;
                private long transactions = 0;
                private long dtransactions = 0;

                @Override
                public void notify(Block block) {
                    int n = block.getTransactions().size();
                    transactions += n;
                    dtransactions += n;
                    int height = block.getHeight();
                    if (height % interval == 0) {
                        long time = System.currentTimeMillis() - startTime;
                        writer.print(height);
                        writer.print(',');
                        writer.print(time/1000);
                        writer.print(',');
                        long dtime = (time - previousTime)/1000;
                        writer.print(dtime);
                        writer.print(',');
                        writer.print(interval/dtime);
                        writer.print(',');
                        writer.print(transactions);
                        writer.print(',');
                        writer.print(dtransactions);
                        writer.print(',');
                        long tps = dtransactions/dtime;
                        writer.println(tps);
                        previousTime = time;
                        dtransactions = 0;
                    }
                }

            }, BlockchainProcessor.Event.BLOCK_PUSHED);

        } catch (IOException e) {
            Logger.logErrorMessage(e.getMessage(), e);
        }

    }

    @Override
    public void shutdown() {
        if (writer != null) {
            writer.flush();
            writer.close();
        }
    }

}
