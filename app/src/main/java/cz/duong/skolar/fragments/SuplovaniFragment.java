package cz.duong.skolar.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.duong.skolar.R;
import cz.duong.skolar.utils.UrlRequest;

public class SuplovaniFragment extends ListFragment implements UrlRequest.RequestComplete {

    public SuplovaniFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();

        Bundle request = new Bundle();
        request.putString("page", "suplovani");
        request.putString("file", "suplovani-zaklad.htm");

        new UrlRequest(this).execute(request);
    }

    @Override
    public void onRequestComplete(JSONObject result) {
        this.dataFinished(result);
    }

    public void dataFinished(JSONObject object) {
        this.setListAdapter(new SuplovaniAdapter(this.getActivity(), object));
    }

    public class SuplovaniAdapter extends BaseAdapter {

        private Context context;
        private JSONObject source;
        private LayoutInflater inflater;

        private List<Date> dates = new ArrayList<Date>();
        private List<CharSequence> changes = new ArrayList<CharSequence>();

        public SuplovaniAdapter(Context context, JSONObject source) {
            try {
                this.context = context;
                this.source = source.getJSONObject("data");

                this.inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                this.prepare(this.source.getJSONArray("suplovani"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private void prepare(JSONArray source) throws JSONException {
            for (int i = 0; i < source.length(); i++) {
                JSONObject day = source.getJSONObject(i);
                JSONArray changes = day.getJSONArray("changes");


                Long date = Long.parseLong(day.getString("date"))* 1000;

                Log.d("SKOLAR-DB", this.JSONToString(changes));
                String span = this.JSONToString(changes);




                this.changes.add(span);
                this.dates.add(new Date(date));
            }
        }

        private String JSONToString(JSONArray input) throws JSONException {
            StringBuilder builder = new StringBuilder();

            for(int y = 0; y < input.length(); y++) {
                builder.append(input.getString(y));
            }

            return builder.toString();
        }
        @Override
        public int getCount() {
            try {
                return source.getJSONArray("suplovani").length() * 2; //chceme ještě přidat ty dny
            } catch (JSONException e) {
                e.printStackTrace();
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {

            if(this.getItemViewType(position) == 0) {
                return dates.get((int) this.getItemId(position));
            } else {
                //return source.getJSONArray("suplovani").getJSONObject((int) this.getItemId(position));
                return changes.get((int) this.getItemId(position));
            }

        }

        @Override
        public long getItemId(int position) {
            Log.d("SKOLAR-DB", "+" + Integer.toString(position));

            if(this.getItemViewType(position) == 1) {
                position--;
            }

            return position / 2;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public int getItemViewType(int position) {
            return position % 2;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = this.inflater.inflate(android.R.layout.simple_list_item_1, null);
            }


            TextView text = (TextView) convertView.findViewById(android.R.id.text1);

            Log.d("SKOLAR-DB", position + " -> "+ this.getItemViewType(position) + " -> "+ (int)this.getItemId(position));
            if(this.getItemViewType(position) == 0) {
                DateFormat format = android.text.format.DateFormat.getDateFormat(this.context);
                Date date = (Date) this.getItem(position);
                text.setText(format.format(date));
            } else {

                text.setText(Html.fromHtml(this.getItem(position)+"<br />"));

            }


            return convertView;

        }
    }
}
