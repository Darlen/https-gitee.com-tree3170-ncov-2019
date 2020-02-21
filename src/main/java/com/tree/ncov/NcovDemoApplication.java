package com.tree.ncov;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * @author tree
 */
@EnableScheduling
@SpringBootApplication(exclude = MongoAutoConfiguration.class)
public class NcovDemoApplication {


    public static void main(String[] args) throws IOException {
        SpringApplication.run(NcovDemoApplication.class, args);
    }

    @Value("${ncov.proxy-server}")
    private String proxyServer;

    @Value("${ncov.proxyable}")
    private boolean proxyable;

    @Bean
    RestTemplate restTemplate() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        RestTemplate restTemplate = null;
        if(proxyable){
            restTemplate = new RestTemplate(generateHttpRequestFactory());
        }else {
            restTemplate = new RestTemplate();
        }
        return restTemplate;
    }

    /**
     * 构建request
     *
     * 1. 代理
     * 2. 代理认证
     * 3. 绕过https （证书）
     * 4. 构建request基本超时信息
     * @return
     */
    private HttpComponentsClientHttpRequestFactory generateHttpRequestFactory()
            throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException{

        //信任证书
        TrustStrategy acceptingTrustStrategy = (x509Certificates, authType) -> true;
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        //不校验host
        SSLConnectionSocketFactory connectionSocketFactory = new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier());

        String proxyUser = "XXX";
        String proxyPwd = "XXX";
        HttpHost proxy = new HttpHost(proxyServer,8080);
        //设置proxy的认证信息
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(new AuthScope(proxy),new UsernamePasswordCredentials(proxyUser,proxyPwd));

        //设置request相关信息
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(5000)
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setStaleConnectionCheckEnabled(true)
                .build();

        //构建httpclient
        CloseableHttpClient httpClient = HttpClients.custom()
                //设置proxy认证信息
                .setDefaultCredentialsProvider(provider)
                //设置proxy
                .setProxy(proxy)
                //设置ssl
                .setSSLSocketFactory(connectionSocketFactory)
                .setDefaultRequestConfig(defaultRequestConfig)
                .build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient);
        return factory;
    }





}
