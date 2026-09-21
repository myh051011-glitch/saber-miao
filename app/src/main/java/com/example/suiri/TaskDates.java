package com.example.suiri;

import java.time.*;
import java.time.temporal.TemporalAdjusters;

/** Pure date rules shared by the app, reminders and widget. */
public final class TaskDates {
 public static LocalDate weekStart(LocalDate day){return day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));}
 public static boolean inRange(String due,LocalDate start,LocalDate end){
  try{LocalDate d=LocalDate.parse(due);return !d.isBefore(start)&&!d.isAfter(end);}catch(Exception e){return false;}
 }
 public static boolean visible(String due,String scope,LocalDate today){
  if(scope.equals("week"))return inRange(due,weekStart(today),weekStart(today).plusDays(6));
  if(scope.equals("month"))return inRange(due,today.withDayOfMonth(1),today.withDayOfMonth(today.lengthOfMonth()));
  if(due.isEmpty())return true;
  try{return !LocalDate.parse(due).isAfter(today);}catch(Exception e){return false;}
 }
 public static long reminderMillis(String date,String time){
  try{return LocalDate.parse(date).atTime(LocalTime.parse(time)).atZone(Schedule.ZONE).toInstant().toEpochMilli();}catch(Exception e){return 0;}
 }
 public static long nextAlarm(boolean done,boolean enabled,long at,long fired,long now){
  if(done||!enabled||at<=0||fired==at)return 0;
  if(at>now)return at;
  return now-at<=86400000L?now+5000:0;
 }
}
