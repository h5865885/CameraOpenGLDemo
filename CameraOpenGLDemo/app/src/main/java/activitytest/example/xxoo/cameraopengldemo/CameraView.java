package activitytest.example.xxoo.cameraopengldemo;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by xxoo on 2017/2/9.
 */

public class CameraView extends SurfaceView implements SurfaceHolder.Callback , Camera.PreviewCallback{
    public CameraView(Context context) {
        super(context);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
