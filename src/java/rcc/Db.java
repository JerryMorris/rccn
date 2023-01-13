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

import rcc.db.BasicDb;
import rcc.db.TransactionalDb;

public final class Db {

    public static final String PREFIX = Constants.isTestnet ? "rcc.testDb" : "rcc.db";
    public static final TransactionalDb db = new TransactionalDb(new BasicDb.DbProperties()
            .maxCacheSize(rcc.getIntProperty("rcc.dbCacheKB"))
            .dbUrl(rcc.getStringProperty(PREFIX + "Url"))
            .dbType(rcc.getStringProperty(PREFIX + "Type"))
            .dbDir(rcc.getStringProperty(PREFIX + "Dir"))
            .dbParams(rcc.getStringProperty(PREFIX + "Params"))
            .dbUsername(rcc.getStringProperty(PREFIX + "Username"))
            .dbPassword(rcc.getStringProperty(PREFIX + "Password", null, true))
            .maxConnections(rcc.getIntProperty("rcc.maxDbConnections"))
            .loginTimeout(rcc.getIntProperty("rcc.dbLoginTimeout"))
            .defaultLockTimeout(rcc.getIntProperty("rcc.dbDefaultLockTimeout") * 1000)
            .maxMemoryRows(rcc.getIntProperty("rcc.dbMaxMemoryRows"))
    );

    public static void init() {
        db.init(new rccDbVersion());
    }

    static void shutdown() {
        db.shutdown();
    }

    private Db() {} // never

}
