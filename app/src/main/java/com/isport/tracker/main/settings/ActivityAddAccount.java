package com.isport.tracker.main.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.isport.tracker.R;
import com.isport.tracker.main.BaseActivity;

/**
 * Created by Administrator on 2017/7/26.
 */

public class ActivityAddAccount extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_addaccount);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()){
            case R.id.google_fit_rela:
//                intent = new Intent(this, ActivityGoogleFit.class);
//                startActivity(intent);
                break;
            case R.id.back_tv:
                finish();
                break;
        }
    }
}
