package activitytest.example.xxoo.cameraopengldemo;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

/**
 * Created by xxoo on 2017/2/17.
 */

public class Screen {

    private static final String TAG = "Screen";

    public static int mScreenWidth;
    public static int mScreenHeight;

    public static int mWidth;
    public static int mHeight;

    public static float density;

    public static void initialize(Context context){
        Resources res = context.getResources();
        DisplayMetrics metrics = res.getDisplayMetrics();
        density = metrics.density;

        mWidth = metrics.widthPixels;// - (int)(50 * density)
        mHeight = metrics.heightPixels/* - mNotificationBarHeight */;//
        Log.d(TAG, "initialize: px"+mWidth+"+++"+mHeight);

        mScreenWidth = metrics.widthPixels;// - (int)(50 * density)
        mScreenHeight = metrics.heightPixels/* - mNotificationBarHeight */;//

    }
}
