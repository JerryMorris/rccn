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

import rcc.rcc;
import rcc.db.pool.ConnectionPool;
import rcc.db.pool.H2ConnectionPool;
import rcc.util.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class BasicDb {

    public static final class DbProperties {

        private long maxCacheSize;
        private String dbUrl;
        private String dbType;
        private String dbDir;
        private String dbParams;
        private String dbUsername;
        private String dbPassword;
        private int maxConnections;
        private int loginTimeout;
        private int defaultLockTimeout;
        private int maxMemoryRows;

        public DbProperties maxCacheSize(int maxCacheSize) {
            this.maxCacheSize = maxCacheSize;
            return this;
        }

        public DbProperties dbUrl(String dbUrl) {
            this.dbUrl = dbUrl;
            return this;
        }

        public DbProperties dbType(String dbType) {
            this.dbType = dbType;
            return this;
        }

        public DbProperties dbDir(String dbDir) {
            this.dbDir = dbDir;
            return this;
        }

        public DbProperties dbParams(String dbParams) {
            this.dbParams = dbParams;
            return this;
        }

        public DbProperties dbUsername(String dbUsername) {
            this.dbUsername = dbUsername;
            return this;
        }

        public DbProperties dbPassword(String dbPassword) {
            this.dbPassword = dbPassword;
            return this;
        }

        public DbProperties maxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
            return this;
        }

        public DbProperties loginTimeout(int loginTimeout) {
            this.loginTimeout = loginTimeout;
            return this;
        }

        public DbProperties defaultLockTimeout(int defaultLockTimeout) {
            this.defaultLockTimeout = defaultLockTimeout;
            return this;
        }

        public DbProperties maxMemoryRows(int maxMemoryRows) {
            this.maxMemoryRows = maxMemoryRows;
            return this;
        }

    }

    private ConnectionPool cp;
    private final String dbUrl;
    private final String dbUsername;
    private final String dbPassword;
    private final int maxConnections;
    private final int loginTimeout;
    private final int defaultLockTimeout;
    private final int maxMemoryRows;
    private volatile boolean initialized = false;

    public BasicDb(DbProperties dbProperties) {
        long maxCacheSize = dbProperties.maxCacheSize;
        if (maxCacheSize == 0) {
            maxCacheSize = Math.min(256, Math.max(16, (Runtime.getRuntime().maxMemory() / (1024 * 1024) - 128)/2)) * 1024;
        }
        String dbUrl = dbProperties.dbUrl;
        if (dbUrl == null) {
            String dbDir = rcc.getDbDir(dbProperties.dbDir);
            dbUrl = String.format("jdbc:%s:%s;%s", dbProperties.dbType, dbDir, dbProperties.dbParams);
        }
        if (!dbUrl.contains("CACHE_SIZE=")) {
            dbUrl += ";CACHE_SIZE=" + maxCacheSize;
        }
        this.dbUrl = dbUrl;
        this.dbUsername = dbProperties.dbUsername;
        this.dbPassword = dbProperties.dbPassword;
        this.maxConnections = dbProperties.maxConnections;
        this.loginTimeout = dbProperties.loginTimeout;
        this.defaultLockTimeout = dbProperties.defaultLockTimeout;
        this.maxMemoryRows = dbProperties.maxMemoryRows;
    }

    public void init(DbVersion dbVersion) {
        Logger.logDebugMessage("Database jdbc url set to %s username %s", dbUrl, dbUsername);
        String implClassName = rcc.getStringProperty("rcc.connectionPoolImpl");

        try {
            Class<?> poolImplClass = Class.forName(implClassName);
            cp = (ConnectionPool) poolImplClass.newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            Logger.logErrorMessage("Failed to create connection pool instance", e);
            cp = new H2ConnectionPool();
        }
        cp.initialize(dbUrl, dbUsername, dbPassword, maxConnections, loginTimeout);

        FullTextTrigger.setActive(true);
        try (Connection con = getPooledConnection();
             Statement stmt = con.createStatement()) {
            stmt.executeUpdate("SET DEFAULT_LOCK_TIMEOUT " + defaultLockTimeout);
            stmt.executeUpdate("SET MAX_MEMORY_ROWS " + maxMemoryRows);
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
        dbVersion.init(this);
        initialized = true;
    }

    public void shutdown() {
        if (!initialized) {
            return;
        }
        try {
            FullTextTrigger.setActive(false);
            Connection con = getPooledConnection();
            Statement stmt = con.createStatement();
            boolean compact = ! rcc.getBooleanProperty("rcc.disableCompactOnShutdown");
            if (compact) {
                Logger.logShutdownMessage("Shutting down with compacting...");
                stmt.execute("SHUTDOWN COMPACT");
                Logger.logShutdownMessage("Database shutdown completed");
            } else {
                stmt.execute("SHUTDOWN");
                Logger.logShutdownMessage("Database shutdown without compact completed");
            }
        } catch (SQLException e) {
            Logger.logShutdownMessage(e.toString(), e);
        }
    }

    public void analyzeTables() {
        try (Connection con = getPooledConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute("ANALYZE");
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public Connection getConnection() throws SQLException {
        Connection con = getPooledConnection();
        con.setAutoCommit(true);
        return con;
    }

    protected Connection getPooledConnection() throws SQLException {
        return cp.getConnection();
    }

    public String getUrl() {
        return dbUrl;
    }

}
