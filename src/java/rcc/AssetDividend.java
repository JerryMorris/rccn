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

import rcc.db.DbClause;
import rcc.db.DbIterator;
import rcc.db.DbKey;
import rcc.db.EntityDbTable;
import rcc.util.Listener;
import rcc.util.Listeners;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class AssetDividend {

    public enum Event {
        ASSET_DIVIDEND
    }

    private static final Listeners<AssetDividend,Event> listeners = new Listeners<>();

    private static final DbKey.LongKeyFactory<AssetDividend> dividendDbKeyFactory = new DbKey.LongKeyFactory<AssetDividend>("id") {

        @Override
        public DbKey newKey(AssetDividend assetDividend) {
            return assetDividend.dbKey;
        }

    };

    private static final EntityDbTable<AssetDividend> assetDividendTable = new EntityDbTable<AssetDividend>("asset_dividend", dividendDbKeyFactory) {

        @Override
        protected AssetDividend load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
            return new AssetDividend(rs, dbKey);
        }

        @Override
        protected void save(Connection con, AssetDividend assetDividend) throws SQLException {
            assetDividend.save(con);
        }

    };

    public static boolean addListener(Listener<AssetDividend> listener, Event eventType) {
        return listeners.addListener(listener, eventType);
    }

    public static boolean removeListener(Listener<AssetDividend> listener, Event eventType) {
        return listeners.removeListener(listener, eventType);
    }

    public static DbIterator<AssetDividend> getAssetDividends(long assetId, int from, int to) {
        return assetDividendTable.getManyBy(new DbClause.LongClause("asset_id", assetId), from, to);
    }

    public static AssetDividend getLastDividend(long assetId) {
        try (DbIterator<AssetDividend> dividends = assetDividendTable.getManyBy(new DbClause.LongClause("asset_id", assetId), 0, 0)) {
            if (dividends.hasNext()) {
                return dividends.next();
            }
        }
        return null;
    }

    static AssetDividend addAssetDividend(long transactionId, Attachment.ColoredCoinsDividendPayment attachment,
                                          long totalDividend, long numAccounts) {
        AssetDividend assetDividend = new AssetDividend(transactionId, attachment, totalDividend, numAccounts);
        assetDividendTable.insert(assetDividend);
        listeners.notify(assetDividend, Event.ASSET_DIVIDEND);
        return assetDividend;
    }

    static void init() {}


    private final long id;
    private final DbKey dbKey;
    private final long holdingId;
    private final HoldingType holdingType;
    private final long assetId;
    private final long amountNQTPerQNT;
    private final int dividendHeight;
    private final long totalDividend;
    private final long numAccounts;
    private final int timestamp;
    private final int height;

    private AssetDividend(long transactionId, Attachment.ColoredCoinsDividendPayment attachment,
                          long totalDividend, long numAccounts) {
        this.id = transactionId;
        this.dbKey = dividendDbKeyFactory.newKey(this.id);
        this.holdingId = attachment.getHoldingId();
        this.holdingType = attachment.getHoldingType();
        this.assetId = attachment.getAssetId();
        this.amountNQTPerQNT = attachment.getAmountNQTPerQNT();
        this.dividendHeight = attachment.getHeight();
        this.totalDividend = totalDividend;
        this.numAccounts = numAccounts;
        this.timestamp = rcc.getBlockchain().getLastBlockTimestamp();
        this.height = rcc.getBlockchain().getHeight();
    }

    private AssetDividend(ResultSet rs, DbKey dbKey) throws SQLException {
        this.id = rs.getLong("id");
        this.dbKey = dbKey;
        this.holdingId = rs.getLong("holding_id");
        this.holdingType = HoldingType.get(rs.getByte("holding_type"));
        this.assetId = rs.getLong("asset_id");
        this.amountNQTPerQNT = rs.getLong("amount");
        this.dividendHeight = rs.getInt("dividend_height");
        this.totalDividend = rs.getLong("total_dividend");
        this.numAccounts = rs.getLong("num_accounts");
        this.timestamp = rs.getInt("timestamp");
        this.height = rs.getInt("height");
    }

    private void save(Connection con) throws SQLException {
        try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO asset_dividend (id, holding_id, holding_type, asset_id, "
                + "amount, dividend_height, total_dividend, num_accounts, timestamp, height) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            int i = 0;
            pstmt.setLong(++i, this.id);
            pstmt.setLong(++i, this.holdingId);
            pstmt.setByte(++i, this.holdingType.getCode());
            pstmt.setLong(++i, this.assetId);
            pstmt.setLong(++i, this.amountNQTPerQNT);
            pstmt.setInt(++i, this.dividendHeight);
            pstmt.setLong(++i, this.totalDividend);
            pstmt.setLong(++i, this.numAccounts);
            pstmt.setInt(++i, this.timestamp);
            pstmt.setInt(++i, this.height);
            pstmt.executeUpdate();
        }
    }

    public long getId() {
        return id;
    }

    public long getHoldingId() {
        return holdingId;
    }

    public HoldingType getHoldingType() {
        return holdingType;
    }

    public long getAssetId() {
        return assetId;
    }

    public long getAmountNQTPerQNT() {
        return amountNQTPerQNT;
    }

    public int getDividendHeight() {
        return dividendHeight;
    }

    public long getTotalDividend() {
        return totalDividend;
    }

    public long getNumAccounts() {
        return numAccounts;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public int getHeight() {
        return height;
    }

}
