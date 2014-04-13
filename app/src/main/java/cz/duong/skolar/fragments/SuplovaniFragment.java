package cz.duong.skolar.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.BulletSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.devspark.progressfragment.ProgressFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.duong.skolar.R;
import cz.duong.skolar.server.Users;
import cz.duong.skolar.utils.UrlRequest;

public class SuplovaniFragment extends ProgressFragment implements UrlRequest.RequestComplete {

    private View contentView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setContentView(contentView);
        setEmptyText(R.string.data_empty);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_main, container, false);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();

        Bundle request = new Bundle();
        request.putString("page", "suplovani");
        //request.putString("file", "suplovani-zaklad.htm");

        request.putParcelable("user", new Users(this.getActivity()).getCurrentUser());

        this.setContentShown(false);
        new UrlRequest(this).execute(request);
    }

    @Override
    public void onRequestComplete(JSONObject result) {
        this.dataFinished(result);
    }

    public void dataFinished(JSONObject object) {
        if(isAdded()) {

            this.setContentShown(true);

            ListView lv = (ListView) this.getView().findViewById(android.R.id.list);
            lv.setAdapter(new SuplovaniAdapter(this.getActivity(), object));
        }
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

                CharSequence span = this.JSONToString(changes);

                this.changes.add(span);
                this.dates.add(new Date(date));
            }
        }

        private CharSequence JSONToString(JSONArray input) throws JSONException {

            CharSequence result = "";

            for(int y = 0; y < input.length(); y++) {
                String change = input.getString(y);

                SpannableString string = new SpannableString(change);
                string.setSpan(new BulletSpan(15), 0, change.length(), 0);


                if(result.length() == 0) {
                    result = TextUtils.concat(result, string);
                } else {
                    result = TextUtils.concat(result, "\n", string);
                }
            }

            return result;


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
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return position % 2;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {

                if(this.getItemViewType(position) == 0) {
                    convertView = this.inflater.inflate(R.layout.day_item, null);
                } else {
                    convertView = this.inflater.inflate(R.layout.suplovani_item, null);
                }

            }



            if(this.getItemViewType(position) == 0) {
                TextView text = (TextView) convertView.findViewById(R.id.day);

                DateFormat format = android.text.format.DateFormat.getDateFormat(this.context);
                Date date = (Date) this.getItem(position);
                text.setText(format.format(date));
            } else {
                TextView text = (TextView) convertView.findViewById(R.id.suplovani_data);

                text.setText((SpannedString) getItem(position));
            }


            return convertView;

        }
    }
}
