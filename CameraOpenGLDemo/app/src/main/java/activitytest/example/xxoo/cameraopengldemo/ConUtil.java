package activitytest.example.xxoo.cameraopengldemo;

import android.content.Context;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Created by xxoo on 2017/2/15.
 */

public class ConUtil {
    public static byte[] getFileContent(Context context, int id) {
        InputStream inputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count = -1;
        try {
            inputStream = context.getResources().openRawResource(id);
            while ((count = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, count);
            }
            byteArrayOutputStream.close();
        } catch (IOException e) {
            return null;
        } finally {
            // closeStreamSilently(inputStream);
            inputStream = null;
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static String getUUIDString(Context mContext) {
        String KEY_UUID = "key_uuid";
        SharedUtil sharedUtil = new SharedUtil(mContext);
        String uuid = sharedUtil.getStringValueByKey(KEY_UUID);
        if (uuid != null && uuid.trim().length() != 0)
            return uuid;

        uuid = UUID.randomUUID().toString();
        uuid = Base64.encodeToString(uuid.getBytes(),
                Base64.DEFAULT);

        sharedUtil.saveStringValue(KEY_UUID, uuid);
        return uuid;
    }
}
