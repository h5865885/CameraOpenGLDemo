package activitytest.example.xxoo.cameraopengldemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.support.v4.content.ContextCompat;
import android.util.Log;

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
                    Camera.Size bestPreviewSize = calBestPreviewSize(mCamera.getParameters(),width,height);
                    cameraWidth = bestPreviewSize.width;
                    cameraHeight= bestPreviewSize.height;
                    parameters.setPreviewFormat(ImageFormat.NV21);
                    parameters.setPreviewSize(cameraWidth,cameraHeight);
                    Angle = 0;
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
            return null;
        }
    }

    /**
     * 开始调用相机
     */
    public void actionDetect(Camera.PreviewCallback mActivity) {
        if (mCamera != null) {
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
}
