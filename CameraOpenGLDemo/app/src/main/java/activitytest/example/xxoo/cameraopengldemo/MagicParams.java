package activitytest.example.xxoo.cameraopengldemo;

import android.content.Context;
import android.os.Environment;

/**
 * Created by xxoo on 2017/2/28.
 */

public class MagicParams {
    public static Context context;
//    public static MagicBaseView magicBaseView;
    public static String videoPath = Environment.getExternalStorageDirectory().getPath();
    public static String videoName = "test.mp4";
    public static int beautyLevel  = 5;
    public MagicParams(){

    }
}
