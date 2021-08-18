package com.isport.tracker.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;

public class BaseFragment extends Fragment {
    

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

   public void clearAdapter(){

   }

}
