package com.isport.fitness.constants;

import com.isport.tracker.R;
import com.isport.tracker.fragment.PedoHistoryFragment;
import com.isport.tracker.util.UIUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wj on 2017/8/11.
 */

public class Constants {

    public final static List<Integer> dateTypeList;
    public final static List<String> dateTypeStringList;

    static {
        dateTypeList = new ArrayList<>();
        dateTypeList.add(PedoHistoryFragment.DATE_DAY);
        dateTypeList.add(PedoHistoryFragment.DATE_WEEK);
        dateTypeList.add(PedoHistoryFragment.DATE_ONE_WEEK);
        dateTypeList.add(PedoHistoryFragment.DATE_MONTH);
        dateTypeStringList = new ArrayList<>();
        dateTypeStringList.add(UIUtils.getString(R.string.day));
        dateTypeStringList.add(UIUtils.getString(R.string.week));
        dateTypeStringList.add(UIUtils.getString(R.string.a_week));
        dateTypeStringList.add(UIUtils.getString(R.string.month));
    }
}
