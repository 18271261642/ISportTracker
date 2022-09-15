package com.energetics.tracker.main.settings;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.isport.tracker.R;
import com.isport.tracker.main.BaseActivity;
import com.joanzapata.pdfview.PDFView;

//import com.joanzapata.pdfview.PDFView;
//import com.joanzapata.pdfview.listener.OnPageChangeListener;

public class AboutUsActivity extends BaseActivity implements com.joanzapata.pdfview.listener.OnPageChangeListener {
	private TextView re_back, text_version;
	public static final String ABOUT_FILE = "about.pdf";
	Integer pageNumber = 1;
	private com.joanzapata.pdfview.PDFView pdfView;

	@TargetApi(19)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings_about_us);
		init();

	}

	private void init() {
		re_back = (TextView) findViewById(R.id.return_back);
		re_back.setOnClickListener(new OnClickListenerImpl());

		pdfView = (PDFView) findViewById(R.id.pdfView);
		pdfView.fromAsset(ABOUT_FILE)
				.onPageChange(this)
				.load();

	}

	/*private PdfScale getPdfScale() {
		PdfScale scale = new PdfScale();
		scale.setScale(1.0f);
		scale.setCenterX(getScreenWidth(this) / 2);
		scale.setCenterY(0f);
		return scale;
	}*/

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public int getScreenWidth(Context ctx) {
		int w = 0;
		if (ctx instanceof Activity) {
			DisplayMetrics displaymetrics = new DisplayMetrics();
			((Activity) ctx).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
			w = displaymetrics.widthPixels;
		}
		return w;
	}

	@Override
	public void onPageChanged(int i, int i1) {

	}

	private class OnClickListenerImpl implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.return_back:
					AboutUsActivity.this.finish();
					break;
				default:
					break;
			}
		}
	}



}