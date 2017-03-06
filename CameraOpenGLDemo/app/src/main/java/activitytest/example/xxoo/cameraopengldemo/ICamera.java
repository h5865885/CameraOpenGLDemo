package activitytest.example.xxoo.cameraopengldemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by xxoo on 2017/2/16.
 */

public class ICamera {
    public android.hardware.Camera mCamera;
    public int cameraWidth;
    public int cameraHeight;
    public int cameraId = 1;//前置摄像头
    public int Angle;

    private static final String TAG = "ICamera";
    
    public ICamera() {

    }

    /**
     * 打开相机
     */
    public Camera openCamera(boolean isBackCamera, Context context, HashMap<String, Integer> resolutionMap) {


        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            if (mCamera == null){
                try {
                    int width = 640;
                    int height = 480;

                    mCamera = Camera.open(cameraId);
                    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                    Camera.getCameraInfo(cameraId,cameraInfo);
                    Camera.Parameters parameters = mCamera.getParameters();
                    Camera.Size bestPreviewSize  = calBestPreviewSize(mCamera.getParameters(),width,height);
                    cameraWidth  = bestPreviewSize.width;
                    cameraHeight = bestPreviewSize.height;
//                    parameters.setPreviewFormat(ImageFormat.NV21);
                    parameters.setPreviewSize(cameraWidth,cameraHeight);
                    Angle = 90;
                    Log.d(TAG, "openCamera  "+cameraWidth+"   "+cameraHeight+" "+bestPreviewSize.toString());
                    mCamera.setParameters(parameters);
                    return mCamera;
                }catch (Exception e){
                    e.printStackTrace();
                    return null;
                }
            }else {
                return null;
            }
        }else {
            Log.d(TAG, "initCamera: error");
            ActivityCompat.requestPermissions((Activity)context,
                    new String[]{Manifest.permission.CAMERA}, 1);
            Log.d(TAG, "openCamera: 申请权限");
            return null;
        }
    }

    /**
     * 开始调用相机
     */
    public void actionDetect(Camera.PreviewCallback mActivity) {
        if (mCamera != null) {
            Log.d(TAG, "actionDetect");
            mCamera.setPreviewCallback(mActivity);
        }
    }

    public void startPreview(SurfaceTexture surfaceTexture) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(surfaceTexture);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 通过传入的宽高算出最接近于宽高值的相机大小
     */
    private Camera.Size calBestPreviewSize(Camera.Parameters camPara,
                                           final int width, final int height) {
        List<Camera.Size> allSupportedSize = camPara.getSupportedPreviewSizes();
        ArrayList<Camera.Size> widthLargerSize = new ArrayList<Camera.Size>();
        for (Camera.Size tmpSize : allSupportedSize) {
            Log.w("ceshi", "tmpSize.width===" + tmpSize.width
                    + ", tmpSize.height===" + tmpSize.height);
            if (tmpSize.width > tmpSize.height) {
                widthLargerSize.add(tmpSize);
            }
        }

        Collections.sort(widthLargerSize, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                int off_one = Math.abs(lhs.width * lhs.height - width * height);
                int off_two = Math.abs(rhs.width * rhs.height - width * height);
                return off_one - off_two;
            }
        });

        return widthLargerSize.get(0);
    }

    // 通过屏幕参数、相机预览尺寸计算布局参数
    public RelativeLayout.LayoutParams getLayoutParam() {
        float scale = cameraWidth * 1.0f / cameraHeight;

//        WindowManager wm =

        int layout_width = Screen.mWidth;
        int layout_height = (int) (layout_width * scale);

        if (Screen.mWidth >= Screen.mHeight) {
            layout_height = Screen.mHeight;
            layout_width = (int) (layout_height / scale);
        }

        RelativeLayout.LayoutParams layout_params = new RelativeLayout.LayoutParams(
                layout_width, layout_height);
        layout_params.addRule(RelativeLayout.CENTER_HORIZONTAL);// 设置照相机水平居中

        return layout_params;
    }
}
