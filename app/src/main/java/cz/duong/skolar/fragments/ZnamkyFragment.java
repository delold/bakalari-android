package cz.duong.skolar.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.duong.skolar.R;
import cz.duong.skolar.utils.UrlRequest;
import cz.duong.skolar.utils.CoreUtils;
import cz.duong.skolar.utils.SchoolUtils;

/**
 * Created by David on 9. 4. 2014.
 */
public class ZnamkyFragment extends ListFragment implements UrlRequest.RequestComplete {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        return rootView;
    }

    public void dataLoaded(JSONObject object) throws JSONException {
        if(this.isAdded()) {

            object.getJSONObject("data").put("averages", this.calculateAverages(object));
            this.setListAdapter(new ZnamkyAdapter(this.getActivity(), object));
        }
    }

    private JSONArray calculateAverages(JSONObject source) {
        try {
            JSONArray array = new JSONArray();
            JSONArray znamky = source.getJSONObject("data").getJSONArray("znamky");

            Integer sum;
            Integer cnt;

            for (int i = 0; i < znamky.length(); i++) {
                JSONArray marks = znamky.getJSONArray(i);

                sum = 0;
                cnt = 0;

                for(int y = 0; y < marks.length(); y++) {
                    JSONObject mark = marks.getJSONObject(y);

                    if(CoreUtils.isNumeric(mark.getString("mark"))) {
                        sum += Integer.parseInt(mark.getString("mark"));
                        cnt++;
                    }
                }

                array.put(i, (double)sum / cnt);
            }

            return array;
        } catch (JSONException e) {
            return null;
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        Bundle request = new Bundle();
        request.putString("page", "znamky");
        request.putString("file", "klasifikace-pokrocily.html");

        new UrlRequest(this).execute(request);
    }
    @Override
    public void onRequestComplete(JSONObject result) {
        try {
            this.dataLoaded(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public class ZnamkyAdapter extends BaseAdapter {

        private Context context;
        private JSONObject data;

        private LayoutInflater inflater;

        public ZnamkyAdapter(Context ctx, JSONObject data) {
            try {
                this.context = ctx;
                this.data = data.getJSONObject("data");
                this.inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            } catch (JSONException e) {
                Log.d("SKOLAR-DB", "error");
                e.printStackTrace();
            }
        }

        @Override
        public int getCount() {
            try {
                return data.getJSONArray("znamky").length();
            } catch (JSONException e) {
                e.printStackTrace();
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {
            try {
                return data.getJSONArray("znamky").get(position);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            try {
                if (convertView == null) {
                    convertView = this.inflater.inflate(R.layout.rozvrh_item, null);
                }

                String name = data.getJSONArray("predmety").get(position).toString();

                TextView subject = (TextView) convertView.findViewById(R.id.rozvrh_subject);
                TextView shorted = (TextView) convertView.findViewById(R.id.rozvrh_shortedSubject);
                TextView average = (TextView) convertView.findViewById(R.id.rozvrh_average);

                subject.setText(name);
                shorted.setText(SchoolUtils.shortenSubject(name));
                shorted.setBackgroundColor(SchoolUtils.subjectToColor(name));

                average.setText(String.format("%.2f", data.getJSONArray("averages").getDouble(position)));

                return convertView;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;

            }
        }
    }
}
