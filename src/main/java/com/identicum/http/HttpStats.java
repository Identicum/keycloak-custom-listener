package com.identicum.http;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.jboss.logging.Logger;

import java.util.TimerTask;

public class HttpStats extends TimerTask {

    private Logger logger;
    private PoolingHttpClientConnectionManager poolingHttpClientConnectionManager;

    public HttpStats(Logger logger, PoolingHttpClientConnectionManager poolingHttpClientConnectionManager) {
        this.logger = logger;
        this.poolingHttpClientConnectionManager = poolingHttpClientConnectionManager;
    }

    @Override
    public void run() {
        StringBuilder sb = new StringBuilder();
        PoolStats poolStats = poolingHttpClientConnectionManager.getTotalStats();
        sb.append("maxConnections: " + poolStats.getMax() + ", ");
        sb.append("defaultMaxPerRoute: " + poolingHttpClientConnectionManager.getDefaultMaxPerRoute());
        sb.append("availableConnections: " + poolStats.getAvailable() + ", ");
        sb.append("leasedConnections: " + poolStats.getLeased() + ", ");
        sb.append("pendingConnections: " + poolStats.getPending() + ", ");
        logger.infov("HTTP pool stats: {0}", sb.toString());
    }
}
