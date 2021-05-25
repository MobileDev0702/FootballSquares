package com.moyersoftware.contender.game;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.moyersoftware.contender.R;

public class HowToPlayFragment extends Fragment {
    // Store instance variables
    private int page;

    // newInstance constructor for creating fragment with arguments
    public static HowToPlayFragment newInstance(int page) {
        HowToPlayFragment fragmentFirst = new HowToPlayFragment();
        Bundle args = new Bundle();
        args.putInt("page", page);
        fragmentFirst.setArguments(args);

        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        page = getArguments().getInt("page", 0);
    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_how_to_play, container, false);
        ImageView image = view.findViewById(R.id.image);
        image.setImageResource(HowToPlayActivity.mImages[page]);
        return view;
    }
}