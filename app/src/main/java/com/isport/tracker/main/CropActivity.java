package com.isport.tracker.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.isport.tracker.R;
import com.isport.tracker.util.Constants;
import com.isport.tracker.util.UtilTools;
import com.isport.tracker.view.crop.ClipImageLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Administrator on 2016/7/7.
 * Crop Image
 */
public class CropActivity extends BaseActivity implements View.OnClickListener {

    ClipImageLayout mLayout;
    TextView mTvCrop;
    View mViewBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        mLayout = (ClipImageLayout) findViewById(R.id.crop_img_layout);
        mTvCrop = (TextView) findViewById(R.id.img_crop);
        mViewBack = findViewById(R.id.back_tv);
        mViewBack.setOnClickListener(this);
        mTvCrop.setOnClickListener(this);

        mLayout.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = getIntent();
                String path = intent.getStringExtra("data");
                if (path != null) {
                    Drawable drawable = new BitmapDrawable(resetBitmap(path, mLayout.getWidth(), mLayout.getHeight()));
                    if (drawable != null) {
                        mLayout.setImageDrawable(drawable);
                    }
                }
            }
        });

    }

    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /*
     * 旋转图片
     * @param angle
     * @param bitmap
     * @return Bitmap
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        ;
        matrix.postRotate(angle);
        System.out.println("angle2=" + angle);
        // 创建新的图片
        if (bitmap != null) {
            Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return resizedBitmap;
        } else {
            return null;
        }
    }

    public Bitmap resetBitmap(String path, int w, int h) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        int bw = options.outWidth;
        int bh = options.outHeight;
        int samplesize = 1;
        if (bw > w && bh > h) {
            samplesize = bw / w > bh / h ? bw / w : bh / h;
        } else if (bw > w) {
            samplesize = bw / w;
        } else if (bh > h) {
            samplesize = bh / h;
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = samplesize;
        Bitmap bmp = BitmapFactory.decodeFile(path, options);
        bmp = rotaingImageView(readPictureDegree(path), bmp);
        return bmp;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.back_tv:
                finish();
                break;
            case R.id.img_crop:
                if (UtilTools.hasSdcard()) {
                    File file = new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera", Constants.SAVEUSERIMAGE);
                    if (file.exists()) {
                        file.delete();
                    }
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    ByteArrayOutputStream outputStream = null;
                    OutputStream os = null;
                    try {
                        file.createNewFile();
                        Bitmap bitmap = mLayout.clip();

                        outputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        byte[] datas = outputStream.toByteArray();
                        os = new FileOutputStream(file);
                        os.write(datas);
                        os.flush();
                        os.close();
                        bitmap.recycle();
                        bitmap = null;
                        Intent intent = new Intent();
                        intent.putExtra("data", file.getAbsolutePath());
                        setResult(RESULT_OK, intent);
                        finish();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (os != null) {
                                os.close();
                            }
                            if (outputStream != null) {
                                outputStream.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                } else {
                    Toast.makeText(this, getString(R.string.user_info_no_sd_card), Toast.LENGTH_LONG).show();
                    finish();
                }

                break;
        }
    }
}
