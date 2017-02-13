package lab.sodino.glsurface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.View;

import activitytest.example.xxoo.cameraopengldemo.CameraView;

/**
 * Created by Administrator on 2015/10/29.
 */
public class DrawYUVView extends View implements CameraView.SaveFrameCallback
{
    private static final String TAG = "DrawYUVView";
    private Bitmap baseBmp = null;
    private int frameWidth = 640,frameHeight = 480;
    public DrawYUVView(Context context)
    {
        super(context);
        baseBmp = Bitmap.createBitmap(frameWidth, frameHeight, Bitmap.Config.ARGB_8888);// 底图
        // 背景
        setBackgroundColor(Color.parseColor("#707070"));
    }
    @Override
    protected void onDraw(Canvas canvas)
    {
        canvas.drawBitmap(baseBmp, 0, 0, null);//在 0，0坐标开始画入src
    }
    @Override
    public void onSaveFrames(byte[] data, int length)
    {
        Log.d(TAG, "onSaveFrames: 34");
        baseBmp = rawByteArray2RGBABitmap2(data,frameWidth,frameHeight);
        invalidate();
    }

    public Bitmap rawByteArray2RGBABitmap2(byte[] data, int width, int height)
    {
        int frameSize = width * height;
        int[] rgba = new int[frameSize];
//        byte [] yArray  = new  byte[width*height];
//        byte [] uArray  = new  byte[width*height/4];
//        byte [] vArray  = new  byte[width*height/4];
//        int k           = 0;
//
//        for (int i = 0; i < height; i++)
//        {
//            for (int j = 0; j < width; j++)
//            {
//                yArray[ k ]  = data[i * width + j];
//                k++;
//            }
//        }
//        k = 0;
//        int uvCount = frameSize>>1;
//        //取分量uv值
//        for( int i = 0;i < uvCount ;i+=2 )
//        {
//            uArray[ k ] = data[ frameSize +  i ];
//            vArray[ k ] = data[ frameSize +  i + 1 ];
//            k++;
//        }

        int i = 0,j = 0;

        for ( i = 0; i < height; i++)
        {
            for ( j = 0; j < width; j++)
            {
                int y = (0xff & ((int) data[i * width + j]));
                int u = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 0]));
                int v = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 1]));
//                int y = (0xff & ((int) yArray[i * width + j]));
//                int u = (0xff & ((int) uArray[(i * width + j)>>2]));
//                int v = (0xff & ((int) vArray[(i * width + j)>>2]));

                y = y < 16 ? 16 : y;

                int r = (int)(1.164f * (y - 16) + 1.596f * (v - 128));
                int g = (int)(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = (int)(1.164f * (y - 16) + 2.018f * (u - 128));
//                int r = (int)( y + 1.403 * v );
//                int g = (int)( y - 0.344 * u - 0.714 * v );
//                int b = (int)( y + 1.770 * u );

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                rgba[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
            }
        }

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.setPixels(rgba, 0 , width, 0, 0, width, height);
        return bmp;
    }
}
