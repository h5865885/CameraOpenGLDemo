package activitytest.example.xxoo.cameraopengldemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by xxoo on 2017/2/9.
 */

public class CameraView extends SurfaceView implements SurfaceHolder.Callback , Camera.PreviewCallback{
//    http://blog.csdn.net/chylove5/article/details/49637535

    private static final String TAG = "CameraView";

    //源视频帧 宽/高
    private int srcFrameWidth  = 640;
    private int srcFrameHeight = 480;
    //数据采集
//    private int curCameraIndex = 1;
    private Camera camera = null;
    //视频帧共享存储回调接口
    private SurfaceHolder surfaceHolder;

    private SaveFrameCallback saveFrameCallback = null;
    private SaveFrameCallback YUVFrameCallback = null;

    private Context _mainContext;

    public CameraView(Context context) {
        super(context);
        _mainContext = context;
//        if (Camera.getNumberOfCameras() > 1){
//            curCameraIndex = 1;
//        }else {
//            curCameraIndex = 0;
//        }
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        Log.d(TAG, "CameraView: create");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        stopCamera();
        Log.d(TAG, "surfaceCreated");
        startCamera(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopCamera();
    }

    //打开摄像头
    private void startCamera(SurfaceHolder holder){
        initCamera(holder);
    }

    //初始化摄像头
    public void initCamera(SurfaceHolder holder){
        //初始化摄像头 并打开
        if (ContextCompat.checkSelfPermission(_mainContext, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            if (camera == null){
                try {
                    camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                }catch (RuntimeException e){
                    Log.d(TAG, "initCamera: Open方法有问题");
                    return;
                }
                Camera.Parameters params = camera.getParameters();
                try {
                    params.setPreviewFormat(ImageFormat.NV21);
                    params.setPreviewSize(srcFrameWidth,srcFrameHeight);
                    camera.setParameters(params);

                    params = camera.getParameters();
//                params.setPreviewFpsRange(15*1000,30*1000);
                }catch (Exception e){
                    Log.d(TAG, "initCamera:"+e);
                }
                try {
                    camera.setPreviewDisplay(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                camera.setPreviewCallback(this);
                camera.startPreview();
                camera.setDisplayOrientation(90);//跟yuv方向一样
            }
        }else {
            Log.d(TAG, "initCamera: error");
            ActivityCompat.requestPermissions((Activity) _mainContext,
                    new String[]{Manifest.permission.CAMERA}, 1);//1 can be another integer
        }
    }

    //停止并释放摄像头
    public void stopCamera(){
        if (camera != null){
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    // 获取摄像头视频数据
    //实时回调
    @Override
    public void onPreviewFrame(byte[] data, Camera camera)
    {
        synchronized (this)
        {
            if (saveFrameCallback != null )
//            Log.d(TAG, "onPreviewFrame 134");
                saveFrameCallback.onSaveFrames(data,data.length);
            if ( YUVFrameCallback != null )
//                Log.d(TAG, "onPreviewFrame 138");
                YUVFrameCallback.onSaveFrames(data,data.length);
        }
    }

    //保存视频帧  interface接口 可以看做是一种特殊的抽象类，可以指定一个类必须做什么，而不是规定它如何去做。
    //在调用的地方 实现 onSaveFrames这个方法....
    // 保存视频帧
    public interface SaveFrameCallback
    {
        public void onSaveFrames(byte[] data, int length);
    }
    //走GLSurfaceView
    public void setSaveFrameCallback(SaveFrameCallback saveFrameCallback)
    {
        this.saveFrameCallback = saveFrameCallback;
    }

    //走DrawYUVView
    public void setDarwYUVFrameCallback(SaveFrameCallback saveFrameCallback)
    {
        this.YUVFrameCallback = saveFrameCallback;
    }

}