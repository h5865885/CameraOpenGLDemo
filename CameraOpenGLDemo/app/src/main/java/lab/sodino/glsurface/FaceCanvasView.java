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
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.megvii.facepp.sdk.Facepp;

import java.util.ArrayList;

import activitytest.example.xxoo.cameraopengldemo.CGRect;
import activitytest.example.xxoo.cameraopengldemo.R;
import activitytest.example.xxoo.cameraopengldemo.Screen;

/**
 * Created by xxoo on 2017/2/23.
 */

public class FaceCanvasView extends View {

    private Handler handler;

    private static final String TAG = "FaceCanvasView";

    public FaceCanvasView(Context context) {
        super(context);
    }

    public FaceCanvasView(Context context, AttributeSet attr) {

        super(context, attr);

        handler = new Handler();

        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.wechat2);
    }

    private Facepp.Face[] facesPoints;
    private int cameraWidth, cameraHeight;

    public void drawPoints(Facepp.Face[] faces, int width, int height) {
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
    private Bitmap newBitmap;
    private Path upPath = new Path();
    private Path downPath = new Path();

    private CGRect rect = new CGRect();

    int[] upMousePoints = {84, 85, 86, 87, 88, 89, 90, 100, 99, 98, 97, 96, 84};
    int[] downMousePoints = {84, 96, 103, 102, 101, 100, 90, 91, 92, 93, 94, 95, 84};
    private PointF[] points =new PointF[upMousePoints.length];
//    private PointF[]

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
        mPaint.setStrokeWidth(5);//画笔宽度
        mPaint.setStyle(Paint.Style.STROKE);
        //画控制点
        upPath.reset();//reset path

        if (facesPoints != null && facesPoints[0].points.length > 0) {

            for (int i = 0; i < upMousePoints.length; i++) {//上嘴唇
                PointF pointf = facesPoints[0].points[upMousePoints[i]];
                points[i].set(transitionX(pointf.x),transitionY(pointf.y));
            }
//          {84, 85, 86, 87, 88, 89, 90, 100, 99, 98, 97, 96, 84};
            upPath.moveTo(points[0].x,points[0].y);
            upPath.cubicTo(points[85].x,points[85].y-5,points[86].x,points[86].y,points[87].x,points[87].y);

//                canvas.drawPoint((480 - pointf.x) / 480 * Screen.mWidth, pointf.y / 640 * this.getHeight(), mPaint);

            upPath.close();
            canvas.drawPath(upPath,mPaint);
//            canvas.clipPath(upPath);
//            canvas.translate(-rect.width,-rect.height);
//            canvas.drawBitmap(bitmap,rect.x-rect.width-50,rect.y-50,null);
//            canvas.translate(rect.width,rect.height);



            for (int i = 0; i < downMousePoints.length; i++) {//下嘴唇
                PointF pointf = facesPoints[0].points[downMousePoints[i]];
                canvas.drawPoint((480 - pointf.x) / 480 * Screen.mWidth, pointf.y / 640 * this.getHeight(), mPaint);
//              Log.d(TAG, "onDraw: x = " + x + ",,y=" + y + "screen" + this.getHeight());
            }
        }
//        Log.d(TAG, "onDraw: ");
//
//        //起点
//        mPath.moveTo(100, 100);
//        //mPath 控制点 x,y 终点 x,y
//        mPath.quadTo(150, 0, 200, 200);
//        //闭合
//        //画path
//        canvas.drawPath(mPath, mPaint);


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



//        canvas.drawPoint(480/2, 100, mPaint);
//        画线
//        canvas.drawLine(100,100,150,0,mPaint);
//        canvas.drawLine(200,200,150,0,mPaint);
//
//        canvas.clipPath(mPath);
//        canvas.drawBitmap(bitmap,0,0,null);
    }

    public void onResumeConfig(Context context) {

    }
}
