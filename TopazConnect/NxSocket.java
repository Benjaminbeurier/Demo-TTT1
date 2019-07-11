/**
 * These materials contain confidential information and trade secrets of Compuware Corporation. You shall maintain the materials 
 * as confidential and shall not disclose its contents to any third party except as may be required by law or regulation. Use, 
 * disclosure, or reproduction is prohibited without the prior express written permission of Compuware Corporation.
 *  
 * All Compuware products listed within the materials are trademarks of Compuware Corporation. All other company or product 
 * names are trademarks of their respective owners.
 *  
 * Copyright (c) 2017 Compuware Corporation. All rights reserved.
 */
package com.compuware.topazc.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.SecureRandom;
import java.security.cert.CertificateException;

import org.eclipse.core.runtime.Status;
import org.eclipse.ui.statushandlers.StatusManager;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class NxSocket {
	private Socket socket = null;
	private InputStream in = null;
	private OutputStream out = null;
	private boolean useSSL = true;

	public NxSocket(boolean useSSL) {
		this.useSSL = useSSL;
	}

	public void connect(String host, int port) throws Exception {
		if ((host == null) || (host.equals(""))) {
			throw new RuntimeException("Host and port must be defined");
		}

		if (useSSL) {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, NO_OP_TRUST_MANAGER, new SecureRandom());
			SSLSocketFactory sslsocketfactory = sslContext.getSocketFactory();
			this.socket = sslsocketfactory.createSocket(host, port); 
		} else {
			this.socket = new Socket(host, port);
		}

		this.in = this.socket.getInputStream();
		this.out = this.socket.getOutputStream();
	}

	public void disconnect() {
		if ((this.socket != null) && (!this.socket.isClosed())) {
			try {
				this.socket.close();
			} catch (IOException e) {
				StatusManager.getManager().handle(
						new Status(2, "com.compuware.topazc", 0,
								"Error closing connection to Topaz Connect", e),
								1);
			}
		}
		this.socket = null;
		this.in = null;
		this.out = null;
	}

	public InputStream getInputStream() {
		return this.in;
	}

	public OutputStream getOutputStream() {
		return this.out;
	}

	public boolean isConnected() {
		if ((this.socket != null) && (this.socket.isConnected())) {
			return true;
		}
		return false;
	}


	TrustManager[] NO_OP_TRUST_MANAGER = new TrustManager[] { 
			new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return new java.security.cert.X509Certificate[0];
				}

				@Override
				public void checkClientTrusted(
						java.security.cert.X509Certificate[] arg0, String arg1)
								throws CertificateException {

				}

				@Override
				public void checkServerTrusted(
						java.security.cert.X509Certificate[] arg0, String arg1)
								throws CertificateException {

				}
			}
	};

}
