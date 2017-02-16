package lab.sodino.glsurface;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.opengl.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.megvii.facepp.sdk.Facepp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import activitytest.example.xxoo.cameraopengldemo.CameraMatrix;
import activitytest.example.xxoo.cameraopengldemo.CameraOpenGLDemo;
import activitytest.example.xxoo.cameraopengldemo.CameraView;
import activitytest.example.xxoo.cameraopengldemo.ConUtil;
import activitytest.example.xxoo.cameraopengldemo.ICamera;
import activitytest.example.xxoo.cameraopengldemo.PointsMatrix;
import activitytest.example.xxoo.cameraopengldemo.R;

/**
 * Created by chenyan on 2015/10/21.
 */
//imlements类似 代理...回调执行的方法,java可以多继承 this 也可以指代 render,saveFrameCallBack
public class CameraGLSurfaceView extends GLSurfaceView implements CameraView.SaveFrameCallback ,GLSurfaceView
        .Renderer,SurfaceTexture.OnFrameAvailableListener,Camera.PreviewCallback
{
    private static final String TAG = "CameraGLSurfaceView";
    // 源视频帧宽/高
    private int srcFrameWidth  = 640;
    private int srcFrameHeight = 480;
    private int viewWidth = 0, viewHeight = 0;
    private int frameWidth = 640, frameHeight = 480;
    private Facepp _facepp;
    private Handler _mHandler;//线程
    private HandlerThread _mHandlerThread;
    private int detection_interval = 25;
    private int min_face_size = 200;
    private float roi_ratio = 0.8f;
    private boolean isStartRecorder, is3DPose, isDebug, isROIDetect, is106Points, isBackCamera, isFaceProperty,
            isSmooth;

    private boolean isTiming = true; // 是否是定时去刷新界面;
    private int printTime = 31;

    private ByteBuffer yBuf = null, uBuf = null, vBuf = null;
    //    private ByteBuffer mBuf = null;
    private  int yuvFrameSize = 640*480;
    // 纹理id
    private int[] Ytexture = new int[1];
    private int[] Utexture = new int[1];
    private int[] Vtexture = new int[1];
    private int[] Mtexture = new int[1];
    private int aPositionMain = 0, aTexCoordMain = 0,  uYTextureMain = 0, uUTextureMain = 0, uVTextureMain = 0,uMTextureMain = 0;
    private int programHandleMain = 0;
    private static final int FLOAT_SIZE_BYTES = 4;

    private CameraMatrix mCameraMatrix;
    private PointsMatrix mPointsMatrix;
    private ICamera mICamera;

    private FloatBuffer squareVertices = null;
    private FloatBuffer coordVertices = null;
    private boolean mbpaly = false;

    //**Frame Buffer Object管理----------------------------------------------------------------------------------------/
    private IntBuffer frameBufferHandle = null;
    private IntBuffer textureHandle = null;
    private int fboWidth = 0, fboHeight = 0;
//    private Bitmap baseBmp = null;

    public CameraGLSurfaceView(Context context, AttributeSet attrs){
        super(context,attrs);
        init();
        Log.d(TAG, "CameraGLSurfaceView attribute");
    }

    public CameraGLSurfaceView(Context context)
    {
        super(context);
        Log.d(TAG, "CameraGLSurfaceView");
        init();
//        baseBmp = Bitmap.createBitmap(frameWidth, frameHeight, Bitmap.Config.ARGB_8888);// 底图
//        Canvas baseCanvas = new Canvas();
//        baseCanvas.setBitmap(baseBmp);
//        baseBmp.copyPixelsToBuffer(mBuf);
    }

    private void init(){
        _facepp = new Facepp();
        _mHandlerThread = new HandlerThread("facepp");
        _mHandlerThread.start();
        _mHandler = new Handler(_mHandlerThread.getLooper());

        //不设置版本会崩溃
        setEGLContextClientVersion(2);
        //设置Renderer到GLSurfaceView 类似设置代理回调...
        setRenderer(this);
        // RENDERMODE_CONTINUOUSLY不停渲染
        // RENDERMODE_WHEN_DIRTY懒惰渲染，需要手动调用 glSurfaceView.requestRender() 才会进行更新
        // 只有在绘制数据改变时才绘制view
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mICamera = new ICamera();

//        2.2读入数据，并将YUV数据分别制作成3个纹理
//        int qtrFrameSize = yuvFrameSize >> 2;
//        yBuf = ByteBuffer.allocateDirect(yuvFrameSize);
//        yBuf.order(ByteOrder.nativeOrder()).position(0);
//
//        uBuf = ByteBuffer.allocateDirect(qtrFrameSize);
//        uBuf.order(ByteOrder.nativeOrder()).position(0);
//
//        vBuf = ByteBuffer.allocateDirect(qtrFrameSize);
//        vBuf.order(ByteOrder.nativeOrder()).position(0);
//
////        mBuf = ByteBuffer.allocateDirect(yuvFrameSize * 4);
////        mBuf.order(ByteOrder.nativeOrder()).position(0);
//        // 顶点坐标
//        squareVertices = ByteBuffer.allocateDirect(util.squareVertices.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
//        squareVertices.put(util.squareVertices).position(0);
//        //纹理坐标
//        coordVertices = ByteBuffer.allocateDirect(util.coordVertices.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
//        coordVertices.put(util.coordVertices).position(0);
    }


    private int mTextureID = -1;
    private SurfaceTexture mSurface;

    /**
     * render的3个回调函数 必须执行
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        Log.d(TAG, "onSurfaceCreated");
        
        GLES20.glClearColor(0.5f,0.0f,0.0f,1.0f);
        int[] texture = new int[1];
        //create TextureID 创建纹理
        GLES20.glGenTextures(1, texture, 0);
        //绑定纹理
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        mTextureID = texture[0];
        mSurface = new SurfaceTexture(mTextureID);
        // 这个接口就干了这么一件事，当有数据上来后会进到onFrameAvailable方法
        mSurface.setOnFrameAvailableListener(this);
        mCameraMatrix = new CameraMatrix(mTextureID);
        mPointsMatrix = new PointsMatrix();

        mICamera.startPreview(mSurface);
//        mICamera.actionDetect(mICamera); 代理回调需要在哪实现...一样一样的
        mICamera.actionDetect(this);
        if (isTiming) {
            timeHandle.sendEmptyMessageDelayed(0, printTime);
        }
    }


    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjMatrix = new float[16];
    private final float[] mVMatrix = new float[16];

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged");
        //设置画面的大小
        GLES20.glViewport(0,0,srcFrameWidth,srcFrameHeight);
        float ratio = (float) srcFrameWidth / srcFrameHeight;
        ratio = 1;
//        projection matrix is applied to object coordinates
//        in the onDrawFrame() method
        Matrix.frustumM(mProjMatrix,0,-ratio,ratio,-1,1,3,7);
        // Matrix.perspectiveM(mProjMatrix, 0, 0.382f, ratio, 3, 700);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.d(TAG, "onDrawFrame");
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);// 清除屏幕和深度缓存
        float[] mtx = new float[16];
        mSurface.getTransformMatrix(mtx);
        mCameraMatrix.draw(mtx);
        // Set the camera position (View matrix)
        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1f, 0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

        mPointsMatrix.draw(mMVPMatrix);
        mSurface.updateTexImage();//更新image 会调用onFrameAvailable方法
    }

    //相机的回调函数
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
//        Log.d(TAG, "onPreviewFrame: 相机回调");
        onSaveFrames(data,camera);
    }

    //surfaceTexture代理回调
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        // TODO Auto-generated method stub
    }

    public  int  getShaderHandle(int programHandle,String name)
    {
        int handle = GLES20.glGetAttribLocation(programHandle, name);
        if (handle == -1)
        {
            handle = GLES20.glGetUniformLocation(programHandle, name);
        }
        return handle;
    }

    public void onResumeConfig(Context context) {
        Log.d(TAG, "onResumeConfig:openCamera");
        mICamera.openCamera(false,context,null);

//        mCamera = mICamera.openCamera(isBackCamera, this, resolutionMap);

        String errorCode = _facepp.init(getContext(), ConUtil.getFileContent(getContext(), R.raw
                .megviifacepp_0_4_1_model));
        Log.d(TAG, "errorcode :"+errorCode);
        Facepp.FaceppConfig faceppConfig = _facepp.getFaceppConfig();
        faceppConfig.interval = detection_interval;//表示每隔多少帧进行一次全图的人脸检测
         _facepp.setFaceppConfig(faceppConfig);
//        faceppConfig.roi_left =
    }


    float pitch,//一个弧度，表示物体顺时针饶x轴旋转的弧度。
            yaw,//一个弧度，表示物体顺时针饶y轴旋转的弧度。
            roll;//一个弧度，表示物体顺时针饶z轴旋转的弧度。
    float confidence;//人脸置信度

    @Override
    public void onSaveFrames(byte[] data, int length) {

    }

    /**
     * 核心方法...
     */
    public void onSaveFrames(final byte[] data, Camera camera)
    {
//        Log.d(TAG, "onSaveFrames.254");

        _mHandler.post(new Runnable() {
            @Override
            public void run() {
                //face数组 考虑多张脸的情况
                final Facepp.Face[] faces = _facepp.detect(data,srcFrameWidth,srcFrameHeight,Facepp.IMAGEMODE_NV21);
                if (faces != null){
                    ArrayList pointsOpengl = new ArrayList();
                    if (faces.length>0){
//                        float x = faces[0].points[0].x;
//                    Log.d(TAG, "run: faces "+x); Log.d(TAG, "run: facesCount "+faces[0].points
// .length)
                        for (int i = 0; i < faces.length;i++){//只执行一次 基本上 1张脸
//                            * 获取指定人脸的Landmark信息，并改变传入的人脸信息
                            _facepp.getLandmark(faces[i],is106Points?Facepp
                                        .FPP_GET_LANDMARK106:Facepp.FPP_GET_LANDMARK81);
                            Facepp.Face face = faces[i];
                            if (isFaceProperty){
                                //暂无
                            }
                            pitch = faces[i].pitch;
                            yaw   = faces[i].yaw;
                            roll  = faces[i].roll;
                            confidence = faces[i].confidence;

                            ArrayList<FloatBuffer> triangleVBList = new ArrayList<FloatBuffer>();
                            for (int j = 0; j < faces[i].points.length; j++){
                                float x = (faces[i].points[j].x/srcFrameHeight) * 2 - 1;
                                float y = 1-(faces[i].points[j].y/srcFrameWidth) * 2;
                                float[] pointf = new float[]{x,y,0.0f};
                                //默认orientation = 0;
//                                FloatBuffer floatBuffer =
                                FloatBuffer floatBuffer = mCameraMatrix.floatBufferUtil(pointf);
                                triangleVBList.add(floatBuffer);
                            }
                            pointsOpengl.add(triangleVBList);
                        }

                        if (faces.length > 0 && is3DPose){
//                        mPointsMatrix.bottomVertexBuffer = OpenGLDrawRect.drawBottomShowRect(0.15f, 0, -0.7f, pitch,
//                                -yaw, roll, 0);
                        }else {
                            mPointsMatrix.bottomVertexBuffer = null;
                        }
                        synchronized (mPointsMatrix){
                            mPointsMatrix.points = pointsOpengl;
                        }
                    }else {
                        pitch = 0;
                        yaw   = 0;
                        roll  = 0;
                    }
                }else {
                    Log.d(TAG, "run: faces = null 为空");
                }
                if (!isTiming) {
                    timeHandle.sendEmptyMessage(1);
                }
            }
        });

        //        //先执行旋转...
//        byte[] tempData1 = new byte[srcFrameWidth * srcFrameWidth * 3/2];
//        rotateYUV240SP(data,tempData1,srcFrameWidth,srcFrameHeight);
////        byte[] tempData2 = new byte[srcFrameWidth * srcFrameWidth * 3/2];
////        rotateYUV240SP(tempData1,tempData2,srcFrameHeight,srcFrameWidth);
//        //帧数回调走这...
////        Log.d(TAG, "onSaveFrames: 235");
//        if (length != 0 && mbpaly )
//        {
//            yBuf.clear();
//            uBuf.clear();
//            vBuf.clear();
//            rotateYUV(tempData1, srcFrameHeight, srcFrameWidth);
//            requestRender();
//        }
    }

    public void setConfig(int rotation){
        Facepp.FaceppConfig faceppConfig = _facepp.getFaceppConfig();
        faceppConfig.rotation = rotation;
        _facepp.setFaceppConfig(faceppConfig);
    }

    public static byte[] rotateYUV240SP(byte[] src,byte[] des,int width,int height)
    {
        int wh = width * height;
        //旋转Y
        int k = 0;
        for(int i=0;i<width;i++) {
            for(int j=0;j<height;j++)
            {
                des[k] = src[width*j + i];
                k++;
            }
        }

        for(int i=0;i<width;i+=2) {
            for(int j=0;j<height/2;j++)
            {
                des[k] = src[wh+ width*j + i];
                des[k+1]=src[wh + width*j + i+1];
                k+=2;
            }
        }

        return des;

        //传说中经过优化的
//        int wh = width * height;
//        int uvHeight = height >> 1;
//        //旋转Y
//        int k = 0;
//        for(int i = 0; i < width; i++) {
//            int nPos = 0;
//            for(int j = 0; j < height; j++) {
//                des[k] = src[nPos + i];
//                k++;
//                nPos += width;
//            }
//        }
//
//        for(int i = 0; i < width; i+=2){
//            int nPos = wh;
//            for(int j = 0; j < uvHeight; j++) {
//                des[k] = src[nPos + i];
//                des[k + 1] = src[nPos + i + 1];
//                k += 2;
//                nPos += width;
//            }
//        }
//        return des;
    }

    public void rotateYUV(byte[] src,int width,int height)
    {
        byte [] yArray = new  byte[yBuf.limit()];
        byte [] uArray = new  byte[uBuf.limit()];
        byte [] vArray = new  byte[vBuf.limit()];
        int nFrameSize = width * height;
        int k          = 0;
        int uvCount    = nFrameSize>>1;

        //取分量y值 一个像素一个Y值 YYYYUV
        for(int i = 0;i < height*width;i++ )
        {
            yArray[ k ] = src[ i ];
            k++;
        }

        k = 0;

        //取分量uv值
        for( int i = 0;i < uvCount ;i+=2 )
        {
            vArray[ k ] = src[nFrameSize +  i ]; //v
            uArray[ k ] = src[nFrameSize +  i + 1 ];//u
            k++;
        }
//        yuv420 又分为
//        I420: YYYYYYYY UU VV   =>YUV420P
//        YV12: YYYYYYYY VV UU   =>YUV420P
//        NV12: YYYYYYYY UVUV    =>YUV420SP
//        NV21: YYYYYYYY VUVU    =>YUV420SP

        yBuf.put(yArray).position(0);
        uBuf.put(uArray).position(0);
        vBuf.put(vArray).position(0);
    }


//    public void initFBO(int nWidth,int nHeight)
//    {
//        fboWidth           = nWidth;
//        fboHeight          = nHeight;
//        frameBufferHandle  = IntBuffer.allocate(1);
//        textureHandle      = IntBuffer.allocate(1);
//
//        // 生成fbo
//        GLES20.glGenFramebuffers(1, frameBufferHandle);
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferHandle.get(0));
//
//        // 将纹理图像与FBO关联
//        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
//                GLES20.GL_COLOR_ATTACHMENT0,
//                GLES20.GL_TEXTURE_2D,
//                Ytexture[0],
//                0);
//
//        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
//                GLES20.GL_COLOR_ATTACHMENT0,
//                GLES20.GL_TEXTURE_2D,
//                Utexture[0],
//                0);
//
//        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
//                GLES20.GL_COLOR_ATTACHMENT0,
//                GLES20.GL_TEXTURE_2D,
//                Vtexture[0],
//                0);
//    }
//    public void bindFBO()
//    {
//        //启动fbo
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBufferHandle.get(0));
//
//    }
//    public void useFBOTexture()
//    {
//        //id置为0时,不再使用FBO
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
//
//    }
//    public void deleteFBO()
//    {
//        GLES20.glDeleteFramebuffers(1, frameBufferHandle);
//        //       GLES20.glDeleteTextures(1, textureHandle);
//        frameBufferHandle.clear();
////        textureHandle.clear();
//        fboWidth = fboHeight = 0;
//    }

    /**
     * 回调 代理回调也可以类似 class 去执行
     */

    public class MyGL20Renderer implements GLSurfaceView.Renderer
    {

        public void onSurfaceCreated(GL10 unused, EGLConfig config)
        {
            Log.d(TAG, "onSurfaceCreated:");
            mbpaly = false;
            //设置背景的颜色
            GLES20.glClearColor(1.0f, 0.5f, 0.5f, 1.0f);
            //启动纹理
            GLES20.glEnable(GLES20.GL_TEXTURE_2D);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE3);

            changeFilterShader(0);
            //创建yuv纹理
            createTexture(frameHeight,frameWidth, GLES20.GL_LUMINANCE, Ytexture);
            createTexture(frameHeight>>1,frameWidth>>1,  GLES20.GL_LUMINANCE, Utexture);
            createTexture(frameHeight>>1,frameWidth>>1, GLES20.GL_LUMINANCE, Vtexture);
            createTexture(frameHeight,frameWidth, GLES20.GL_RGBA, Mtexture);

            mbpaly = true;
        }

        private void changeFilterShader(int filterId)
        {
            programHandleMain = util.createShaderProgram();
            if ( programHandleMain != -1 )
            {
                // 获取VertexShader变量
                aPositionMain = getShaderHandle(programHandleMain, "vPosition");
                aTexCoordMain = getShaderHandle(programHandleMain, "a_texCoord");
                // 获取FrameShader变量
                uYTextureMain = getShaderHandle(programHandleMain, "SamplerY");
                uUTextureMain = getShaderHandle(programHandleMain, "SamplerU");
                uVTextureMain = getShaderHandle(programHandleMain, "SamplerV");
                uMTextureMain = getShaderHandle(programHandleMain, "SamplerM");

                // 使用滤镜着色器程序
                GLES20.glUseProgram(programHandleMain);

                //给变量赋值
                GLES20.glUniform1i(uYTextureMain, 0);
                GLES20.glUniform1i(uUTextureMain, 1);
                GLES20.glUniform1i(uVTextureMain, 2);
                GLES20.glUniform1i(uMTextureMain, 3);
                GLES20.glEnableVertexAttribArray(aPositionMain);
                GLES20.glEnableVertexAttribArray(aTexCoordMain);

                // 设置Vertex Shader数据
                squareVertices.position(0);
                GLES20.glVertexAttribPointer(aPositionMain, 2, GLES20.GL_FLOAT, false, 0, squareVertices);
                coordVertices.position(0);
                GLES20.glVertexAttribPointer(aTexCoordMain, 2, GLES20.GL_FLOAT, false, 0, coordVertices);
            }
        }

        // 创建纹理
        private void createTexture(int width, int height, int format, int[] textureId)
        {
            //创建纹理
            GLES20.glGenTextures(1, textureId, 0);
            //绑定纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);
            //设置纹理属性
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, format, width, height, 0, format, GLES20.GL_UNSIGNED_BYTE, null);
        }
        public void onDrawFrame(GL10 unused)
        {
            // 重绘背景色
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            if ( yBuf != null )
            {
                //y
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, Ytexture[0]);
                GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D,
                        0,
                        0,
                        0,
                        frameHeight,
                        frameWidth,
                        GLES20.GL_LUMINANCE,
                        GLES20.GL_UNSIGNED_BYTE,
                        yBuf);

                //u
                GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, Utexture[0]);
                GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D,
                        0,
                        0,
                        0,
                        frameHeight >> 1,
                        frameWidth >> 1,
                        GLES20.GL_LUMINANCE,
                        GLES20.GL_UNSIGNED_BYTE,
                        uBuf);

                //v
                GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, Vtexture[0]);
                GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D,
                        0,
                        0,
                        0,
                        frameHeight >> 1,
                        frameWidth >> 1,
                        GLES20.GL_LUMINANCE,
                        GLES20.GL_UNSIGNED_BYTE,
                        vBuf);

                //mark图层
                GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, Mtexture[0]);
                GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D, 0, 0, 0, frameHeight,frameWidth,  GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            }

            //绘制  height = 480 width = 640
            GLES20.glViewport(0, 0, viewHeight, viewWidth);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        }

        public void onSurfaceChanged(GL10 unused, int width, int height)
        {
            viewWidth  = width;
            viewHeight = height;
            GLES20.glViewport(0, 0, viewHeight, viewWidth);
        }
    }

    Handler timeHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Log.d(TAG, "handleMessage: 0");
                    requestRender();// 发送去绘制照相机不断去回调
                    timeHandle.sendEmptyMessageDelayed(0, printTime);
                    break;
                case 1:
                    Log.d(TAG, "handleMessage: 1");
                    requestRender();// 发送去绘制照相机不断去回调
                    break;
            }
        }
    };
}
