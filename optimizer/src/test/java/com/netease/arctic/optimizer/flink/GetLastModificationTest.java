package com.netease.arctic.optimizer.flink;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GetLastModificationTest {

    private static final String JOB_OVERVIEW_REST_API = "/jobs/overview";

    private static final String LAST_MODIFICATION = "last-modification";

    private static final String STATUS_IDENTIFICATION = "status_identification";

    public static void main(String[] args) throws IOException {


        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(new Configuration());
        //a application id
        String yarnApp = "application_1676556061024_178049";
        String restApiPrefix = "http://xxxxxx";


        if (StringUtils.isNotEmpty(restApiPrefix) && StringUtils.isNotEmpty(yarnApp)) {
            //http://xxxxxx/{application_id}/jobs/overview
            String restApiUrl = restApiPrefix + yarnApp + JOB_OVERVIEW_REST_API;

            URL url = new URL(restApiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.connect();
            StringBuilder sb = new StringBuilder();

            if (conn.getResponseCode() == 200) {
                InputStream is = conn.getInputStream();
                //面对获取的输入流进行读取
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                String jsonStr = sb.toString();
                JSONArray jobs = JSON.parseObject(jsonStr).getJSONArray("jobs");
                Long lastModification = jobs.getJSONObject(0).getLong(LAST_MODIFICATION);

                System.out.println(STATUS_IDENTIFICATION + " , " + lastModification);

            }

            conn.disconnect();
        }
    }
}
