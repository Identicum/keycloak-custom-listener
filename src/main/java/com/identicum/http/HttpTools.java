package com.identicum.http;

import java.net.SocketTimeoutException;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.stream.Stream;

public class HttpTools {

	private static final Logger logger = Logger.getLogger(HttpTools.class);

	/**
	 * Execute http request with the connection pool and handle the received response.
	 * If the response status is not OK it throws a {@link RuntimeException} to stop the flow.
	 *
	 * @param request Request to be executed with all needed headers.
	 * @return SimpleHttpResponse with code received and body
	 * @throws RuntimeException if status code received is not 200
	 */
	public static SimpleHttpResponse executeCall(CloseableHttpClient httpClient, HttpRequestBase request) {
		logger.debugv("Executing Http Request [{0}] on [{1}]", request.getMethod(), request.getURI());
		request.setHeader(HttpHeaders.CONNECTION, HTTP.CONN_KEEP_ALIVE);

		Stream.of( request.getAllHeaders() ).forEach(header -> logger.debugv("Request header: {0} -> {1}", header.getName(), header.getValue() ));
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(request);
			String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
			logger.debugv("Response code obtained from server: {0}", response.getStatusLine().getStatusCode());
			logger.debugv("Response body obtained from server: {0}", responseString);
			return new SimpleHttpResponse(response.getStatusLine().getStatusCode(), responseString);
		}
		catch(ConnectionPoolTimeoutException cpte) {
			logger.errorv("Connection pool timeout exception: {0}", cpte);
			throw new RuntimeException("Connection pool timeout exception.", cpte);
		}
		catch(ConnectTimeoutException cte) {
			logger.errorv("Connect timeout exception: {0}", cte);
			throw new RuntimeException("Connect timeout exception.", cte);
		}
		catch(SocketTimeoutException ste) {
			logger.errorv("Socket timeout exception: {0}", ste);
			throw new RuntimeException("Socket timeout exception.", ste);
		}
		catch(IOException io) {
			logger.errorv("Error executing request: {0}", io);
			throw new RuntimeException("Error executing request.", io);
		}
		finally {
			closeQuietly(response);
		}
	}

	public static void stopOnError(SimpleHttpResponse response) {
		if(!response.isSuccess()) {
			logger.debugv("Response status code was not success. Code received: {0}", response.getStatus());
			logger.debugv("Response received: {0}", response.getResponse());
			throw new RuntimeException("Http Request was not success. Check logs to get more information");
		}
	}

	/**
	 * Close quietly a http response
	 * @param response Response to be closed
	 */
	public static void closeQuietly(CloseableHttpResponse response) {
		if (response != null)
			try {
				response.close();
			} catch (IOException io) {
				logger.warn("Error closing http response", io);
			}
	}

	/**
	 * Close quietly a http client
	 * @param client HttpClient to be closed
	 */
	public static void closeQuietly(CloseableHttpClient client) {
		if (client != null)
			try {
				client.close();
			} catch (IOException io) {
				logger.warn("Error closing http response", io);
			}
	}
}
