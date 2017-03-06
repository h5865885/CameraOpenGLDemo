package lab.sodino.glsurface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
//import android.opengl.Matrix;
import android.graphics.Region;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.megvii.facepp.sdk.Facepp;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.Mac;

import activitytest.example.xxoo.cameraopengldemo.CGRect;
import activitytest.example.xxoo.cameraopengldemo.MagicParams;
import activitytest.example.xxoo.cameraopengldemo.R;
import activitytest.example.xxoo.cameraopengldemo.Screen;

/**
 * Created by xxoo on 2017/2/23.
 */

public class FaceCanvasView extends View {

    private Handler handler;

    private Matrix matrix = new Matrix();

    private static final String TAG = "FaceCanvasView";

    public FaceCanvasView(Context context) {
        super(context);
    }

    public FaceCanvasView(Context context, AttributeSet attr) {

        super(context, attr);

        handler = new Handler();

        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.wechat2);

//        matrix.setScale(1,-1);
    }

    private Facepp.Face[] facesPoints;
    private int cameraWidth, cameraHeight;
    private boolean once = false;
    private float[] values = {1.0f,0.0f,0.0f, 0.0f,-1.0f,500.0f,  0.0f,0.0f,1.0f};

    public void drawPoints(Facepp.Face[] faces, int width, int height) {
        if (MagicParams.bmpRect != null ){
            values[5] = MagicParams.bmpRect.top + MagicParams.bmpRect.height();
            values[2] = MagicParams.bmpRect.left;
            matrix.setValues(values);
            calculatePixels(MagicParams.bmp);
        }
//        float[] values = new float[9];
//        matrix.getValues(values);
        if (faces != null) {
            facesPoints = faces;
            cameraHeight = height;
            cameraWidth = width;
        }
        // 构建Runnable对象，在runnable中更新界面
        new Thread() {
            public void run() {
                handler.post(runnableUi);
            }
        }.start();
    }

    Runnable runnableUi = new Runnable() {
        @Override
        public void run() {
            invalidate();
            //更新界面
        }
    };

    private Paint mPaint = new Paint();
    private Bitmap bitmap;
    private Bitmap testBitmap;
    private Path upPath = new Path();
    private Path downPath = new Path();

    int[] upMousePoints   = {84, 85, 86, 87, 88, 89, 90, 100, 99, 98, 97, 96, 84};//13
    int[] downMousePoints = {84, 96, 103, 102, 101, 100, 90, 91, 92, 93, 94, 95, 84};//13
    private final int number = upMousePoints.length;
    private PointF[] upPoints    = new PointF[number];
    private PointF[] downPoints  = new PointF[number];

    private RectF    upRect      = new RectF();
//    private RectF    downRect   = new RectF();

    private float transitionX(float x){
        return (480 - x) / 480 * Screen.mWidth;
    }

    private float transitionY(float y){
        return y / 640 * this.getHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(Color.argb(255, 150, 5, 210));//a:0-255
        mPaint.setStrokeWidth(0);//画笔宽度
        mPaint.setStyle(Paint.Style.STROKE);
        //画控制点
        upPath.reset();//reset path
//        downPath.reset();

        if (facesPoints != null && facesPoints[0].points.length > 0) {

            for (int i = 0; i < upMousePoints.length; i++) {//上嘴唇
                PointF pointf = facesPoints[0].points[upMousePoints[i]];
                if (upPoints[i] == null) {
                    upPoints[i] = new PointF();
                }
                upPoints[i].set(transitionX(pointf.x), transitionY(pointf.y));
//                canvas.drawPoint(points[i].x, points[i].y, mPaint);
            }

            getBerzierpathControlPoints(upPoints);
//            mPaint.setStrokeWidth(5);
//            for (int i = 0;i<controlPoints.length;i++){
//                PointF pointf = controlPoints[i];
//                canvas.drawPoint(pointf.x,pointf.y, mPaint);
//            }

//            Log.d(TAG, "onDraw: "+upPoints[0].x+"90= "+upPoints[6].x);
            /**
             *  ### face++给的图例是 经Y轴 镜像过的 84点 在 90点 右边
             */

            upPath.moveTo(upPoints[0].x,upPoints[0].y);
            for (int i = 0;i<number-1;i++){
//                Log.d(TAG, "onDraw: i "+i);
                int j = 2 * i -1 < 0?controlPoints.length-1:2*i-1;
                int w = 2 * i < controlPoints.length? 2 * i:0;
                int m = i + 1 < upPoints.length?i+1:0;
//                Log.d(TAG, "onDraw: j="+j+"  w="+w+"  m="+m);
                upPath.cubicTo(controlPoints[j].x,controlPoints[j].y,controlPoints[w].x,
                        controlPoints[w].y,
                        upPoints[m].x,upPoints[m].y);
            }
            upPath.close();
            canvas.drawPath(upPath,mPaint);
//            canvas.clipPath(upPath);
            mPaint.setColor(Color.argb(100, 150, 5, 210));//a:0-255
//            upPath.computeBounds(upRect,false);
//            canvas.drawBitmap(bitmap,upRect.left-50,upRect.top-50,mPaint);


            for (int i = 0; i < downMousePoints.length; i++) {//下嘴唇
                PointF pointf = facesPoints[0].points[downMousePoints[i]];
                if (downPoints[i] == null) {
                    downPoints[i] = new PointF();
                }
                downPoints[i].set(transitionX(pointf.x), transitionY(pointf.y));
//                canvas.drawPoint(points[i].x, points[i].y, mPaint);
            }

            getBerzierpathControlPoints(downPoints);
//            mPaint.setStrokeWidth(5);
//            for (int i = 0;i<controlPoints.length;i++){
//                PointF pointf = controlPoints[i];
//                canvas.drawPoint(pointf.x,pointf.y, mPaint);
//            }
            mPaint.setColor(Color.argb(0, 0, 255, 210));//a:0-255
            upPath.moveTo(downPoints[0].x,downPoints[0].y);
            for (int i = 0;i<number-1;i++){
                int j = 2 * i -1 < 0?controlPoints.length-1:2*i-1;
                int w = 2 * i < controlPoints.length? 2 * i:0;
                int m = i + 1 < downPoints.length?i+1:0;
                upPath.cubicTo(controlPoints[j].x,controlPoints[j].y,controlPoints[w].x,
                        controlPoints[w].y,
                        downPoints[m].x,downPoints[m].y);
            }
            upPath.close();
            canvas.drawPath(upPath,mPaint);
            mPaint.setColor(Color.argb(255, 255, 255, 255));//a:0-255

            if (newBitmap != null){
                canvas.drawBitmap(newBitmap,matrix,mPaint);
            }
        }
//        p.setAntiAlias(true);// 设置画笔的锯齿效果。 true是去除，大家一看效果就明白了
//        canvas.drawCircle(120, 20, 20, p);// 大圆
    }

    PointF[] midPoints1     = new PointF[number];//都是13个
    PointF[] midPoints2     = new PointF[number];
    PointF[] distancePoints = new PointF[number];
    PointF[] controlPoints  = new PointF[number * 2];

    private PointF[] getBerzierpathControlPoints(PointF[] allPoints){
        for (int i = 0;i < allPoints.length;i++){
            if (midPoints1[i] == null){
                midPoints1[i] = new PointF();
            }
            float x = (allPoints[i].x + allPoints[i+1<allPoints.length?i+1:0].x)/2;
            float y = (allPoints[i].y + allPoints[i+1<allPoints.length?i+1:0].y)/2;
            midPoints1[i].set(x,y);
        }
        for (int i = 0;i < midPoints1.length;i++){
            if (midPoints2[i] == null){
                midPoints2[i] = new PointF();
            }
            float x = (midPoints1[i].x + midPoints1[i+1<midPoints1.length?i+1:0].x)/2;
            float y = (midPoints1[i].y + midPoints1[i+1<midPoints1.length?i+1:0].y)/2;
            midPoints2[i].set(x,y);
        }
        for (int i = 0;i < midPoints2.length;i++){
            if (distancePoints[i] == null){
                distancePoints[i] = new PointF();
            }
            float x = (midPoints2[i].x - allPoints[i+1<midPoints2.length?i+1:0].x);
            float y = (midPoints2[i].y - allPoints[i+1<midPoints2.length?i+1:0].y);
            distancePoints[i].set(x,y);
//            Log.d(TAG, "getBerzierpathControlPoints: i="+i+"  "+distancePoints[i]);
        }
        for (int i = 0;i < distancePoints.length;i++){
            if (controlPoints[2*i] == null || controlPoints[2*i+1] ==null){
                controlPoints[2*i] = new PointF();
                controlPoints[2*i+1] = new PointF();
            }
            controlPoints[2*i].set(midPoints1[i].x - distancePoints[i].x,midPoints1[i].y - distancePoints[i].y);
            int j = i+1<midPoints1.length?i+1:0;
            controlPoints[2*i+1].set(midPoints1[j].x - distancePoints[i].x,midPoints1[j].y - distancePoints[i]
                    .y);
        }
        return controlPoints;
    }

    private int Rn = 172,Gn = 0,Bn = 72;
    private double Y1, I, Q, K;
    private double Y,Yt, U, V, Cb,Cr,Cg;
    private double Yn,Un,Vn;
    private double kb = 0.114,kr = 0.299,kg = 0.587;
    private int RR,GG,BB;
    private int width,height;
    private Bitmap newBitmap;
    private Point tempPoint = new Point();

    private void calculatePixels(Bitmap bit) {
        width = bit.getWidth();
        height = bit.getHeight();
        int[] pixels = new int[width * height];//保存所有的像素的数组，图片宽×高
        bit.getPixels(pixels, 0, width, 0, 0, width, height);
        Date curDate = new Date(System.currentTimeMillis());
//        Log.d(TAG, "calculatePixels: new date" + new Date());
        for (int i = 0; i < pixels.length; i++) {
//            tempPoint.set(i -  MagicParams.bmpRect.width() * (i / MagicParams.bmpRect.width()) , i / MagicParams.bmpRect
//                    .width());
//            pathContainRGB(tempPoint);

            int clr = pixels[i];
            int red = (clr & 0x00ff0000) >> 16;  //取高两位 向右移动16位 16进制每一位有 0000,0000,0000,0000
            int green = (clr & 0x0000ff00) >> 8;   //取中两位 向右移动8位  0000,0000
            int blue = clr & 0x000000ff;         //取低两位

            Y1 = 0.299 * red + 0.587 * green + 0.114 * blue;
            I  = 0.596 * red - 0.275 * green - 0.321 * blue;
            Q  = 0.212 * red - 0.523 * green + 0.311 * blue;

//         Y的范围从O到255，I的范围从-152到152，Q的范围从-134到134
//          80~220  12~78  7~25
            if ((Y1 >= 80 && Y1 <= 220 && I >= 12 && I <= 78 && Q >= 7 && Q <= 25)){
//                Log.d(TAG, "calculatePixels: red "+red);
//                (int x, int y, @ColorInt int color)
//                颜色空间算法1
                Y =  0.299 * red + 0.587 * green + 0.114 * blue;
//                U = -0.147 * red - 0.289 * green + 0.436 * blue;
//                V =  0.615 * red - 0.515 * green + 0.100 * blue;
//                int color = Color.parseColor("#ff00ff");
//
                U = -0.147 * Rn - 0.289 * Gn + 0.436 * Bn;
                V =  0.615 * Rn - 0.515 * Gn + 0.100 * Bn;
//
                RR = (int) (Y + 1.14 * V);
                GG = (int) (Y - 0.39 * U - 0.58 * V);
                BB = (int) (Y + 2.03 * U);

                if (RR > 255)
                    RR = 255;
                if (GG > 255)
                    GG = 255;
                if (BB > 255)
                    BB = 255;

                int color2 = Color.argb(255,RR,GG,BB);
                pixels[i] = color2;
            }else {
                int color2 = Color.argb(0,0,0,0);
                pixels[i] = color2;
//                Log.d(TAG, "calculatePixels: NO 符合 red "+red);
            }
        }
        newBitmap = Bitmap.createBitmap(pixels,width,height,bit.getConfig());
    }

    private RectF  r          = new RectF();
    private Region region     = new Region();
    private Region testRegion = new Region();

    private boolean pathContainRGB(Point point){
        upPath.computeBounds(r,true);//计算控制点的边界
        testRegion.set((int)r.left,(int)r.top,(int)r.right,(int)r.bottom);
        region.setPath(upPath, testRegion);
//        Log.d(TAG, "pathContainRGB: "+region.contains(point.x,point.y));
        return  region.contains(point.x,point.y);
    }

    public void onResumeConfig(Context context) {
//            PointF point0 = new PointF(500,100);
//            PointF point1 = new PointF(200,300);
//            PointF point2 = new PointF(500,500);
//            PointF point3 = new PointF(800,300);
//
//            points[0] = point0;
//            points[1] = point1;
//            points[2] = point2;
//            points[3] = point3;
    }
}
