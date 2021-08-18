package com.isport.tracker.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.isport.tracker.R;
import com.ypy.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by 中庸 on 2016/5/20.
 */
public class CamaraActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG = CamaraActivity.class.getSimpleName();
    TextView returnBack;
    TextView tvswitchCamera;
    //启动摄像机
    private Camera mCamera;

    public boolean isopen_camara = false;
    private SurfaceView surfaceView;
    private SurfaceHolder mholder = null;
    private SurfaceCallback previewCallBack;
    private boolean isTakingPhoto;//是否正在拍照

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyPermission(new String[]{Manifest.permission.CAMERA});
        //  getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camara);
        ButterKnife.bind(this);
        Log.e("mainService", "6666" );
        // 预览控件
        surfaceView = (SurfaceView) this
                .findViewById(R.id.surfaceView);
        returnBack = this
                .findViewById(R.id.return_back);
        tvswitchCamera = this
                .findViewById(R.id.tvswitch_camera);
        // 设置参数
        surfaceView.getHolder().setKeepScreenOn(true);
        surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.setOnClickListener(this);
        returnBack.setOnClickListener(this);
        tvswitchCamera.setOnClickListener(this);
        EventBus.getDefault().register(this);
    }

    public void verifyPermission(String[] permissions) {
        if (permissions != null) {
            List<String> lists = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (ActivityCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    if(ActivityCompat.shouldShowRequestPermissionRationale(this,permissions[i])){

                    }
                    lists.add(permissions[i]);
                }
            }
            if (lists.size() > 0) {
                String[] ps = new String[lists.size()];
                for (int i = 0; i < lists.size(); i++) {
                    ps[i] = lists.get(i);
                }
                ActivityCompat.requestPermissions(this, ps, 1);
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void takePhoto() {
        if (!isopen_camara) {
            previewCallBack = new SurfaceCallback();
            surfaceView.getHolder().addCallback(previewCallBack);
        } else {
            autoTakePhoto();
        }
    }

    /**
     * when takePhoto() callback
     * @param msg
     */
    public void onEventMainThread(Message msg){
        switch(msg.what) {
            case 0x12:
                takePhoto();
                break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        previewCallBack = new SurfaceCallback();
        surfaceView.getHolder().addCallback(previewCallBack);
        if(mCamera == null){
            if(isopen_camara) {

            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            if(surfaceView != null && surfaceView.getHolder() != null && previewCallBack != null){
                surfaceView.getHolder().removeCallback(previewCallBack);
            }
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.lock();
            mCamera.release();
            mCamera = null;
        }
        isopen_camara = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.return_back:
                finish();
                break;
            case R.id.tvswitch_camera:
                switchCamara();
                break;
            case R.id.surfaceView:
                autoFocus();
                break;
        }
    }

    boolean isSurfaceCreate;
    // 预览界面回调
    private final class SurfaceCallback implements SurfaceHolder.Callback {
        // 预览界面被创建
        public void surfaceCreated(SurfaceHolder holder) {
            isSurfaceCreate = true;
            try {
                //1代表打开后置摄像头,0代表打开前置摄像头.
                mCamera = Camera.open(cameraPosition);// 打开摄像头
                setCameraDisplayOrientation(cameraPosition,mCamera);
                setParams(holder,cameraPosition);
            } catch (Exception e) {
                e.printStackTrace();
                if (mCamera != null) {
                    holder.removeCallback(this);
                    mCamera.setPreviewCallback(null);
                    mCamera.stopPreview();
                    mCamera.lock();
                    mCamera.release();
                    mCamera = null;
                }
                finish();
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            System.out.println("surfaceChanged");
            isopen_camara = true;
            //autoTakePhoto();
        }

        // 预览界面被销毁
        public void surfaceDestroyed(SurfaceHolder holder) {
            System.out.println("surfaceDestroyed");
            isSurfaceCreate = false;
            if(!isopen_camara)
                return;
            if (mCamera != null) {
                holder.removeCallback(this);
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();
                mCamera.lock();
                mCamera.release();
                mCamera = null;
            }
        }

    }

    public void reset(){
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数

        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
            if (cameraInfo.facing == cameraPosition) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                if (surfaceView != null && surfaceView.getHolder() != null && previewCallBack != null) {
                    surfaceView.getHolder().removeCallback(previewCallBack);
                }
                if(mCamera != null) {
                    mCamera.setPreviewCallback(null);
                    mCamera.stopPreview();//停掉原来摄像头的预览
                    mCamera.lock();
                    mCamera.release();//释放资源
                    mCamera = null;//取消原来摄像头
                }
                mCamera = Camera.open(i);//打开当前选中的摄像头
                setCameraDisplayOrientation(i,mCamera);
                if (null != mholder)
                    setParams(mholder, cameraPosition);

                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        EventBus.getDefault().unregister(this);
    }

    private void setParams(SurfaceHolder mySurfaceView,int postion) {
        try {
            int PreviewWidth = 0;
            int PreviewHeight = 0;
            int PictureWidth = 0;
            int PictureHeight = 0;
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);//获取窗口的管理器
            Camera.Parameters parameters = mCamera.getParameters();
            // 选择合适的预览尺寸
            List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
            List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
            if (sizeList.size() > 0) {
                for (int i = 0; i < sizeList.size(); i++) {
                    Log.e(TAG, "   PreviewHeight  " + sizeList.get(i).height + "  PreviewWidth  " + sizeList.get(i)
                            .width);
                    if (i == 0) {
                        PreviewWidth = sizeList.get(i).width;
                        PreviewHeight = sizeList.get(i).height;
                    }
                }
            }
            if (pictureSizes.size() > 0) {
                for (int i = 0; i < pictureSizes.size(); i++) {
                    Log.e(TAG, "   PictureHeight  " + pictureSizes.get(i).height + "  PictureWidth  " + pictureSizes
                            .get(i)
                            .width);
                    if (i == 0) {
                        PictureWidth = pictureSizes.get(i).width;
                        PictureHeight = pictureSizes.get(i).height;
                    }
                }
            }
            parameters.setPreviewSize(PreviewWidth, PreviewHeight); //获得摄像区域的大小
            //parameters.setPreviewFrameRate(3);//每秒3帧  每秒从摄像头里面获得3个画面
            //parameters.setPreviewFpsRange(3,);
            List<int[]> list = parameters.getSupportedPreviewFpsRange();
            int[] v = null;
            int index = 0;
            int min = 0;
            for (int i = 0; i < list.size(); i++) {
                v = list.get(i);
                if (v[0] > min) {
                    min = v[0];
                    index = i;
                }
            }
            List<String> allFocus = parameters.getSupportedFocusModes();

            if (allFocus.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            } else if (allFocus.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//
                // FOCUS_MODE_CONTINUOUS_PICTURE FOCUS_MODE_AUTO
            }
            parameters.setPreviewFpsRange(list.get(index)[0], list.get(index)[1]);
            parameters.setPictureFormat(PixelFormat.JPEG);//设置照片输出的格式
            parameters.set("jpeg-quality", 100);//设置照片质量
            parameters.setPictureSize(PictureWidth, PictureHeight); //获得摄像区域的大小
            parameters.setRotation(180); //Java部分
            mCamera.setParameters(parameters);//把上面的设置 赋给摄像头
            mCamera.setPreviewDisplay(mySurfaceView);//把摄像头获得画面显示在SurfaceView控件里面
            mholder = mySurfaceView;
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {

                }
            });
            mCamera.startPreview();//开始预览
            mCamera.cancelAutoFocus();
            autoFocus();

            //   mPreviewRunning = true;
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    Handler handler = null;

    private void autoTakePhoto() {
        // 拍照前需要对焦 获取清析的图片
        if (null == mCamera) return;
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {

                if (success && isopen_camara) {
                    // 对焦成功
                    //    Toast.makeText(MainActivity.this, "对焦成功 !!",Toast.LENGTH_SHORT).show();
                    if(!isTakingPhoto) {
                        isTakingPhoto = true;
                        handler = new Handler();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mCamera.takePicture(null, null, new MyPictureCallback());
                            }
                        });
                    }
                }
            }
        });
    }

    private int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
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

    // 照片回调
    private final class MyPictureCallback implements Camera.PictureCallback {
        // 照片生成后
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.e(TAG,"-----拍照完毕="+data.length);
            if(data.length == 0)
                return;
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                data = null;
                Matrix matrix = new Matrix();
                if(cameraPosition == 1) {
                    matrix.setRotate(rotateDegress);
                }else {
                    matrix.setRotate(rotateDegress+90);
                }
                File jpgFile = new File(Environment.getExternalStorageDirectory() + "/DCIM/camera/");
                if (!jpgFile.exists()) {
                    jpgFile.mkdir();
                }
                File jpgFile1 = new File(jpgFile.getAbsoluteFile(), System.currentTimeMillis() + ".jpg");
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                FileOutputStream fos = new FileOutputStream(jpgFile1);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                Toast.makeText(CamaraActivity.this,getString(R.string.save_success),Toast.LENGTH_SHORT).show();
                fos.close();
                bitmap.recycle();
                bitmap = null;
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(jpgFile1);
                intent.setData(uri);
                sendBroadcast(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                if(Build.VERSION.SDK_INT>=24){
                    reset();
                }
                isTakingPhoto = false;
            }
        }
    }

    private int cameraPosition = 0;//0代表前置摄像头，1代表后置摄像头

    private void switchCamara() {
        if(mCamera == null)
            return;
        //切换前后摄像头
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数

        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
            if (cameraPosition == 1) {
                //现在是后置，变更为前置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    if(surfaceView != null && surfaceView.getHolder() != null && previewCallBack != null){
                        surfaceView.getHolder().removeCallback(previewCallBack);
                    }
                    mCamera.setPreviewCallback(null);
                    mCamera.stopPreview();//停掉原来摄像头的预览
                    mCamera.lock();
                    mCamera.release();//释放资源
                    mCamera = null;//取消原来摄像头
                    mCamera = Camera.open(i);//打开当前选中的摄像头
                    mCamera.startPreview();//开始预览*/
                    setCameraDisplayOrientation(i,mCamera);
                    if (null != mholder)
                        setParams(mholder,Camera.CameraInfo.CAMERA_FACING_BACK);
                    cameraPosition = 0;
                    break;
                }
            } else {
                //现在是前置， 变更为后置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                    if(surfaceView != null && surfaceView.getHolder() != null && previewCallBack != null){
                        surfaceView.getHolder().removeCallback(previewCallBack);
                    }
                    mCamera.setPreviewCallback(null);
                    mCamera.stopPreview();//停掉原来摄像头的预览
                    mCamera.lock();
                    mCamera.release();//释放资源
                    mCamera = null;//取消原来摄像头
                    mCamera = Camera.open(i);//打开当前选中的摄像头
                    mCamera.startPreview();//开始预览*/
                    setCameraDisplayOrientation(i,mCamera);
                    if (null != mholder)
                        setParams(mholder,Camera.CameraInfo.CAMERA_FACING_FRONT);
                    cameraPosition = 1;
                    break;
                }
            }
            autoFocus();
        }
    }

    private void autoFocus(){
        if(!autoFocusHandler.hasMessages(0x01)){
            autoFocusHandler.sendEmptyMessageDelayed(0x01,1000);
        }
    }

    private int rotateDegress;
    public void setCameraDisplayOrientation(int cameraId, Camera camera) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
        rotateDegress = degrees;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            takePhoto();
            return true;
        }
        return super.onKeyDown(keyCode, event);

    }

    @SuppressLint("HandlerLeak")
    private final Handler autoFocusHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                if (mCamera != null && isSurfaceCreate) {
                    Camera.Parameters p = mCamera.getParameters();
                    List<String> focusModes = p.getSupportedFocusModes();

                    if (focusModes != null && focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                        mCamera.autoFocus(null);
                    } else {
                        //Phone does not support autofocus!
                    }
                }
            }catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    };
}
