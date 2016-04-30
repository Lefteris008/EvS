/*
 * Copyright (C) 2016 Lefteris Paraskevas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edmodule.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author  Lefteris Paraskevas
 * @version 2016.04.30_1827
 */
public class StringDateUtils {
    
    /**
     * Method to return a String date key, assembled in YYYYMMDD_HHMM. <br>
     * More formally, the key is constructed by a given date and is mapped
     * to the nearest minute refresh window ('window' variable). <br>
     * E.g. for 10-minute refresh window, the key is mapped to the nearest 10-minute. <br>
     * For 30-minute refresh window, the key is mapped to the nearest half-hour.
     * @param cal A Calendar instance.
     * @param date The date from which is key is going to be constructed.
     * @param window The refresh window.
     * @return A String with key in YYYYMMDD_HHMM form.
     */
    public final static String getDateKey(Calendar cal, Date date, int window) {
        int year;
        int month;
        int day;
        int hour;
        int minute;
        cal.setTime(date);
        year = cal.get(Calendar.YEAR); //Current year
        month = cal.get(Calendar.MONTH) + 1; //Zero-index based
        day = cal.get(Calendar.DAY_OF_MONTH); //Get the current day of month
        hour = cal.get(Calendar.HOUR_OF_DAY); //24h
        minute = cal.get(Calendar.MINUTE) / window; //Nearest window, starting from 0

        //Assemble the key in YYYYMMDD_HHMM form.
        String key = String.valueOf(year) 
                + (month < 10 ? "0" + String.valueOf(month) : String.valueOf(month)) 
                + (day < 10 ? "0" + String.valueOf(day) : String.valueOf(day))
                + "_" //Separate actual date from hour information
                + (hour < 10 ? "0" + String.valueOf(hour) : String.valueOf(hour))
                + String.valueOf(minute) + "0";
        return key;
    }
    
    /**
     * Get the year of the date key.
     * More formally it returns the first 4 digits of the String key, that
     * represent the year.
     * @param dateKey The String Date key.
     * @return An integer representing the year.
     */
    public final static int getYear(String dateKey) {
        return Integer.valueOf(dateKey.substring(0, 4));
    }
    
    /**
     * Get the month of the date key.
     * More formally it returns digits 5 and 6 (starting from 1), that represent
     * the month.
     * @param dateKey The String Date key.
     * @return An integer representing the month (starting from 1).
     */
    public final static int getMonth(String dateKey) {
        return Integer.valueOf(dateKey.substring(4, 6));
    }
    
    /**
     * Get the day of the month of the date key.
     * More formally it returns digits 7 and 8 (starting from 1), that represent
     * the day of the month.
     * @param dateKey The String Date key.
     * @return An integer representing the day.
     */
    public final static int getDayOfMonth(String dateKey) {
        return Integer.valueOf(dateKey.substring(6, 8));
    } 
    
    /**
     * Get the hour of the date key.
     * More formally it returns digits 10 and 11 (starting from 1), that represent
     * the hour of the day.
     * @param dateKey The String Date key.
     * @return An integer representing the hour (starting from 0).
     */
    public final static int getHourOfDay(String dateKey) {
        return Integer.valueOf(dateKey.substring(9, 11));
    }
    
    /**
     * Get the minute of the date key.
     * More formally it returns digits 12 and 13 (starting from 1), that represent
     * the minute of the hour.
     * @param dateKey The String Date key.
     * @return An integer representing the minute (starting from 0).
     */
    public final static int getMinuteOfHour(String dateKey) {
        return Integer.valueOf(dateKey.substring(11));
    }
    
    /**
     * Clears a Calendar object and sets it according to the String Date key.
     * @param c A Calendar object.
     * @param dateKey A String assembled in YYYYMMDD_HHMM fashion.
     */
    public final static void clearAndSetYearToMinute(Calendar c, String dateKey) {
        c.clear();
        c.set(StringDateUtils.getYear(dateKey),
            StringDateUtils.getMonth(dateKey) - 1, 
            StringDateUtils.getDayOfMonth(dateKey),
            StringDateUtils.getHourOfDay(dateKey),
            StringDateUtils.getMinuteOfHour(dateKey));
    }
    
    /**
     * Returns a complete Date object from a
     * @param date The date to be parsed in plain text.
     * @return A date object from the input String.
     */
    public final static Date getDateFromString(String date) {
        try {
            DateFormat format = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.ENGLISH);
            return format.parse(date);
        } catch(ParseException e) {
            System.err.println("Input String was malformed.");
            Logger.getLogger(StringDateUtils.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
    }
    
    public final static boolean matchDate(Date date) {
        Calendar cal = Calendar.getInstance();
        int year;
        int month;
        int day;
        cal.setTime(date);
        year = cal.get(Calendar.YEAR); //Current year
        month = cal.get(Calendar.MONTH) + 1; //Zero-index based
        day = cal.get(Calendar.DAY_OF_MONTH); //Get the current day of month
        return (year == 2012) && 
                (month == 11) && 
                (
                    (day == 6) || 
                    (day == 5)
                );
    }
}
