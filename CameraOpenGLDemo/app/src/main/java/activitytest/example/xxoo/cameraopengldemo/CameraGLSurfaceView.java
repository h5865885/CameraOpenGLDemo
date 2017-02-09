package activitytest.example.xxoo.cameraopengldemo;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Created by xxoo on 2017/2/9.
 */

public class CameraGLSurfaceView extends GLSurfaceView implements CameraView.SaveFrameCallback{

    public CameraGLSurfaceView(Context context) {
        super(context);
    }
}
