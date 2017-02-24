package activitytest.example.xxoo.cameraopengldemo;

import android.graphics.Point;

import java.nio.FloatBuffer;

/**
 * Created by xxoo on 2017/2/24.
 */

public class CGRect {
    public float x;
    public float y;
    public float width;
    public float height;

    public CGRect() {

    }

    public CGRect(Float x, Float y,Float width,Float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Set the point's x and y coordinates
     */
    public void set(Float x, Float y,Float width,Float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
