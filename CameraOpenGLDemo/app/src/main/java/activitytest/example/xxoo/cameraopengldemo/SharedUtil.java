package activitytest.example.xxoo.cameraopengldemo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by xxoo on 2017/2/15.
 */

public class SharedUtil {
    private Context ctx;
    private String FileName = "megvii";
    public SharedUtil(Context ctx) {
        this.ctx = ctx;
    }
    public void saveStringValue(String key, String value) {
        SharedPreferences sharePre = ctx.getSharedPreferences(FileName,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharePre.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public String getStringValueByKey(String key) {
        SharedPreferences sharePre = ctx.getSharedPreferences(FileName,
                Context.MODE_PRIVATE);
        return sharePre.getString(key, null);
    }
}
