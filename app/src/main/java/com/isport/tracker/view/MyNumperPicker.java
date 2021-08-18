package com.isport.tracker.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.isport.tracker.R;

import java.lang.reflect.Field;

/**
 * Created by Administrator on 2016/10/17.
 */

public class MyNumperPicker extends NumberPicker {
    public MyNumperPicker(Context context) {
        super(context);
    }

    public MyNumperPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyNumperPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        updateView(child);
    }

    @Override
    public void addView(View child, int index,
                        android.view.ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        updateView(child);
    }

    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, params);
        updateView(child);
    }

    public void updateView(View view) {
        if (view instanceof EditText) {
            //这里修改字体的属性
            EditText editText = ((EditText) view);
            editText.setTextColor(Color.BLACK);
            editText.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
//            ((EditText) view).setTextSize();
        }else if(view instanceof Button){
            Button btn = ((Button)view);
            btn.setTextColor(getResources().getColor(R.color.light_white));
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);
        }
        setNumberPickerDividerColor();
    }

    private void setNumberPickerDividerColor() {
        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    //设置分割线的颜色值
                    pf.set(this, new ColorDrawable(this.getResources().getColor(R.color.light_white)));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
}
