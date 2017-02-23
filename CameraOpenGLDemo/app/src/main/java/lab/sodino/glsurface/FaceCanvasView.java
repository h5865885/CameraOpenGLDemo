package lab.sodino.glsurface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.megvii.facepp.sdk.Facepp;

import java.util.ArrayList;

import activitytest.example.xxoo.cameraopengldemo.Screen;

/**
 * Created by xxoo on 2017/2/23.
 */

public class FaceCanvasView extends View {

    private Paint mPaint = new Paint();
    private Path mPath  = new Path();
    private Bitmap bitmap;
    private Handler handler;
    PointF[] points;

    private static final String TAG = "FaceCanvasView";
    
    public FaceCanvasView(Context context) {
        super(context);
    }

    public FaceCanvasView(Context context, AttributeSet attr){

        super(context,attr);

        handler=new Handler();

//        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.reddd);
    }

    private ArrayList listPoints;

    public void drawPoints(ArrayList list){
        if (list != null)
            listPoints = list;
        // 构建Runnable对象，在runnable中更新界面
        new Thread(){
            public void run(){
                handler.post(runnableUi);
            }
        }.start();
    }

    Runnable  runnableUi=new  Runnable(){
        @Override
        public void run() {
            invalidate();
            //更新界面
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        Log.d(TAG, "onDraw: ");
        mPaint.setColor(Color.argb(255,150,5,210));//画笔颜色  a:0-255
        mPaint.setStrokeWidth(10);//画笔宽度
        mPaint.setStyle(Paint.Style.STROKE);

        mPath.reset();//reset path
        //起点
        mPath.moveTo(100,100);
        //mPath 控制点 x,y 终点 x,y
        mPath.quadTo(150,0,200,200);
        //闭合
        mPath.close();
        //画path
        canvas.drawPath(mPath,mPaint);


        // 创建画笔
//        Paint p = new Paint();
//        p.setColor(Color.RED);// 设置红色
//
//        canvas.drawText("画圆：", 10, 20, p);// 画文本
//        canvas.drawCircle(60, 20, 10, p);// 小圆
//        p.setAntiAlias(true);// 设置画笔的锯齿效果。 true是去除，大家一看效果就明白了
//        canvas.drawCircle(120, 20, 20, p);// 大圆

//        mPath.moveTo(300,300);
//        mPath.quadTo(350,0,400,400);
//        mPath.close();


        //画控制点
        if (points != null && points.length > 0)
            canvas.drawPoint(listPoints[45][0] * Screen.mWidth,points[46].y,mPaint);
        //画线
//        canvas.drawLine(100,100,150,0,mPaint);
//        canvas.drawLine(200,200,150,0,mPaint);
//
//        canvas.clipPath(mPath);
//        canvas.drawBitmap(bitmap,0,0,null);
    }
}
