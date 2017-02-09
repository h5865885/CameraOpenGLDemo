package activitytest.example.xxoo.cameraopengldemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by xxoo on 2017/2/9.
 */

public class CameraView extends SurfaceView implements SurfaceHolder.Callback , Camera.PreviewCallback{
//    http://blog.csdn.net/chylove5/article/details/49637535
    //源视频帧 宽/高
    private int srcFrameWidth  = 640;
    private int srcFrameHeight = 480;
    //数据采集
    private int curCameraIndex = 1;
    private Camera camera = null;
    //视频帧共享存储回调接口
    private SurfaceHolder surfaceHolder;

    public CameraView(Context context) {
        super(context);
        if (Camera.getNumberOfCameras() > 1){
            curCameraIndex = 1;
        }else {
            curCameraIndex = 0;
        }
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        stopCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    //打开摄像头
    private void startCamera(int cameraIndex){
        initCamera(cameraIndex);
    }

    //初始化摄像头
    public void initCamera(int cameraIndex){
        //初始化摄像头 并打开
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {

        }else {
            ActivityCompat.requestPermissions(this,
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
}