package com.alibaba.datax.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class HttpClientUtils {

    private static Log logger = LogFactory.getLog(HttpClientUtil.class);

    // private static volatile HttpClient httpClient = null;

    /**
     * Constructor
     */
    private HttpClientUtils() {

    }

    /**
     * Send post to URL with parameters.
     *
     * @param url          url
     * @param parameterMap parameterMap
     * @return result content
     * @throws Exception Exception
     */
    public static String post(String url, Map<String, String> parameterMap) throws Exception {
        return post(url, parameterMap, null, "UTF-8");
    }

    /**
     * Send post to URL with parameters and headers by given encoding.
     *
     * @param url          url
     * @param parameterMap parameterMap
     * @param headerMap    headerMap
     * @param encoding     encoding
     * @return result content
     * @throws Exception Exception
     */
    public static String post(String url, Map<String, String> parameterMap, Map<String, String> headerMap, String encoding)
            throws Exception {
        StringEntity entity = null;

        if (parameterMap != null && !parameterMap.isEmpty()) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            for (Entry<String, String> entry : parameterMap.entrySet()) {
                params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }

            try {
                entity = new StringEntity(URLEncodedUtils.format(params, encoding));
                entity.setContentType(URLEncodedUtils.CONTENT_TYPE);
            } catch (UnsupportedEncodingException e) {
                logger.error("Encode the parameter failed!", e);
            }
        }

        return postWithEntity(url, entity, headerMap);
    }

    private static String postWithEntity(String url, HttpEntity entity, Map<String, String> headerMap) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        if (entity != null) {
            httpPost.setEntity(entity);
        }
        if (headerMap != null && !headerMap.isEmpty()) {
            for (Entry<String, String> entry : headerMap.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }
        HttpResponse response = getHttpClient().execute(httpPost);
        return responseToString(response);
    }

    /**
     * Create an HttpClient with the ThreadSafeClientConnManager.
     *
     * @return
     */
    private static HttpClient getHttpClient() {
        HttpClient httpClient = null;
        HttpParams params = new BasicHttpParams();
        ConnManagerParams.setMaxTotalConnections(params, 500);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        // Create and initialize scheme registry
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

        ThreadSafeClientConnManager connectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);
        DefaultHttpClient defaultHttpClient = new DefaultHttpClient(connectionManager, params);
        HttpConnectionParams.setConnectionTimeout(defaultHttpClient.getParams(), 5000); // 5秒超时

        httpClient = defaultHttpClient;
        return httpClient;
    }

    /**
     * Create an HttpClient with the ThreadSafeClientConnManager.
     *
     * @return
     */
    private static HttpClient getHttpClientProxy() {
        HttpClient httpClient = null;
        String proxyip = "220.181.29.110";
        int proxyport = 1638;
        HttpParams params = new BasicHttpParams();
        ConnManagerParams.setMaxTotalConnections(params, 500);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        // Create and initialize scheme registry
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

        ThreadSafeClientConnManager connectionManager = new ThreadSafeClientConnManager(params, schemeRegistry);
        DefaultHttpClient defaultHttpClient = new DefaultHttpClient(connectionManager, params);
        HttpHost proxy = new HttpHost(proxyip, proxyport);
        defaultHttpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        HttpConnectionParams.setConnectionTimeout(defaultHttpClient.getParams(), 5000); // 5秒超时

        // 增加gzip支持
        // defaultHttpClient.addRequestInterceptor(new AcceptEncodingRequestInterceptor());
        // defaultHttpClient.addResponseInterceptor(new ContentEncodingResponseInterceptor());

        httpClient = defaultHttpClient;
        return httpClient;
    }

    private static String responseToString(HttpResponse response) throws Exception {
        HttpEntity entity = getHttpEntity(response);
        if (entity == null) {
            return null;
        }
        return EntityUtils.toString(entity, "UTF-8");
    }

    private static HttpEntity getHttpEntity(HttpResponse response) throws Exception {
        StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() >= 300) {
            // EntityUtils.consume(response.getEntity());
            throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
        } else {
            return response.getEntity();
        }
    }

    /**
     * Send get to URL.
     *
     * @param url url
     * @return result content
     */
    public static String get(String url) {
        try {
            HttpGet httpGet = new HttpGet(url);
            // 修改成使用带代理的
            HttpResponse response = getHttpClientProxy().execute(httpGet);
            return responseToString(response);
        } catch (Exception e) {
            logger.error("Send Get request to url faild, url: " + url, e);
        }
        return null;
    }
}
