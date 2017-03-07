package activitytest.example.xxoo.cameraopengldemo.help;

import android.graphics.RectF;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * Created by xxoo on 2017/3/7.
 */

public class OpenGLDrawRect {

    public static void rotatePoint3f(float points[], int offset, float angle/*radis*/, int x_axis, int y_axis) {
        float x = points[offset + x_axis], y = points[offset + y_axis];
        float alpha_x = (float)Math.cos(angle), alpha_y = (float)Math.sin(angle);

        points[offset + x_axis] = x * alpha_x - y * alpha_y;
        points[offset + y_axis] = x * alpha_y + y * alpha_x;
    }

    public static ArrayList<FloatBuffer> drawCenterShowRect(boolean isBackCamera, float width, float height, float roi_ratio) {
        RectF rectF = new RectF();

        //roi_ratio 相当于屏幕的比例 0.8倍
        float showRectHeight = height * roi_ratio;
        float _x_offset = 0, _y_offset = 0;
        float max_len = height;
        if (width > height) {
            max_len = width;
            _y_offset = (width - height) / 2;
        } else
            _x_offset = (height - width) / 2;
        // 把框固定在中间以最短边的0.8倍大小
        rectF.left = ((width - showRectHeight) / 2.0f + _x_offset) / max_len;
        rectF.top = ((height - showRectHeight) / 2.0f + _y_offset) / max_len;
        rectF.right = ((width - showRectHeight) / 2.0f + showRectHeight + _x_offset) / max_len;
        rectF.bottom = ((height - showRectHeight) / 2.0f + showRectHeight + _y_offset) / max_len;

        Log.w("ceshi", "rectF===" + rectF);

        float _left = rectF.left * 2 - 1;
        float _right = rectF.right * 2 - 1;
        float _top = 1 - rectF.top * 2;
        float _bottom = 1 - rectF.bottom * 2;
        if (isBackCamera) {
            _left = -_left;
            _right = -_right;
        }
        float delta_x = 3 / height, delta_y = 3 / height;
        // 4个点分别是// top_left bottom_left bottom_right
        // top_right
        float rectangle_left[] = { _left, _top, 0, _left, _bottom, 0, _left + delta_x, _bottom, 0, _left + delta_x,
                _top, 0 };
        float rectangle_top[] = { _left, _top, 0, _left, _top - delta_y, 0, _right, _top - delta_y, 0, _right, _top,
                0 };
        float rectangle_right[] = { _right - delta_x, _top, 0, _right - delta_x, _bottom, 0, _right, _bottom, 0, _right,
                _top, 0 };
        float rectangle_bottom[] = { _left, _bottom + delta_y, 0, _left, _bottom, 0, _right, _bottom, 0, _right,
                _bottom + delta_y, 0 };

        FloatBuffer fb_left = floatBufferUtil(rectangle_left);
        FloatBuffer fb_top = floatBufferUtil(rectangle_top);
        FloatBuffer fb_right = floatBufferUtil(rectangle_right);
        FloatBuffer fb_bottom = floatBufferUtil(rectangle_bottom);
        ArrayList<FloatBuffer> vertexBuffersOpengl = new ArrayList<FloatBuffer>();
        vertexBuffersOpengl.add(fb_left);
        vertexBuffersOpengl.add(fb_top);
        vertexBuffersOpengl.add(fb_right);
        vertexBuffersOpengl.add(fb_bottom);
        return vertexBuffersOpengl;
    }

    // 定义一个工具方法，将float[]数组转换为OpenGL ES所需的FloatBuffer
    public static FloatBuffer floatBufferUtil(float[] arr) {
        // 初始化ByteBuffer，长度为arr数组的长度*4，因为一个int占4个字节
        ByteBuffer qbb = ByteBuffer.allocateDirect(arr.length * 4);
        // 数组排列用nativeOrder
        qbb.order(ByteOrder.nativeOrder());
        FloatBuffer mBuffer = qbb.asFloatBuffer();
        mBuffer.put(arr);
        mBuffer.position(0);
        return mBuffer;
    }
}
