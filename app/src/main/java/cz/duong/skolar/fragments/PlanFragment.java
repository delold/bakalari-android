package cz.duong.skolar.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.devspark.progressfragment.ProgressFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;

import cz.duong.skolar.R;
import cz.duong.skolar.server.Users;
import cz.duong.skolar.utils.CoreUtils;
import cz.duong.skolar.utils.UrlRequest;

/**
 * Created by David on 9. 4. 2014.
 */
public class PlanFragment extends ProgressFragment implements UrlRequest.RequestComplete {

    private View contentView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setContentView(contentView);
        setEmptyText(R.string.data_empty);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView = inflater.inflate(R.layout.fragment_plan, container, false);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void dataLoaded(JSONObject object) throws JSONException {
        if(this.isAdded()) {
            setContentShown(true);

            //seřadit objekty jinak
            object = this.sortToDay(object.getJSONObject("data").getJSONArray("akce"));

            int dp = CoreUtils.convertDPtoPX(this.getResources(), 40+8);

            ExpandableListView lv = (ExpandableListView) this.getView().findViewById(R.id.plan_list);

            ExpandableListAdapter adapter = new PlanAdapter(this.getActivity(), object);

            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                lv.setIndicatorBounds(lv.getRight() - dp, lv.getWidth());
            } else {
                lv.setIndicatorBoundsRelative(lv.getRight() - dp, lv.getWidth());
            }

            lv.setAdapter(adapter);
        }
    }

    public JSONObject sortToDay(JSONArray obj) throws JSONException {
        JSONObject days = new JSONObject();

        for(int x = 0; x < obj.length(); x++) {
            JSONObject event = obj.getJSONObject(x);

            //TODO: ... upravit, aby to pochopil i neandrtalec
            if(!days.has(event.getJSONObject("time").getString("date"))) {
                days.put(event.getJSONObject("time").getString("date"), new JSONArray().put(event));
            } else {
                days.put(event.getJSONObject("time").getString("date"),
                        days.getJSONArray(
                                event.getJSONObject("time").getString("date"))
                                .put(event)
                );
            }
        }

        Log.d("SKOLAR-DB", days.toString());
        return days;

    }


    @Override
    public void onResume(){
        super.onResume();

        Bundle request = new Bundle();
        request.putString("page", "akce");
        //request.putString("file", "akce-zaklad.html");

        request.putParcelable("user", new Users(this.getActivity()).getCurrentUser());

        setContentShown(false);
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

    public class PlanAdapter extends BaseExpandableListAdapter {

        private Context context;
        private JSONObject data;

        private LayoutInflater inflater;

        public PlanAdapter(Context context, JSONObject data) {
            this.context = context;
            this.data = data;

            this.inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getGroupCount() {
            return data.length();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return ((JSONArray) this.getGroup(groupPosition)).length();
        }

        @Override
        public Object getGroup(int groupPosition) {
            try {
                return data.getJSONArray(data.names().getString(groupPosition));
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            try {
                return ((JSONArray) this.getGroup(groupPosition)).getJSONObject(childPosition);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public long getGroupId(int groupPosition) {
            try {
                return Long.parseLong(data.names().getString(groupPosition));
            } catch (JSONException e) {
                e.printStackTrace();
                return 0L;
            }
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition; //... co čekám...
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = inflater.inflate(R.layout.plan_label, null);
            }

            TextView day_text = (TextView) convertView.findViewById(R.id.plan_day);
            DateFormat format = android.text.format.DateFormat.getDateFormat(this.context);
            day_text.setText(format.format(new Date(this.getGroupId(groupPosition) * 1000)));

            return convertView;
        }

        public SpannableStringBuilder createKeyValueText(String label, String value) {
            final SpannableStringBuilder builder = new SpannableStringBuilder(label+": "+value);
            final StyleSpan span = new StyleSpan(Typeface.BOLD);
            builder.setSpan(span, 0, label.length()+1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

            return builder;
        }

        public String convertArrayToString(JSONArray array, String divider) throws JSONException {
            StringBuilder builder = new StringBuilder();

            for(int i = 0; i < array.length(); i++) {
                builder.append(array.getString(i));

                if(i != array.length()-1) {
                    builder.append(divider);
                }
            }

            return builder.toString();

        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            try {
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.plan_item, null);
                }

                JSONObject event = (JSONObject) this.getChild(groupPosition, childPosition);

                TextView name_text = (TextView) convertView.findViewById(R.id.plan_name);
                TextView class_text = (TextView) convertView.findViewById(R.id.plan_class);
                TextView teacher_text = (TextView) convertView.findViewById(R.id.plan_teacher);
                TextView time_text = (TextView) convertView.findViewById(R.id.plan_time);
                TextView place_text = (TextView) convertView.findViewById(R.id.plan_place);
                TextView detail_text = (TextView) convertView.findViewById(R.id.plan_detail);



                if(!event.has("name")) {
                    name_text.setVisibility(View.GONE);
                } else {
                    name_text.setText(event.getString("name"));
                }

                if(!event.has("class")) { //hhehe
                    class_text.setVisibility(View.GONE);
                } else {
                    class_text.setText(
                        this.createKeyValueText("Třídy",
                            this.convertArrayToString(event.getJSONArray("class"), ",")
                        )
                    );
                }

                if(!event.has("teacher")) {
                    teacher_text.setVisibility(View.GONE);
                } else {
                    teacher_text.setText(
                            this.createKeyValueText("Učitelé",
                                    this.convertArrayToString(event.getJSONArray("teacher"), ",")
                            )
                    );
                }

                if(!event.getJSONObject("time").has("time")) {
                    time_text.setVisibility(View.GONE);
                } else {
                    time_text.setText(
                        this.createKeyValueText("Čas",
                            this.convertArrayToString(event.getJSONObject("time").getJSONArray("time"), " - ")
                        )
                    );
                }

                if(!event.has("place")) {
                    place_text.setVisibility(View.GONE);
                } else {
                    place_text.setText(
                            this.createKeyValueText("Místo",
                                    event.getString("place")
                            )
                    );
                }


                if (!event.has("detail")) {
                    detail_text.setVisibility(View.GONE);
                } else {
                    detail_text.setText(event.getString("detail"));
                }

                return convertView;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }

    /*public class PlanAdapter extends BaseAdapter {

        private Context context;
        private JSONObject data;

        private LayoutInflater inflater;

        public PlanAdapter(Context ctx, JSONObject data) {
            this.context = ctx;
            this.data = data;
            this.inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {

            return data.length();

        }

        @Override
        public Object getItem(int position) {
            try {
                return data.getJSONArray(data.names().getString(position));
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            try {
                return Long.parseLong(data.names().getString(position));
            } catch (JSONException e) {
                e.printStackTrace();
                return 0L;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            try {
                if (convertView == null) {
                    convertView = this.inflater.inflate(R.layout.znamky_item, null);
                }

                String name = ((JSONArray) getItem(position)).getJSONObject(0).toString();

                Log.d("SKOLAR-DB-PLAN", name);

                return convertView;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;

            }
        }
    }*/
}
