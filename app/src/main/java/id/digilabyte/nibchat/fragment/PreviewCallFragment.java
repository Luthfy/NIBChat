package id.digilabyte.nibchat.fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import id.digilabyte.nibchat.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreviewCallFragment extends Fragment {


    public PreviewCallFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_preview_call, container, false);
    }

}
