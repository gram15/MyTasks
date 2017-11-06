package com.gram15.am.mytasks;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by AMantovan on 19/08/2017.
 */

public class TaskUtils {


    public class Constants{
        public final static int DEFAULT_HOUR_OF_REMINDER = 12;
        public final static int PRIORITY_HIGH = 1;
        public final static int PRIORITY_MEDIUM= 2;
        public final static int PRIORITY_lOW = 3;
    }
    /**
     * This method compares date. Return true if current date is grater than param date, otherwase false
     *
     * @param date
     * @return
     */
    public static boolean isDateOverdue(long date) {

        Calendar c = Calendar.getInstance();
        long dateNow = c.getTimeInMillis();

        if (dateNow > date) {
            // if currentDate > overduedate return true
            return true;
        } else {
            // otherwise
            return false;
        }
    }

    public static long addHourToDate(int year, int month, int day, int hour) {
        Calendar calendar = new GregorianCalendar(year,month,day);
        calendar.add(Calendar.HOUR, hour); // adds hour
        long dateLong = calendar.getTimeInMillis();
        return dateLong;
    }
}

