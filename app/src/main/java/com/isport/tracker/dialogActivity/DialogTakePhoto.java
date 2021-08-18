package com.isport.tracker.dialogActivity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.isport.tracker.BuildConfig;
import com.isport.tracker.R;
import com.isport.tracker.main.BaseActivity;
import com.isport.tracker.main.CropActivity;
import com.isport.tracker.util.Constants;
import com.isport.tracker.util.UtilTools;

import java.io.File;
import java.io.IOException;

public class DialogTakePhoto extends BaseActivity {
    private TextView take_photo, browsing;
    private Button cancle;
    private static final int IMAGE_REQUEST_CODE = 1;// 请求码
    private static final int CAMERA_REQUEST_CODE = 2;
    private static final int RESULT_REQUEST_CODE = 3;


    // 是否是Android 10以上手机

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyPermission();
        setContentView(R.layout.activity_settings_profile_photo);
        setTitle("");
        init();
    }

    private void init() {

        take_photo = (TextView) findViewById(R.id.user_info_take_photo);
        browsing = (TextView) findViewById(R.id.user_info_browsing);
        cancle = (Button) findViewById(R.id.user_info_take_photo_cancle);
        take_photo.setOnClickListener(new OnClickListenerImpl());
        browsing.setOnClickListener(new OnClickListenerImpl());
        cancle.setOnClickListener(new OnClickListenerImpl());
    }

    public void verifyPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
            return;
        }
    }

    private class OnClickListenerImpl implements OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.user_info_take_photo:
                    PackageManager packageManager = getPackageManager();
                    if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {///判断是否支持相机
                        Toast.makeText(DialogTakePhoto.this, getString(R.string.not_support_camera), Toast.LENGTH_LONG).show();
                        return;
                    }
                    // 判断存储卡是否可以用，可用进行存储
                    if (UtilTools.hasSdcard()) {
                        int SDK_INT = Build.VERSION.SDK_INT;

                        Intent intentFromCapture = new Intent(
                                MediaStore.ACTION_IMAGE_CAPTURE);
                        File file = new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera", Constants.SAVEUSERIMAGE);

                        // 下面这句指定调用相机拍照后的照片存储的路径

                        ContentValues contentValues = new ContentValues(1);
                        contentValues.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                        Uri uri = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            uri = FileProvider.getUriForFile(DialogTakePhoto.this, BuildConfig.APPLICATION_ID + ".fileprovider", file);
                        } else {
                            uri = Uri.fromFile(file);
                        }
                        intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, uri);


                        startActivityForResult(intentFromCapture, CAMERA_REQUEST_CODE);
                        break;
                    }
                    break;
                case R.id.user_info_browsing:
                    try {
                        Intent intentFromGallery = new Intent(Intent.ACTION_PICK, null);
                        intentFromGallery.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                        startActivityForResult(intentFromGallery, 1);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(DialogTakePhoto.this, getString(R.string.not_select_album), Toast.LENGTH_LONG).show();
                    }

                    break;
                case R.id.user_info_take_photo_cancle:
                    DialogTakePhoto.this.finish();
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (IMAGE_REQUEST_CODE == requestCode) {
            if (data != null) {
                File temp = new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera" + Constants.SAVEUSERIMAGE);
                Uri uri = data.getData();
                if (Build.VERSION.SDK_INT >= 24) {
                    String path = getImageAbsolutePath(this, uri);
                    if (path != null) {
                        File file = new File(path);
                        //uri = FileProvider.getUriForFile(DialogTakePhoto.this, "com.isport.tracker.fileProvider", file);
                        uri = getImageContentUri(this, file);
                        startPhotoZoom(path);
                    }
                } else {
                    String path = getImageAbsolutePath(this, uri);
                    startPhotoZoom(path);
                }
                //startPhotoZoom(DialogTakePhoto.this,uri,temp,1);
                System.out.println(data.getData() + "data");
            }
        } else if (CAMERA_REQUEST_CODE == requestCode) {
            if (resultCode == 0) {
            } else {
                if (UtilTools.hasSdcard()) {
                    File temp = new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera" + Constants.SAVEUSERIMAGE);
                    Uri uri = null;
                    if (Build.VERSION.SDK_INT >= 24) {
                        uri = FileProvider.getUriForFile(DialogTakePhoto.this, BuildConfig.APPLICATION_ID + ".fileprovider", temp);

                        //uri = getImageContentUri(this,temp);
                    } else {
                        uri = Uri.fromFile(temp);
                    }

                    //startPhotoZoom(DialogTakePhoto.this,uri,temp,0);
                    startPhotoZoom(temp.getAbsolutePath());
                } else {
                    Toast.makeText(this, DialogTakePhoto.this.getResources().getString(R.string.user_info_no_sd_card), Toast.LENGTH_SHORT).show();
                }
            }
        } else if (RESULT_REQUEST_CODE == requestCode) {
            //if (data != null) {
            getImageToView(data);
            //}
        }
    }

    public void startPhotoZoom(String path) {
        if (path != null) {
            Intent intent = new Intent(this, CropActivity.class);
            intent.putExtra("data", path);
            startActivityForResult(intent, RESULT_REQUEST_CODE);
        }
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }


    /**
     * @param context
     * @param uri
     * @param file
     * @param type    0 拍照  1 相册
     */
    public void startPhotoZoom(Context context, Uri uri, File file, int type) {


        Intent intent = new Intent("com.android.camera.action.CROP");

        if (Build.VERSION.SDK_INT >= 24) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {

        }
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        if (Build.VERSION.SDK_INT >= 24) {
            if (type == 0) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(DialogTakePhoto.this, BuildConfig.APPLICATION_ID + ".fileprovider", file));
            } else {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            }
        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        }
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 206);
        intent.putExtra("outputY", 206);
        intent.putExtra("return-data", false);
        startActivityForResult(intent, RESULT_REQUEST_CODE);
    }

    /**
     * 保存裁剪之后的图片数据
     */
    private void getImageToView(Intent data) {
        if (data == null)
            return;
        Bundle extras = data.getExtras();
        Uri uri = data.getData();
        data.putExtra("head_path", data.getStringExtra("data"));
        setResult(200, data);
        DialogTakePhoto.this.finish();
		/*Bundle extras = data.getExtras();
		Intent photoIntent = null;
		Uri uri = data.getData();
		if(Build.VERSION.SDK_INT>=24){
			if (extras != null) {
				Bitmap bitmap = extras.getParcelable("data");
				String path = extras.getString("src_url");
				BitmapUtil.save(bitmap, Environment.getExternalStorageDirectory()+"/DCIM/Camera" + Constants.SAVEUSERIMAGE);
				photoIntent = new Intent();
				photoIntent.putExtra("head_path", Environment.getExternalStorageDirectory()+"/DCIM/Camera" + Constants.SAVEUSERIMAGE);
				setResult(200, photoIntent);
				DialogTakePhoto.this.finish();
			} else if(uri != null){
				String[] strs = uri.toString().split("/\\./");
				String path = null;
				if(strs.length == 1){
					path = getImageAbsolutePath(this,uri);
				}else {
					path = strs[1];
					path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+path;
				}
				photoIntent = new Intent();
				photoIntent.putExtra("head_path", path);
				setResult(200, photoIntent);
				DialogTakePhoto.this.finish();
			}else {
				String action = data.getAction();
				if(action != null) {
					String[] strs = action.toString().split("/\\./");
					String path = null;
					if (strs.length == 1) {
						path = getImageAbsolutePath(this, Uri.parse(action));
					} else {
						path = strs[1];
						path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + path;
					}
					photoIntent = new Intent();
					photoIntent.putExtra("head_path", path);
					setResult(200, photoIntent);
				}
				DialogTakePhoto.this.finish();
			}
		}else {
			if (extras != null) {
				Bitmap bitmap = extras.getParcelable("data");
				String path = extras.getString("src_url");
				BitmapUtil.save(bitmap, Environment.getExternalStorageDirectory()+"/DCIM/Camera" + Constants.SAVEUSERIMAGE);
				photoIntent = new Intent();
				photoIntent.putExtra("head_path", Environment.getExternalStorageDirectory()+"/DCIM/Camera" + Constants.SAVEUSERIMAGE);
				setResult(200, photoIntent);
				DialogTakePhoto.this.finish();
			}else if(uri != null){
				String path = getImageAbsolutePath(this,uri);
				photoIntent = new Intent();
				photoIntent.putExtra("head_path", path);
				setResult(200, photoIntent);
				DialogTakePhoto.this.finish();
			}
		}*/
    }

    /**
     * 保存裁剪之后的图片数据
     */
    private void getImageToView(String path) {
        Intent intent = new Intent();
        intent.putExtra("head_path", path);
        setResult(200, intent);
        DialogTakePhoto.this.finish();
		/*Bundle extras = data.getExtras();
		Intent photoIntent = null;
		Uri uri = data.getData();
		if(Build.VERSION.SDK_INT>=24){
			if (extras != null) {
				Bitmap bitmap = extras.getParcelable("data");
				String path = extras.getString("src_url");
				BitmapUtil.save(bitmap, Environment.getExternalStorageDirectory()+"/DCIM/Camera" + Constants.SAVEUSERIMAGE);
				photoIntent = new Intent();
				photoIntent.putExtra("head_path", Environment.getExternalStorageDirectory()+"/DCIM/Camera" + Constants.SAVEUSERIMAGE);
				setResult(200, photoIntent);
				DialogTakePhoto.this.finish();
			} else if(uri != null){
				String[] strs = uri.toString().split("/\\./");
				String path = null;
				if(strs.length == 1){
					path = getImageAbsolutePath(this,uri);
				}else {
					path = strs[1];
					path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+path;
				}
				photoIntent = new Intent();
				photoIntent.putExtra("head_path", path);
				setResult(200, photoIntent);
				DialogTakePhoto.this.finish();
			}else {
				String action = data.getAction();
				if(action != null) {
					String[] strs = action.toString().split("/\\./");
					String path = null;
					if (strs.length == 1) {
						path = getImageAbsolutePath(this, Uri.parse(action));
					} else {
						path = strs[1];
						path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + path;
					}
					photoIntent = new Intent();
					photoIntent.putExtra("head_path", path);
					setResult(200, photoIntent);
				}
				DialogTakePhoto.this.finish();
			}
		}else {
			if (extras != null) {
				Bitmap bitmap = extras.getParcelable("data");
				String path = extras.getString("src_url");
				BitmapUtil.save(bitmap, Environment.getExternalStorageDirectory()+"/DCIM/Camera" + Constants.SAVEUSERIMAGE);
				photoIntent = new Intent();
				photoIntent.putExtra("head_path", Environment.getExternalStorageDirectory()+"/DCIM/Camera" + Constants.SAVEUSERIMAGE);
				setResult(200, photoIntent);
				DialogTakePhoto.this.finish();
			}else if(uri != null){
				String path = getImageAbsolutePath(this,uri);
				photoIntent = new Intent();
				photoIntent.putExtra("head_path", path);
				setResult(200, photoIntent);
				DialogTakePhoto.this.finish();
			}
		}*/
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
        matrix.postRotate(angle);
        System.out.println("angle2=" + angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    /**
     * 根据Uri获取图片绝对路径，解决Android4.4以上版本Uri转换
     *
     * @param context
     * @param imageUri
     * @author yaoxing
     * @date 2014-10-12
     */
    @TargetApi(19)
    public static String getImageAbsolutePath(Activity context, Uri imageUri) {
        if (context == null || imageUri == null)
            return null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, imageUri)) {
            if (isExternalStorageDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(imageUri)) {
                String id = DocumentsContract.getDocumentId(imageUri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } // MediaStore (and general)
        else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(imageUri))
                return imageUri.getLastPathSegment();
            return getDataColumn(context, imageUri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
            return imageUri.getPath();
        }
        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

}