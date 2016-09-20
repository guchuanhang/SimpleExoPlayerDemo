//: reusing/CADSystem.java
// Ensuring proper cleanup.
package com.google.android.exoplayer.demo.com.google.android.exoplayer.bean;

import com.google.android.exoplayer.demo.player.WelcomeActivity;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class QueryLiveProgramList {
    public static final String BASE_URL = "http://www.dingdongfm.com/DingDongFM/servlet/MobileServlet";

    public static String getLiveAddress(String liveType) {
        Map<String, String> map = new HashMap<>();
        map.put("Filter", formQueryCondition());
        map.put("ObjectName", "D_Media");
        map.put("OrderString", "");
        map.put("OrderAsc", "");
        map.put("StartRow", "0");
        map.put("EndRow", "20");
        map.put("OrderType", "");
        map.put("model", "" + 18);
        String queryResult = doPost(BASE_URL, map);
        System.out.println(queryResult);
        ResponseBean responseBean = new Gson().fromJson(queryResult, ResponseBean.class);
        if ("0".equals(responseBean.getErrorCode())) {
            Type type = new TypeToken<List<MediaBean>>() {
            }.getType();
            List<MediaBean> mediaList = new Gson().fromJson(responseBean.getResponseObject().get("D_Media"), type);
            if (WelcomeActivity.TYPE_QINGTING.equals(liveType)) {
                return mediaList.get(0).getMediaUrl();
            }
            return mediaList.get(0).getMediaDynamicUrl();
        }
        return null;
    }

    public static String formQueryCondition() {
        JsonObject filter = new JsonObject();

        JsonArray andParams = new JsonArray();
        JsonObject favorUser = new JsonObject();

        favorUser.addProperty("column", "MediaType");
        favorUser.addProperty("value", "0");
        favorUser.addProperty("operation", "1");
        andParams.add(favorUser);
        JsonObject activityObject = new JsonObject();
        activityObject.addProperty("column", "Show");
        activityObject.addProperty("value", "0");
        activityObject.addProperty("operation", "1");
        andParams.add(activityObject);
        // finish
        filter.add("and", andParams);
        return filter.toString();
    }

    public static String doPost(String urlInString,
                                Map<String, String> paramsMap) {
        StringBuilder stringBuilder = new StringBuilder();

        if (paramsMap != null && paramsMap.size() > 0) {
            Iterator<Map.Entry<String, String>> iterator = paramsMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                try {
                    stringBuilder.append(URLEncoder.encode(entry.getKey(), "utf-8"));
                    stringBuilder.append("=");
                    if ((entry.getValue() != null && !"".equals(entry.getValue()))) {
                        stringBuilder.append(URLEncoder.encode(entry.getValue(), "utf-8"));
                    }
                    stringBuilder.append("&");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        }
        try {
            URL url = new URL(urlInString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setConnectTimeout(5 * 1000);
            conn.setRequestMethod("POST");
            String paramsContent = stringBuilder.toString();
            if ((paramsContent != null && !"".equals(paramsContent))) {
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(wr, "UTF-8"));
                writer.write(paramsContent);
                writer.close();
                wr.close();
            }

            InputStream inStream = conn.getInputStream();
            if (conn != null && conn.getResponseCode() == 200) {
                byte[] buffer = new byte[1024 * 8];
                int readedByte = -1;
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                while ((readedByte = inStream.read(buffer)) > 0) {
                    byteStream.write(buffer, 0, readedByte);
                }
                byte[] resultInByte = byteStream.toByteArray();
                String resultInString = new String(resultInByte, "UTF-8");
                if (resultInString.startsWith("{")) {
                    return resultInString;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}

