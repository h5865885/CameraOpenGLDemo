package activitytest.example.xxoo.cameraopengldemo;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by xxoo on 2017/2/10.
 */

public class MyGL20Render implements GLSurfaceView.Renderer{
    //初始化 设置OpenGl背景色 开启纹理绘制等
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }
    //在尺寸发送变化时调用
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }
    //用于具体的绘制操作
    @Override
    public void onDrawFrame(GL10 gl) {

    }
}
