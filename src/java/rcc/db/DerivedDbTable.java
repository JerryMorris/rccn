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

package rcc.db;

import rcc.Constants;
import rcc.Db;
import rcc.rcc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class DerivedDbTable {

    protected static final TransactionalDb db = Db.db;

    protected final String table;

    protected DerivedDbTable(String table) {
        this.table = table;
        rcc.getBlockchainProcessor().registerDerivedTable(this);
    }

    public void popOffTo(int height) {
        if (!db.isInTransaction()) {
            throw new IllegalStateException("Not in transaction");
        }
        try (Connection con = db.getConnection();
             PreparedStatement pstmtDelete = con.prepareStatement("DELETE FROM " + table + " WHERE height > ? LIMIT " + Constants.BATCH_COMMIT_SIZE)) {
            pstmtDelete.setInt(1, height);
            int deleted;
            do {
                deleted = pstmtDelete.executeUpdate();
                Db.db.commitTransaction();
            } while (deleted >= Constants.BATCH_COMMIT_SIZE);
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public void rollback(int height) {
        popOffTo(height);
    }

    public void truncate() {
        if (!db.isInTransaction()) {
            throw new IllegalStateException("Not in transaction");
        }
        try (Connection con = db.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.executeUpdate("TRUNCATE TABLE " + table);
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public void trim(int height) {
        //nothing to trim
    }

    public void createSearchIndex(Connection con) throws SQLException {
        //implemented in EntityDbTable only
    }

    public boolean isPersistent() {
        return false;
    }

    @Override
    public final String toString() {
        return table;
    }

}
