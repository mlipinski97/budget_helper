package com.example.engineerdegreeapp.util;

import android.annotation.SuppressLint;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    @SuppressLint("SimpleDateFormat")
    public static SimpleDateFormat dd_mm_yyy_sdf = new SimpleDateFormat("dd-MM-yyyy");
    @SuppressLint("SimpleDateFormat")
    public static int parseMonthNameToMonthNumber(String monthName){
        Date date = null;
        try {
            date = new SimpleDateFormat("MMMM").parse(monthName);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH) + 1;
    }
    public static DecimalFormat twoDigitsFormat = new DecimalFormat("00");

    public static int getNumberOfDaysInMonth(int month, int year) {
        Calendar calendar = Calendar.getInstance();
        int date = 1;
        calendar.set(year, month + 1, date);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

}

