package lab.sodino.glsurface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import com.megvii.facepp.sdk.Facepp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import activitytest.example.xxoo.cameraopengldemo.CameraView;
import activitytest.example.xxoo.cameraopengldemo.ConUtil;
import activitytest.example.xxoo.cameraopengldemo.R;

/**
 * Created by chenyan on 2015/10/21.
 */
public class CameraGLSurfaceView extends GLSurfaceView implements CameraView.SaveFrameCallback
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

    private FloatBuffer squareVertices = null;
    private FloatBuffer coordVertices = null;
    private boolean mbpaly = false;

    //**Frame Buffer Object管理----------------------------------------------------------------------------------------/
    private IntBuffer frameBufferHandle = null;
    private IntBuffer textureHandle = null;
    private int fboWidth = 0, fboHeight = 0;
//    private Bitmap baseBmp = null;

    public CameraGLSurfaceView(Context context)
    {
        super(context);
        Log.d(TAG, "CameraGLSurfaceView");

        init();

        setEGLContextClientVersion(2);
        //设置Renderer到GLSurfaceView
        setRenderer(new MyGL20Renderer());
        // 只有在绘制数据改变时才绘制view
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

//        2.2读入数据，并将YUV数据分别制作成3个纹理
        int qtrFrameSize = yuvFrameSize >> 2;
        yBuf = ByteBuffer.allocateDirect(yuvFrameSize);
        yBuf.order(ByteOrder.nativeOrder()).position(0);

        uBuf = ByteBuffer.allocateDirect(qtrFrameSize);
        uBuf.order(ByteOrder.nativeOrder()).position(0);

        vBuf = ByteBuffer.allocateDirect(qtrFrameSize);
        vBuf.order(ByteOrder.nativeOrder()).position(0);

//        mBuf = ByteBuffer.allocateDirect(yuvFrameSize * 4);
//        mBuf.order(ByteOrder.nativeOrder()).position(0);
        // 顶点坐标
        squareVertices = ByteBuffer.allocateDirect(util.squareVertices.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        squareVertices.put(util.squareVertices).position(0);
        //纹理坐标
        coordVertices = ByteBuffer.allocateDirect(util.coordVertices.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        coordVertices.put(util.coordVertices).position(0);

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
    }

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

    public  int  getShaderHandle(int programHandle,String name)
    {
        int handle = GLES20.glGetAttribLocation(programHandle, name);
        if (handle == -1)
        {
            handle = GLES20.glGetUniformLocation(programHandle, name);
        }
        return handle;
    }

    public void onResumeConfig() {
        String errorCode = _facepp.init(getContext(), ConUtil.getFileContent(getContext(), R.raw
                .megviifacepp_0_4_1_model));
        Log.d(TAG, "errorcode :"+errorCode);
        Facepp.FaceppConfig faceppConfig = _facepp.getFaceppConfig();
        faceppConfig.interval = detection_interval;//表示每隔多少帧进行一次全图的人脸检测
         _facepp.setFaceppConfig(faceppConfig);
//        faceppConfig.roi_left =
    }

    /**
     * 核心方法...
     */
    public void onSaveFrames(final byte[] data, int length)
    {
//        Log.d(TAG, "onSaveFrames.254");

        _mHandler.post(new Runnable() {
            @Override
            public void run() {
                final Facepp.Face[] faces = _facepp.detect(data,srcFrameWidth,srcFrameHeight,Facepp.IMAGEMODE_NV21);
//                faces.
                Log.d(TAG, "run: faces");
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
     * Created by Administrator on 2015/10/29.
     */
    /*
        暂时没用到..?
    */
//    public static class DrawYUVView extends View implements CameraView.SaveFrameCallback
//    {
//        private static final String TAG = "DrawYUVView";
//        private Bitmap baseBmp = null;
//        private int frameWidth = 640,frameHeight = 480;
//        public DrawYUVView(Context context)
//        {
//            super(context);
//            Log.d(TAG, "DrawYUVView");
//            baseBmp = Bitmap.createBitmap(frameWidth, frameHeight, Bitmap.Config.ARGB_8888);// 底图
//            // 背景
//            setBackgroundColor(Color.parseColor("#707070"));
//        }
//        @Override
//        protected void onDraw(Canvas canvas)
//        {
//            canvas.drawBitmap(baseBmp, 0, 0, null);//在 0，0坐标开始画入src
//        }
//        @Override
//        public void onSaveFrames(byte[] data, int length)
//        {
//            Log.d(TAG, "onSaveFrames ");
//            baseBmp = rawByteArray2RGBABitmap2(data,frameWidth,frameHeight);
//            invalidate();
//        }
//
//        public Bitmap rawByteArray2RGBABitmap2(byte[] data, int width, int height)
//        {
//            Log.d(TAG, "rawByteArray2RGBABitmap2");
//            int frameSize = width * height;
//            int[] rgba = new int[frameSize];
//    //        byte [] yArray  = new  byte[width*height];
//    //        byte [] uArray  = new  byte[width*height/4];
//    //        byte [] vArray  = new  byte[width*height/4];
//    //        int k           = 0;
//    //
//    //        for (int i = 0; i < height; i++)
//    //        {
//    //            for (int j = 0; j < width; j++)
//    //            {
//    //                yArray[ k ]  = data[i * width + j];
//    //                k++;
//    //            }
//    //        }
//    //        k = 0;
//    //        int uvCount = frameSize>>1;
//    //        //取分量uv值
//    //        for( int i = 0;i < uvCount ;i+=2 )
//    //        {
//    //            uArray[ k ] = data[ frameSize +  i ];
//    //            vArray[ k ] = data[ frameSize +  i + 1 ];
//    //            k++;
//    //        }
//
//            int i = 0,j = 0;
//
//            for ( i = 0; i < height; i++)
//            {
//                for ( j = 0; j < width; j++)
//                {
//                    int y = (0xff & ((int) data[i * width + j]));
//                    int u = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 0]));
//                    int v = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 1]));
//    //                int y = (0xff & ((int) yArray[i * width + j]));
//    //                int u = (0xff & ((int) uArray[(i * width + j)>>2]));
//    //                int v = (0xff & ((int) vArray[(i * width + j)>>2]));
//
//                    y = y < 16 ? 16 : y;
//
//                    int r = (int)(1.164f * (y - 16) + 1.596f * (v - 128));
//                    int g = (int)(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
//                    int b = (int)(1.164f * (y - 16) + 2.018f * (u - 128));
//    //                int r = (int)( y + 1.403 * v );
//    //                int g = (int)( y - 0.344 * u - 0.714 * v );
//    //                int b = (int)( y + 1.770 * u );
//
//                    r = r < 0 ? 0 : (r > 255 ? 255 : r);
//                    g = g < 0 ? 0 : (g > 255 ? 255 : g);
//                    b = b < 0 ? 0 : (b > 255 ? 255 : b);
//
//                    rgba[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
//                }
//            }
//
//            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//            bmp.setPixels(rgba, 0 , width, 0, 0, width, height);
//
//            return bmp;
//        }
//    }
}
