package com.example.suiri;
import java.time.*;
public final class CourseStatus {
 public static Instant start(Schedule.Event e,LocalDate day){return day.atTime(e.start.toLocalTime()).atZone(Schedule.ZONE).toInstant();}
 public static Instant end(Schedule.Event e,LocalDate day){return start(e,day).plus(Duration.between(e.start,e.end));}
 public static String label(Schedule.Event e,LocalDate day,Instant now){if(now.isBefore(start(e,day)))return "未开始";if(now.isBefore(end(e,day)))return "上课中";return "已下课";}
}
