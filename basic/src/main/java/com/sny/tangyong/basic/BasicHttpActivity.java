package com.sny.tangyong.basic;

import android.app.Activity;
import android.app.DownloadManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class BasicHttpActivity extends Activity {

    private TextView text;

    private static final String HTTPS_TCL_SERVER = "https://gstest.udc.cn.tclclouds.com/api/device/log";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_http);

        initComponent();
    }


    private void initComponent() {

        text = (TextView) findViewById(R.id.http_text);

        findViewById(R.id.btn_https).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // GetHttps();
                getHttpsVersionTwo();
            }
        });
    }

    private void getHttpsVersionTwo() {

        String result = "";
        Log.i("控制", HTTPS_TCL_SERVER);
        DownloadManager.Query obj = new DownloadManager.Query();
        HttpURLConnection http = null;
        URL url;
        try {
            url = new URL(HTTPS_TCL_SERVER);
            // 判断是http请求还是https请求
            if (url.getProtocol().toLowerCase().equals("https")) {
                trustAllHosts();
                http = (HttpsURLConnection) url.openConnection();
                ((HttpsURLConnection) http).setHostnameVerifier(DO_NOT_VERIFY);// 不进行主机名确认
            } else {
                http = (HttpURLConnection) url.openConnection();
            }

            http.setConnectTimeout(10000);// 设置超时时间
            http.setReadTimeout(50000);
            http.setRequestMethod("POST");// 设置请求类型为post
            http.setDoInput(true);
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "text/xml");
            // http.setRequestProperty("Cookie", DataDefine.mCookieStore);
            DataOutputStream out = new DataOutputStream(http.getOutputStream());
            // out.writeBytes(base64);
            out.flush();
            out.close();
            // obj.setHttpStatus(http.getResponseCode());// 设置http返回状态200还是403
            BufferedReader in = null;
            // if (obj.getHttpStatus() == 200) {
            // getCookie(http);
            // in = new BufferedReader(new InputStreamReader(http.getInputStream()));
            // } else
            in = new BufferedReader(new InputStreamReader(http.getErrorStream()));
            result = in.readLine();// 得到返回结果
            in.close();
            http.disconnect();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /** 得到cookie */
    private static void getCookie(HttpURLConnection http) {
        /*
         * String cookieVal = null; String key = null; DataDefine.mCookieStore = ""; for (int i = 1;
         * (key = http.getHeaderFieldKey(i)) != null; i++) { if (key.equalsIgnoreCase("set-cookie"))
         * { cookieVal = http.getHeaderField(i); cookieVal = cookieVal.substring(0,
         * cookieVal.indexOf(";")); DataDefine.mCookieStore = DataDefine.mCookieStore + cookieVal +
         * ";"; } }
         */
    }

    private void GetHttps() {

        String https = "https://gstest.udc.cn.tclclouds.com/api/device/log";

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[] {new MyTrustManager()}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new MyHostnameVerifier());
            HttpsURLConnection conn = (HttpsURLConnection) new URL(https).openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null)
                sb.append(line);

            text.setText(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class MyHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            // TODO Auto-generated method stub

            Logger.d("MyHostnameVerifier" + hostname);

            return true;
        }
    }


    /**
     * 证书
     */
    class MyTrustManager implements X509TrustManager {

        public X509Certificate[] getAcceptedIssuers() {
            // return null;
            return new X509Certificate[] {};
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            // TODO Auto-generated method stub

        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            // TODO Auto-generated method stub
            // System.out.println("cert: " + chain[0].toString() + ", authType: "
            // + authType);
        }
    }

    TrustManager[] xtmArray = new MyTrustManager[] {new MyTrustManager()};

    /**
     * 信任所有主机-对于任何证书都不做检查
     */
    private void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        // Android 采用X509的证书信息机制
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, xtmArray, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(DO_NOT_VERIFY);//
            // 不进行主机名确认
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            // TODO Auto-generated method stub
            System.out.println("Warning: URL Host: " + hostname + " vs. " + session.getPeerHost());
            return true;
        }
    };



}
