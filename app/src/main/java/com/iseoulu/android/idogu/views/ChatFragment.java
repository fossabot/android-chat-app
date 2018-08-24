package com.iseoulu.android.idogu.views;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iseoulu.android.idogu.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View chattingView = inflater.inflate(R.layout.fragment_chat,container, false);
        return chattingView;
    }

}
