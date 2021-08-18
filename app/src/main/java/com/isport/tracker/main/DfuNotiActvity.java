package com.isport.tracker.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Administrator on 2016/11/1.
 */

public class DfuNotiActvity extends Activity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // If this activity is the root activity of the task, the app is not running
        if (isTaskRoot()) {
            // Start the app before finishing
            final Intent parentIntent = new Intent(this, MainActivityGroup.class);
            parentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            parentIntent.putExtras(getIntent().getExtras());
            startActivity(parentIntent);

			/*
			 * If we'd like to recreate a whole history here, we can do it like this:
			 *
			 * final Intent parentIntent = new Intent(this, ParentActivity.class);
			 * parentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			 * final Intent nextIntent = new Intent(this, NextActivity.class);
			 * final Intent startAppIntent = new Intent(this, TargetActivity.class);
			 * startActivities(new Intent[] { parentIntent, next, startAppIntent });
			 */
        }

        // Now finish, which will drop the user in to the activity that was at the top of the task stack
        finish();
    }
}
