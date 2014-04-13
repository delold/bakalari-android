package cz.duong.skolar.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.devspark.progressfragment.ProgressFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.duong.skolar.R;
import cz.duong.skolar.server.Users;
import cz.duong.skolar.utils.UrlRequest;

/**
 * Created by David on 9. 4. 2014.
 */
public class RozvrhFragment extends ProgressFragment implements UrlRequest.RequestComplete {
    private View contentView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setContentView(contentView);
        setEmptyText(R.string.data_empty);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        contentView = inflater.inflate(R.layout.fragment_rozvrh, container, false);

        return super.onCreateView(inflater, container, savedInstanceState);
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


            layout_days.addView(
                    inf.inflate(R.layout.rozvrh_times_cell, new TableRow(this.getActivity()))
            );

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

                //HACK, ale co naděláš...
                int last_lesson = 0;

                for(int y = 0; y < lessons.length(); y++) {
                    JSONObject lesson = lessons.getJSONObject(y);
                    JSONArray content = lesson.getJSONArray("content");


                    int begin = (lesson.get("lesson") instanceof Integer) ? lesson.getInt("lesson") :
                            lesson.getJSONObject("lesson").getInt("begin");


                    //obvykle bych zde použil pouze LayoutParams.column, ono to však hází NPE, tak proto takový hnusný hack
                    if(last_lesson < begin) {
                        LinearLayout span = (LinearLayout) inf.inflate(R.layout.rozvrh_cell, row, false);
                        TableRow.LayoutParams span_params = (TableRow.LayoutParams) span.getLayoutParams();

                        span_params.span = begin - last_lesson;

                        span.setLayoutParams(span_params);

                        row.addView(span);
                    }

                    last_lesson = begin+1;

                    LinearLayout view = (LinearLayout) inf.inflate(R.layout.rozvrh_cell, row, false);
                    TableRow.LayoutParams params = (TableRow.LayoutParams) view.getLayoutParams();

                    if(!(lesson.get("lesson") instanceof Integer)) {
                        params.span = lesson.getJSONObject("lesson").getInt("length");
                    }

                    //shit

                    if(lesson.getString("type").equals("changed")) {
                        view.setBackgroundResource(R.color.mark_5);
                    } else if (lesson.getString("type").equals("free")) {
                        view.setBackgroundResource(R.color.mark_1);
                    }



                    view.setLayoutParams(params);

                    for(int z = 0; z < content.length(); z++) {
                        JSONObject subject = content.getJSONObject(z);

                        View lesson_view = inf.inflate(R.layout.rozvrh_lesson, view, false);

                        TextView lesson_txt = (TextView) lesson_view.findViewById(R.id.rozvrh_lesson_name);
                        TextView teacher_txt = (TextView) lesson_view.findViewById(R.id.rozvrh_lesson_teacher);
                        TextView room_txt = (TextView) lesson_view.findViewById(R.id.rozvrh_lesson_room);
                        TextView group_txt = (TextView) lesson_view.findViewById(R.id.rozvrh_lesson_group);

                        lesson_txt.setText(subject.getJSONObject("name").getString("short"));
                        teacher_txt.setText(subject.getJSONObject("teacher").getString("short"));
                        room_txt.setText(subject.getJSONObject("place").getString("short"));
                        group_txt.setText(subject.getJSONObject("group").getString("short"));





                        view.addView(lesson_view);
                    }

                    row.addView(view);
                }

                layout.addView(row);
            }

            this.setContentShown(true);
        }
    }


    @Override
    public void onResume(){
        super.onResume();

        Bundle request = new Bundle();
        request.putString("page", "rozvrh");
        //request.putString("file", "rozvrh-novy-zmena.html");

        request.putParcelable("user", new Users(this.getActivity()).getCurrentUser());

        this.setContentShown(false);
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
