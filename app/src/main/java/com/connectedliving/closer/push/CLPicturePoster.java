package com.connectedliving.closer.push;

import android.util.Log;

import com.connectedliving.closer.Configuration;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.client.config.RequestConfig;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;

public class CLPicturePoster {

    private static final int TIMEOUT = 20000;

    public static void sendPicture(byte[] image) throws Exception {
        Configuration configuration = Configuration.getInstance();
        Log.d("Poster ", "Sending picture");
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(TIMEOUT)
                .setConnectionRequestTimeout(TIMEOUT)
                .setSocketTimeout(TIMEOUT).build();
        CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
        String uri = ClConnection.getBuilder()
                .appendPath("picture")
                .appendQueryParameter("token", NetData.getInstance().getToken())
                .appendQueryParameter("facility", configuration.getFacility())
                .appendQueryParameter("robot", configuration.getDisplayName())
                .appendQueryParameter("robotId", Integer.toString(configuration.getRobotId()))
                .build().toString();
        HttpPost httpPost = new HttpPost(uri);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addBinaryBody(
                "file", image, ContentType.APPLICATION_OCTET_STREAM, "file.ext");
        HttpEntity multipart = builder.build();
        httpPost.setEntity(multipart);
        client.execute(httpPost);
        client.close();
    }
}
