package cz.duong.skolar.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.duong.skolar.R;
import cz.duong.skolar.server.Users;
import cz.duong.skolar.utils.CoreUtils;
import cz.duong.skolar.utils.UrlRequest;

/**
 * Created by David on 9. 4. 2014.
 */
public class ZnamkyFragment extends Fragment implements UrlRequest.RequestComplete {



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_znamky, container, false);

        return rootView;
    }

    public void dataLoaded(JSONObject object) throws JSONException {
        if(this.isAdded()) {
            object.getJSONObject("data").put("averages", this.calculateAddons(object));

            ExpandableListView lv = (ExpandableListView) this.getView().findViewById(R.id.znamky_list);
            ExpandableListAdapter adapter = new ZnamkyAdapter(this.getActivity(), object);

            lv.setAdapter(adapter);

        }
    }

    private JSONArray calculateAddons(JSONObject source) {
        try {
            JSONArray array = new JSONArray();
            JSONArray znamky = source.getJSONObject("data").getJSONArray("znamky");



            Integer sum;
            Integer cnt;

            for (int i = 0; i < znamky.length(); i++) {
                JSONArray marks = znamky.getJSONArray(i);
                JSONObject average = new JSONObject();

                sum = 0;
                cnt = 0;

                for(int y = 0; y < marks.length(); y++) {
                    JSONObject mark = marks.getJSONObject(y);

                    if(CoreUtils.isNumeric(mark.getString("mark"))) {
                        sum += Integer.parseInt(mark.getString("mark"));
                        cnt++;
                    }
                }

                Double avg = (double) sum / cnt;
                Integer fin = (int) Math.round(avg);

                switch(fin) {
                    case 1:
                        average.put("color", R.color.mark_1);
                        break;
                    case 2:
                        average.put("color", R.color.mark_2);
                        break;
                    case 3:
                        average.put("color", R.color.mark_3);
                        break;
                    case 4:
                        average.put("color", R.color.mark_4);
                        break;
                    case 5:
                        average.put("color", R.color.mark_5);
                        break;
                }


                average.put("average", avg);
                array.put(i, average);
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
        //request.putString("file", "klasifikace-pokrocily.html");
        request.putParcelable("user", new Users().getCurrentUser());

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

    public class ZnamkyAdapter extends BaseExpandableListAdapter {

        private Context context;
        private JSONObject data;

        private JSONArray predmety;
        private JSONArray znamky;
        private JSONArray averages;

        private LayoutInflater inflater;

        public ZnamkyAdapter(Context context, JSONObject data) {
            try {
                this.context = context;
                this.data = data;

                this.predmety = this.data.getJSONObject("data").getJSONArray("predmety");
                this.znamky = this.data.getJSONObject("data").getJSONArray("znamky");
                this.averages = this.data.getJSONObject("data").getJSONArray("averages");

                this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            } catch (JSONException e) {
                Log.d("SKOLAR-DB", "error");
                e.printStackTrace();
            }
        }

        @Override
        public int getGroupCount() {
            return predmety.length();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return ((JSONArray) this.getGroup(groupPosition)).length();
        }

        @Override
        public Object getGroup(int groupPosition) {
            try {
                return znamky.getJSONArray(groupPosition);
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
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            try {
                if (convertView == null) {
                    convertView = inflater.inflate(android.R.layout.simple_list_item_2, null);
                }

                TextView lesson = (TextView) convertView.findViewById(android.R.id.text1);
                lesson.setText(predmety.getString(groupPosition));

                TextView average = (TextView) convertView.findViewById(android.R.id.text2);
                average.setText(String.format("%.2f", averages.getJSONObject(groupPosition).getDouble("average")));
                average.setTextColor(
                        this.context.getResources().getColor(averages.getJSONObject(groupPosition).getInt("color")));

                return convertView;

            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            try {
                if (convertView == null) {
                    convertView = inflater.inflate(android.R.layout.simple_list_item_2, null);
                }

                JSONObject mark = (JSONObject) this.getChild(groupPosition, childPosition);
                TextView mark_text = (TextView) convertView.findViewById(android.R.id.text1);
                TextView caption_text = (TextView) convertView.findViewById(android.R.id.text2);

                mark_text.setText(mark.getString("mark"));
                caption_text.setText(mark.getString("caption"));

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

   /* public class ZnamkyAdapter extends BaseAdapter {

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
                    convertView = this.inflater.inflate(R.layout.znamky_item, null);
                }

                String name = data.getJSONArray("predmety").get(position).toString();

                TextView subject = (TextView) convertView.findViewById(R.id.znamky_subject);
                TextView shorted = (TextView) convertView.findViewById(R.id.znamky_shortedSubject);
                TextView average = (TextView) convertView.findViewById(R.id.znamky_average);

                subject.setText(name);
                shorted.setText(SchoolUtils.shortenSubject(name));
                shorted.setBackgroundColor(SchoolUtils.subjectToColor(name));

                JSONObject avg_data =  data.getJSONArray("averages").getJSONObject(position);

                average.setText(String.format("%.2f", avg_data.getDouble("average")));
                average.setTextColor(this.context.getResources().getColor(avg_data.getInt("color")));

                return convertView;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;

            }
        }
    }*/
}
