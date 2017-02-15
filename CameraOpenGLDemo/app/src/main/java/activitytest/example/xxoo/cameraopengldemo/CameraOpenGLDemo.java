package activitytest.example.xxoo.cameraopengldemo;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import lab.sodino.glsurface.CameraGLSurfaceView;
import lab.sodino.glsurface.DrawYUVView;

public class CameraOpenGLDemo extends Activity{

    private static final String TAG = "CameraOpenGLDemo";

    private CameraGLSurfaceView mGLView = null;
    private DrawYUVView mDrawView = null;
    private CameraView cameraView = null;
    private RelativeLayout previewLayout = null,GLpreviewLayout = null,DrawpreviewLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate~~~");
        //创建一个GLSurfaceView实例然后设置为activity的ContentView.
        //
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera_open_gldemo);
        RelativeLayout.LayoutParams layoutParams = null;

        //GL绘制窗口
        GLpreviewLayout = (RelativeLayout)findViewById(R.id.GLpreviewLayout);
        layoutParams = new RelativeLayout.LayoutParams(640,480);
        mGLView = new CameraGLSurfaceView(this);
        GLpreviewLayout.addView(mGLView, layoutParams);

//
        //绘制视频窗口
//        DrawpreviewLayout = (RelativeLayout)findViewById(R.id.DrawpreviewLayout);
//        layoutParams = new RelativeLayout.LayoutParams(640,480);
//        mDrawView = new DrawYUVView(this);
//        DrawpreviewLayout.addView(mDrawView, layoutParams);

        //视频窗口
        previewLayout = (RelativeLayout)findViewById(R.id.previewLayout);
        layoutParams = new RelativeLayout.LayoutParams(480,640);
        cameraView = new CameraView(this);
        cameraView.setSaveFrameCallback(mGLView);
//        cameraView.setDarwYUVFrameCallback(mDrawView);
        previewLayout.addView(cameraView, layoutParams);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResumeConfig();
    }
}
