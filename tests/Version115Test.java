package com.example.suiri;
import java.time.*;import java.util.*;import org.json.*;
public class Version115Test {
 static int n;static void check(boolean b,String s){n++;if(!b)throw new AssertionError(s);}
 static JSONObject task(String id,LocalDate d)throws Exception{return new JSONObject().put("id",id).put("title",id).put("due",d.toString());}
 public static void main(String[] args)throws Exception{
  for(LocalDate now:Arrays.asList(LocalDate.parse("2026-09-19"),LocalDate.parse("2026-12-29"))){
   JSONObject first=task("first",now),last=task("last",now.plusDays(6)),outside=task("outside",now.plusDays(7)).put("important",true),late=task("late",now.minusDays(3)),canvas=task("canvas",now.plusDays(7)).put("source","canvas").put("canvasDue",now.plusDays(7)+"T00:00:00+08:00");JSONArray a=new JSONArray().put(first).put(last).put(outside).put(late).put(canvas);
   List<JSONObject> personal=Agenda.section(a,false,"week",now,now,false);check(personal.contains(first)&&personal.contains(last),"inclusive seven days across week/year");check(!personal.contains(outside),"eighth day important task excluded");check(personal.contains(late),"overdue retained");check(Agenda.section(a,true,"week",now,now,false).isEmpty(),"eighth day Canvas excluded");
   Schedule.Event e=new Schedule.Event();e.title="last-day-course";e.location="room";e.start=now.plusDays(6).atTime(8,0);e.end=e.start.plusHours(2);List<WidgetService.Row> rows=WidgetService.rows(a,Arrays.asList(e),"week",now.atStartOfDay(Schedule.ZONE).toInstant());check(rows.stream().anyMatch(r->r.title.contains("last-day-course")),"course on seventh day included");e.start=e.start.plusDays(1);e.end=e.end.plusDays(1);check(WidgetService.rows(a,Arrays.asList(e),"week",now.atStartOfDay(Schedule.ZONE).toInstant()).stream().noneMatch(r->r.title.contains("last-day-course")),"eighth day course excluded");
  }
  System.out.println("PASS: "+n+" rolling seven-day boundary checks");
 }
}
