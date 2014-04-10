package cz.duong.skolar.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;

public class SuplovaniFragment extends ListFragment {


    // TODO: Rename and change types of parameters
    public static SuplovaniFragment newInstance(String param1, String param2) {
        SuplovaniFragment fragment = new SuplovaniFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SuplovaniFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }



}
