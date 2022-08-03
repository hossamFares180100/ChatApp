package com.example.chatapp.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.chatapp.MainActivity;
import com.example.chatapp.R;

public class PeopleFragment extends Fragment {


    public PeopleFragment() {
        // Required empty public constructor
    }

    public static PeopleFragment newInstance(String param1, String param2) {
        PeopleFragment fragment = new PeopleFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView titleToolbar=getActivity().findViewById(R.id.title_toolbar_textview);
        titleToolbar.setText("People");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_people, container, false);
    }
}