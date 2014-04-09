package cz.duong.skolar.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import cz.duong.skolar.R;
import cz.duong.skolar.server.UrlRequest;

/**
 * Created by David on 9. 4. 2014.
 */
public class ZnamkyFragment extends Fragment implements UrlRequest.RequestComplete {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.section_label);

        new UrlRequest(this).execute("znamky");

        return rootView;
    }

    public void dataLoaded(JSONObject object) {

    }

    public void dataFailed() {

    }

    @Override
    public void onRequestComplete(JSONObject result) {
        try {
            Log.d("SKOLAR-DB", result.getString("status"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
