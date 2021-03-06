package android.course.books_312316433_313601130.Network;

import android.graphics.Bitmap;

import org.json.JSONObject;

/**
 * NetworkResListener interface
 */
public interface NetworkResListener {
    /**
     * callback method which called when the resources update is started
     */
    public void onPreUpdate();

    public void onPostUpdate(JSONObject res, String table, ResStatus status);

    public void onPostUpdate(Bitmap res, ResStatus status);
}
