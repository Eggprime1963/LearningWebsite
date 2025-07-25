package model;

import java.time.LocalDate;

public class ActivityDate {
    private int day;
    private int month;
    private int year;
    private String monthName;
    private String dateKey;
    private int dayOfWeek; // 0=Sun, 1=Mon, ..., 6=Sat
    private LocalDate date;

    public ActivityDate(int day, int month, int year, String monthName, String dateKey) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.monthName = monthName;
        this.dateKey = dateKey;
    }

    public ActivityDate(int day, int month, int year, String monthName, String dateKey, int dayOfWeek, LocalDate date) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.monthName = monthName;
        this.dateKey = dateKey;
        this.dayOfWeek = dayOfWeek;
        this.date = date;
    }

    // getters and setters
    public int getDay() { return day; }
    public void setDay(int day) { this.day = day; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getMonthName() { return monthName; }
    public void setMonthName(String monthName) { this.monthName = monthName; }

    public String getDateKey() { return dateKey; }
    public void setDateKey(String dateKey) { this.dateKey = dateKey; }

    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}