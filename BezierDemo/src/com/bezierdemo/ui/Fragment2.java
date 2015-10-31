package com.bezierdemo.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class Fragment2 extends Fragment implements OnClickListener {
    TextView mRedPoint;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_2, container, false);

        mRedPoint = (TextView) rootView.findViewById(R.id.red_point);

        // Bundle args = getArguments();
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.red_point:
            break;

        default:
            break;
        }
    }
}
