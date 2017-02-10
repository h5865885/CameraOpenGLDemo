package activitytest.example.xxoo.cameraopengldemo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.RelativeLayout;

public class CameraOpenGLDemo extends Activity {

//    private
    private static final String TAG = "CameraOpenGLDemo";

    private CameraGLSurfaceView mGLView = null;
//    private DrawYUVView
//    private RelativeLayout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_open_gldemo);
    }
}
