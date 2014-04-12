package cz.duong.skolar.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.duong.skolar.R;
import cz.duong.skolar.utils.UrlRequest;

/**
 * Created by David on 9. 4. 2014.
 */
public class RozvrhFragment extends Fragment implements UrlRequest.RequestComplete {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_rozvrh, container, false);

        return rootView;
    }

    public void dataLoaded(JSONObject object) throws JSONException {
        if(this.isAdded()) {
            TableLayout layout = (TableLayout) this.getView().findViewById(R.id.rozvrh_layout);
            TableLayout layout_days = (TableLayout) this.getView().findViewById(R.id.rozvrh_layout_days);

            LayoutInflater inf = (LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            layout.removeAllViews();
            layout_days.removeAllViews();

            JSONArray rozvrh = object.getJSONObject("data").getJSONArray("rozvrh");
            JSONArray casy = object.getJSONObject("data").getJSONArray("casy");

            TableRow times = new TableRow(this.getActivity());

            //generate days
            for(int i = 0; i < casy.length(); i++) {
                JSONObject time = casy.getJSONObject(i);

                View cell = inf.inflate(R.layout.rozvrh_times_cell, null);

                TextView name = (TextView) cell.findViewById(R.id.rozvrh_times_label);
                name.setText(time.getString("label"));

                TextView occurence = (TextView) cell.findViewById(R.id.rozvrh_times_time);
                JSONArray time_array = time.getJSONArray("time");
                occurence.setText(time_array.getString(0) + " - "+ time_array.getString(1));

                times.addView(cell);
            }

            layout.addView(times);

            for(int i = 0; i < rozvrh.length(); i++) {
                JSONObject day = rozvrh.getJSONObject(i);

                TableLayout.LayoutParams row_params = new TableLayout.LayoutParams(
                        TableLayout.LayoutParams.MATCH_PARENT,
                        TableLayout.LayoutParams.MATCH_PARENT, 1.0f);

                TableRow row_day = new TableRow((this.getActivity()));
                row_day.setLayoutParams(row_params);

                TextView row_day_text = new TextView(this.getActivity());
                row_day_text.setText(day.getJSONObject("day").getString("label"));

                row_day.addView(row_day_text);

                layout_days.addView(row_day);


                TableRow row = new TableRow(this.getActivity());
                row.setLayoutParams(row_params);

                JSONArray lessons = day.getJSONArray("lessons");

                for(int y = 0; y < lessons.length(); y++) {
                    JSONObject lesson = lessons.getJSONObject(y);
                    JSONArray content = lesson.getJSONArray("content");

                    //Zmršený den jen kvůli tomuhle...
                    LinearLayout view = (LinearLayout) inf.inflate(R.layout.rozvrh_cell, null);

                    TableRow.LayoutParams params = new TableRow.LayoutParams();



                    if(lesson.get("lesson") instanceof Integer) {
                        params.column = lesson.getInt("lesson");
                    } else {
                        params.column = lesson.getJSONObject("lesson").getInt("begin");
                        params.span = lesson.getJSONObject("lesson").getInt("length");
                    }

                    view.setLayoutParams(params);

                    for(int z = 0; z < content.length(); z++) {
                        JSONObject subject = content.getJSONObject(z);

                        TextView tv = new TextView(this.getActivity());
                        tv.setText(subject.getJSONObject("name").getString("short"));



                        view.addView(tv);
                    }



                    row.addView(view);
                }

                layout.addView(row);
            }
        }
    }


    @Override
    public void onResume(){
        super.onResume();

        Bundle request = new Bundle();
        request.putString("page", "rozvrh");
        request.putString("file", "rozvrh-novy-zmena.html");

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
}
