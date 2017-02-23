package activitytest.example.xxoo.cameraopengldemo.activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.camera2.params.Face;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.megvii.facepp.sdk.Facepp;

import java.util.ArrayList;

import activitytest.example.xxoo.cameraopengldemo.CameraView;
import activitytest.example.xxoo.cameraopengldemo.R;
import activitytest.example.xxoo.cameraopengldemo.Screen;
import lab.sodino.glsurface.CameraGLSurfaceView;
import lab.sodino.glsurface.DrawYUVView;
import lab.sodino.glsurface.FaceCanvasView;

public class CameraOpenGLDemo extends Activity implements CameraGLSurfaceView.FacePointsCallback{

    private static final String TAG = "CameraOpenGLDemo";

    private CameraGLSurfaceView mGLSurfaceView = null;
    private FaceCanvasView      faceCanvasView = null;
    private DrawYUVView mDrawView = null;
    private CameraView cameraView = null;
    private RelativeLayout previewLayout = null,GLpreviewLayout = null,DrawpreviewLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate~~~");
        Screen.initialize(this);
        //创建一个GLSurfaceView实例然后设置为activity的ContentView.
        //
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera_open_gldemo);
//        RelativeLayout.LayoutParams layoutParams = null;

//        Log.d(TAG, "onCreate: start");
        mGLSurfaceView = (CameraGLSurfaceView) findViewById(R.id.opengl_layout_surfaceview);
        mGLSurfaceView.setFacePointsCallback(this);
//        Log.d(TAG, "onCreate: end");
        faceCanvasView = (FaceCanvasView) findViewById(R.id.facecanvasview);
//        faceCanvasView.getBackground().setAlpha(50);

        //GL绘制窗口
//        GLpreviewLayout = (RelativeLayout)findViewById(R.id.GLpreviewLayout);
//        layoutParams = new RelativeLayout.LayoutParams(640,480);
//        mGLView = new CameraGLSurfaceView(this);
//        GLpreviewLayout.addView(mGLView, layoutParams);

//
        //绘制视频窗口
//        DrawpreviewLayout = (RelativeLayout)findViewById(R.id.DrawpreviewLayout);
//        layoutParams = new RelativeLayout.LayoutParams(640,480);
//        mDrawView = new DrawYUVView(this);
//        DrawpreviewLayout.addView(mDrawView, layoutParams);

        //视频窗口
//        previewLayout = (RelativeLayout)findViewById(R.id.previewLayout);
//        layoutParams = new RelativeLayout.LayoutParams(480,640);
//        cameraView = new CameraView(this);
//        cameraView.setSaveFrameCallback(mGLView);
//        cameraView.setDarwYUVFrameCallback(mDrawView);
//        previewLayout.addView(cameraView, layoutParams);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLSurfaceView.onResumeConfig(this);
    }

    @Override
    public void onFacePoints(ArrayList list) {
        faceCanvasView.drawPoints(list);
//        faceCanvasView.invalidate();
///        Log.d(TAG, "onFacePoints: !!!"+facepp[0].points.length);
    }
}
