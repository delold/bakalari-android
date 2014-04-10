package cz.duong.skolar.utils;

import android.os.AsyncTask;
import android.os.Bundle;

import com.squareup.okhttp.OkHttpClient;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import cz.duong.skolar.server.Users;

/**
 * Created by David on 15.3.14.
 */
public class UrlRequest extends AsyncTask<Bundle, Void, JSONObject> {
    public static String SERVER_HOST = "http://192.168.1.103/BakaParser/";

    private RequestComplete completeListener;
    private OkHttpClient client = new OkHttpClient();

    public UrlRequest(RequestComplete listener) {
        this.completeListener = listener;
    }

    public URL constructURL(String page) {
        try {
            return new URL(SERVER_HOST + page);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    public JSONObject stringToJSON(String string) {
        try {
            return new JSONObject(string);
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    private String request(String page, List<NameValuePair> params) throws IOException {

        HttpURLConnection connection = client.open(constructURL(page));
        OutputStream out = null;
        InputStream in = null;

        try {


            connection.setRequestMethod("POST");

            out = connection.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));

            writer.write(getQuery((params)));

            writer.flush();
            writer.close();
            out.close();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Unexpected HTTP response: "
                        + connection.getResponseCode() + " " + connection.getResponseMessage());
            }
            in = connection.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                total.append(line);
            }

            return total.toString();

        } finally {
            if(in != null) in.close();
            if(out != null) out.close();
        }
    }

    private String get(String page, Users.User user) throws IOException {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("user", user.user));
        params.add(new BasicNameValuePair("pass", user.pass));
        params.add(new BasicNameValuePair("url", user.url));

        return this.request(page, params);
    }

    private String get(String page, String debug) throws IOException {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("file", debug));

        return this.request(page, params);
    }

    @Override
    protected JSONObject doInBackground(Bundle... params) {
        try {
            Bundle request = params[0];

            if(!request.containsKey("page")) {
                throw new IOException("Not defined page");
            }

            if (request.containsKey("user")) {
                return this.stringToJSON(this.get(request.getString("user"),
                        (Users.User) request.getParcelable("user")));
            } else if (request.containsKey("file")) {
                return this.stringToJSON(this.get(request.getString("user"),
                        (Users.User) request.getParcelable("file")));
            } else {
                throw new IOException("Not defined parameter");

            }
        } catch (IOException e) {
            return new JSONObject();
        }
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        completeListener.onRequestComplete(result);
    }

    public interface RequestComplete {
        void onRequestComplete(JSONObject result);
    }
}
