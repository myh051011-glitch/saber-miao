package com.example.suiri;
import java.time.*;
import org.json.*;
public class Version11Test {
 static int n;static void check(boolean v){n++;if(!v)throw new AssertionError("1.1 check "+n);}
 public static void main(String[] args)throws Exception{
  Schedule.Event e=new Schedule.Event();e.start=LocalDateTime.parse("2026-09-14T08:00");e.end=LocalDateTime.parse("2026-09-14T09:50");LocalDate d=LocalDate.parse("2026-09-21");
  check(CourseStatus.label(e,d,Instant.parse("2026-09-20T23:59:59Z")).equals("未开始"));check(CourseStatus.label(e,d,Instant.parse("2026-09-21T00:00:00Z")).equals("上课中"));check(CourseStatus.label(e,d,Instant.parse("2026-09-21T01:50:00Z")).equals("已下课"));
  e.start=LocalDateTime.parse("2026-09-14T23:00");e.end=LocalDateTime.parse("2026-09-15T01:00");check(CourseStatus.label(e,d,Instant.parse("2026-09-21T16:30:00Z")).equals("上课中"));
  LocalDate start=LocalDate.parse("2026-09-14");check(TimetableUi.week(LocalDate.parse("2026-09-18"),start)==1);check(TimetableUi.week(LocalDate.parse("2026-09-20"),start)==1);check(TimetableUi.week(LocalDate.parse("2026-09-21"),start)==2);check(TimetableUi.week(LocalDate.parse("2027-01-04"),start)==17);
  JSONObject daily=new JSONObject("{\"days\":[{\"forecastStartTime\":\"2026-09-18T07:00+08:00\",\"daytime\":{\"condition\":{\"text\":\"晴\"}},\"nighttime\":{\"condition\":{\"text\":\"多云\"},\"precipitation\":{\"probability\":0.7},\"windGustMax\":{\"value\":15}},\"temperatureMax\":{\"value\":36}}]}");LocalDate today=LocalDate.parse("2026-09-18");String advice=WeatherRules.advice(new JSONObject(),daily,false,today);check(advice.contains("雨伞"));check(advice.contains("夜间多云"));check(advice.contains("大风"));check(advice.contains("补水"));check(!WeatherRules.advice(new JSONObject(),daily,false,today.plusDays(1)).contains("雨伞"));check(WeatherRules.advice(new JSONObject(),new JSONObject(),false,today).contains("尚未更新"));
  check(Agenda.monthMark(new JSONArray(),java.util.Collections.emptyList(),today,today).isEmpty());
  e.start=LocalDateTime.parse("2026-09-16T08:00");e.end=e.start.plusHours(2);e.weekly=true;e.interval=2;e.count=3;java.util.List<Schedule.Event> all=java.util.Arrays.asList(e);
  check(TimetableModel.base(all).equals(start));check(TimetableModel.events(all,3,start,0).size()==1);check(TimetableModel.events(all,3,start,2).isEmpty());check(TimetableModel.events(all,3,start,3).size()==1);check(TimetableModel.weeks(e,start).equals("第1—5单周"));check(TimetableModel.events(all,4,start,0).isEmpty());
  System.out.println("PASS: "+n+" version 1.1 status, term week, overnight rain and forecast checks");
 }
}
