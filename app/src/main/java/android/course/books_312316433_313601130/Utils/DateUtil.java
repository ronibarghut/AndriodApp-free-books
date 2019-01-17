package android.course.books_312316433_313601130.Utils;


import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * class used for date conversion operations
 */
public class DateUtil {

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static DateFormat timeFormat = new SimpleDateFormat("K:mma");

    public static String getCurrentTime() {
        Date today = Calendar.getInstance().getTime();
        return timeFormat.format(today);
    }

    public static String getCurrentDate(long date) {
        Date today = new Date(date);
        return dateFormat.format(today);
    }

    public static String formatDateTime(long dateFormat) {
        Date today = new Date(dateFormat);
        return timeFormat.format(today);

    }

    public static long getCurrentDate(String date) {
        try {
            Date today = dateFormat.parse(date);
            return today.getTime();
        } catch (ParseException e) {
            Log.e("Exception : ", e.getMessage());
            return -1;
        }

    }
}
