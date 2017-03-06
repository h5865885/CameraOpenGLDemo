package activitytest.example.xxoo.cameraopengldemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Environment;

/**
 * Created by xxoo on 2017/2/28.
 */

public class MagicParams {
    public static Context context;
    public static Bitmap bmp;
    public static Rect   bmpRect;

//    public static MagicBaseView magicBaseView;
    public static String videoPath = Environment.getExternalStorageDirectory().getPath();
    public static String videoName = "test.mp4";
    public static int beautyLevel  = 5;
    public MagicParams(){

    }
}
