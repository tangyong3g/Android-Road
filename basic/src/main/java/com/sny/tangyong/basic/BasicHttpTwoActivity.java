package com.sny.tangyong.basic;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.orhanobut.logger.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;


/**
 * The CA that issued the server certificate was unknown
 * The server certificate wasn't signed by a CA, but was self signed
 * The server configuration is missing an intermediate CA
 */
public class BasicHttpTwoActivity extends Activity {

    private static final String HTTPS_TCL_SERVER = "https://gstest.udc.cn.tclclouds.com/api/device/log";
    private static final String HTTPS_TEST_SEVER = "https://certs.cac.washington.edu/CAtest/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d("onCreate");
        setContentView(R.layout.activity_main);
        initComponent();
    }

    private void initComponent() {

        Logger.d("开始测试!");
        findViewById(R.id.btn_https_method_two).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            initSSLCertainWithHttpClient();
//                            initSSLAllWithHttpClient();
//                            TestHttp();
//                            testMethodTwo();
//                            initSSLALL();
//                            initSSL();
//                            initSSLCertainWithHttpClient();
//                            testMethodThreed();
                        } catch (Exception e) {
                            Log.e("HTTPS TEST", e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });
    }

    /**
     * HttpUrlConnection 方式，支持指定load-der.crt证书验证，此种方式Android官方建议
     *
     * @throws CertificateException
     * @throws IOException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public void initSSL() throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException,
            KeyManagementException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream in = getAssets().open("server.crt");
        Certificate ca = cf.generateCertificate(in);

        InputStream inKey = getAssets().open("server.key");

        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(inKey, null);
        keystore.setCertificateEntry("ca", ca);

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keystore);

        // Create an SSLContext that uses our TrustManager
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);
        URL url = new URL(HTTPS_TCL_SERVER);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        urlConnection.setSSLSocketFactory(context.getSocketFactory());
        InputStream input = urlConnection.getInputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        urlConnection.connect();
        Log.e("TTTT", result.toString() + "\t responseCode" + urlConnection.getResponseCode());
    }

    /**
     * HttpUrlConnection支持所有Https免验证，不建议使用
     *
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public void initSSLALL() throws KeyManagementException, NoSuchAlgorithmException, IOException {

        URL url = new URL(HTTPS_TCL_SERVER);
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[]{new TrustAllManager()}, null);
        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String arg0, SSLSession arg1) {
                Logger.d("免验证!。!!!");
                return true;
            }
        });
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.setDoOutput(false);
        connection.setRequestMethod("POST");
        connection.connect();
        InputStream in = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = "";
        StringBuffer result = new StringBuffer();
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        Log.e("TTTT", result.toString());
    }


    /**
     * HttpClient方式实现，支持所有Https免验证方式链接
     *
     * @throws ClientProtocolException
     * @throws IOException
     */
    public void initSSLAllWithHttpClient() throws ClientProtocolException, IOException {
        int timeOut = 30 * 1000;
        HttpParams param = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(param, timeOut);
        HttpConnectionParams.setSoTimeout(param, timeOut);
        HttpConnectionParams.setTcpNoDelay(param, true);

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        registry.register(new Scheme("https", TrustAllSSLSocketFactory.getDefault(), 443));
        ClientConnectionManager manager = new ThreadSafeClientConnManager(param, registry);
        DefaultHttpClient client = new DefaultHttpClient(manager, param);

        HttpGet request = new HttpGet("https://certs.cac.washington.edu/CAtest/");
        // HttpGet request = new HttpGet("https://www.alipay.com/");
        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();
        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        Log.e("HTTPS TEST", result.toString());
    }



    /**
     * HttpClient方式实现，支持验证指定证书
     *
     * @throws ClientProtocolException
     * @throws IOException
     */
    public void initSSLCertainWithHttpClient() throws ClientProtocolException, IOException {
        int timeOut = 30 * 1000;
        HttpParams param = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(param, timeOut);
        HttpConnectionParams.setSoTimeout(param, timeOut);
        HttpConnectionParams.setTcpNoDelay(param, true);

        SchemeRegistry registry = new SchemeRegistry();
        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        registry.register(new Scheme("https", TrustCertainHostNameFactory.getDefault(this), 443));
        ClientConnectionManager manager = new ThreadSafeClientConnManager(param, registry);
        DefaultHttpClient client = new DefaultHttpClient(manager, param);

        // HttpGet request = new
        // HttpGet("https://certs.cac.washington.edu/CAtest/");
        HttpGet request = new HttpGet(HTTPS_TCL_SERVER);
        HttpResponse response = client.execute(request);
        HttpEntity entity = response.getEntity();
        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        Log.e("HTTPS TEST", result.toString());
    }


    public class TrustAllManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            // TODO Auto-generated method stub

        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            // TODO Auto-generated method stub

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            // TODO Auto-generated method stub
            return null;
        }
    }


    private void TestHttp() {

        try {

            URL url = new URL("https://wikipedia.org");
            URLConnection urlConnection = url.openConnection();
            InputStream in = urlConnection.getInputStream();
            copyInputStreamToOutputStream(in, System.out);

        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ioEx) {
            ioEx.printStackTrace();
        }

    }


    /*********************************************************** from my self********************************************/


    /**
     * 没有证书不行,处理办法是加载证书官方的
     *
     * @throws CertificateException
     * @throws IOException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private void testMethodOne() throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

        // Load CAs from an InputStream
        // (could be from a resource or ByteArrayInputStream or ...)
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        // From https://www.washington.edu/itconnect/security/ca/load-der.crt
        InputStream caInput = new BufferedInputStream(new FileInputStream("load-der.crt"));
        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
        } finally {
            caInput.close();
        }

        // Create a KeyStore containing our trusted CAs
        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        // Create an SSLContext that uses our TrustManager
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);

        // Tell the URLConnection to use a SocketFactory from our SSLContext
        URL url = new URL("https://certs.cac.washington.edu/CAtest/");
        HttpsURLConnection urlConnection =
                (HttpsURLConnection) url.openConnection();
        urlConnection.setSSLSocketFactory(context.getSocketFactory());
        InputStream in = urlConnection.getInputStream();
        copyInputStreamToOutputStream(in, System.out);
    }


    /**
     * 信任所有的请求来源。裸奔。
     *
     * @throws CertificateException
     * @throws IOException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private void testMethodTwo() throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {


        // Create a TrustManager that trusts the CAs in our KeyStore
        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);


        // Create an SSLContext that uses our TrustManager
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[]{new TrustAllManager()}, null);

        // Tell the URLConnection to use a SocketFactory from our SSLContext
        URL url = new URL("https://certs.cac.washington.edu/CAtest/");
        HttpsURLConnection urlConnection =
                (HttpsURLConnection) url.openConnection();
        urlConnection.setSSLSocketFactory(context.getSocketFactory());
        InputStream in = urlConnection.getInputStream();
        copyInputStreamToOutputStream(in, System.out);
    }


    private void testMethodThreed() throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

        // Create an HostnameVerifier that hardwires the expected hostname.
        // Note that is different than the URL's hostname:
        // example.com versus example.org
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
//                HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
//                return hv.verify("certs.cac.washington.edu/CAtest/", session);
                return true;
            }
        };

        // Tell the URLConnection to use our HostnameVerifier
        URL url = new URL("https://certs.cac.washington.edu/CAtest/");
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        urlConnection.setHostnameVerifier(hostnameVerifier);
        InputStream in = urlConnection.getInputStream();
        copyInputStreamToOutputStream(in, System.out);
    }

    private void copyInputStreamToOutputStream(InputStream in, OutputStream os) throws IOException {

        byte[] buffer = new byte[1024];
        byte[] bufferOut = new byte[1024];

        while (in.read(buffer, 0, 1024) != -1) {
            os.write(bufferOut);
        }
    }


}
