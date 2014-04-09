package cz.duong.skolar.server;

import android.os.AsyncTask;

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

/**
 * Created by David on 15.3.14.
 */
public class UrlRequest extends AsyncTask<String, Void, JSONObject> {
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

    public String request(String page) throws IOException {

        HttpURLConnection connection = client.open(constructURL(page));
        OutputStream out = null;
        InputStream in = null;

        try {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("file", "klasifikace-pokrocily.html"));

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

    public JSONObject stringToJSON(String string) {
        try {
            return new JSONObject(string);
        } catch (JSONException e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        try {
            return this.stringToJSON(this.request(params[0]));
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
