/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.template.service;

import java.net.HttpURLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

@SuppressWarnings("unused")
public final class TrustModifier {

    private TrustModifier() {

    }

    private static final TrustingHostnameVerifier TRUSTING_HOSTNAME_VERIFIER = new TrustingHostnameVerifier();
    private static SSLSocketFactory factory;

    /**
     * Call this with any HttpURLConnection, and it will modify the trust settings if it is an HTTPS connection.
     */
    public static void relaxHostChecking(final HttpURLConnection conn)
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {

        if (conn instanceof HttpsURLConnection) {
            final HttpsURLConnection httpsConnection = (HttpsURLConnection) conn;
            final SSLSocketFactory factory = prepFactory(httpsConnection);
            httpsConnection.setSSLSocketFactory(factory);
            // Removed explicit setting of TrustingHostnameVerifier to use default secure hostname verification.
            // The default HttpsURLConnection hostname verifier performs proper validation.
        }
    }

    static synchronized SSLSocketFactory prepFactory(final HttpsURLConnection httpsConnection)
            throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        if (factory == null) {
            final SSLContext ctx = SSLContext.getInstance("TLS");
            // Initialize TrustManagerFactory to use the default system trust store
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init((KeyStore) null); // Use null to initialize with the default system KeyStore
            TrustManager[] trustManagers = tmf.getTrustManagers();
            ctx.init(null, trustManagers, null); // Use the default trust managers for secure certificate validation
            factory = ctx.getSocketFactory();
        }
        return factory;
    }

    private static final class TrustingHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(final String hostname, final SSLSession session) {
            return true;// NOSONAR
        }
    }

    private static final class AlwaysTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(final X509Certificate[] arg0, final String arg1) throws CertificateException {}// NOSONAR

        @Override
        public void checkServerTrusted(final X509Certificate[] arg0, final String arg1) throws CertificateException {}// NOSONAR

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}