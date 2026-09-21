package com.example.suiri;
import org.json.*;import java.time.*;import java.util.*;
public class Version114Test {
 static int n;static void check(boolean b,String label){n++;if(!b)throw new AssertionError(label);}
 static JSONObject task(String id,String due)throws Exception{return new JSONObject().put("id",id).put("title",id).put("due",due);}
 static String ids(List<JSONObject> tasks){String s="";for(JSONObject t:tasks)s+=t.optString("id")+",";return s;}
 static String rows(List<WidgetService.Row> list){String s="";for(WidgetService.Row r:list)s+=r.title+"|"+r.id+";";return s;}
 public static void main(String[] args)throws Exception{
  LocalDate now=LocalDate.parse("2026-09-19");Instant instant=Instant.parse("2026-09-19T04:00:00Z");
  JSONObject undated=task("undated",""),late=task("late","2026-09-01"),today=task("today","2026-09-19"),future=task("future","2026-09-20"),done=task("done","").put("done",true),deleted=task("deleted","").put("deleted",true),canvas=task("canvas","2026-10-01").put("source","canvas").put("canvasDue","2026-09-20T00:00:00Z");
  JSONArray data=new JSONArray().put(canvas).put(undated).put(late).put(today).put(future).put(done).put(deleted);
  String daily=ids(Agenda.section(data,false,"today",now,now,false));check(daily.contains("undated,")&&daily.contains("late,")&&daily.contains("today,"),"today carries undated and overdue");check(!daily.contains("future,")&&!daily.contains("done,")&&!daily.contains("deleted,"),"future and completed filtered");
  String week=ids(Agenda.section(data,false,"week",now,now,false));check(week.contains("undated,")&&week.contains("late,")&&week.contains("future,"),"week carries pending");check(ids(Agenda.section(data,true,"week",now,now,false)).equals("canvas,"),"Canvas uses real deadline");check(Agenda.section(data,false,"week",now.plusWeeks(1),now,false).isEmpty(),"future week not polluted by carryover");
  Schedule.Event course=new Schedule.Event();course.title="course";course.location="room";course.start=now.atTime(13,0);course.end=now.atTime(14,0);List<Schedule.Event> courses=Arrays.asList(course);
  List<WidgetService.Row> first=WidgetService.rows(data,courses,"today",instant);String original=rows(first);check(original.indexOf("undated")<original.indexOf("course")&&original.indexOf("course")<original.indexOf("canvas"),"personal courses Canvas order");
  for(int i=0;i<10;i++){WidgetService.rows(data,courses,"week",instant);WidgetService.rows(data,courses,"month",instant);check(rows(WidgetService.rows(data,courses,"today",instant)).equals(original),"switch preserves rows");}
  check(rows(first).equals(original),"older snapshot unchanged");today.put("done",true);check(!rows(WidgetService.rows(data,courses,"today",instant)).contains("|today;"),"completed item removed");check(!rows(WidgetService.rows(data,courses,"today",instant.plusSeconds(7200))).contains("course"),"ended course removed");
  check(Addressing.normalize("主人，请看。御主，你好").equals("亲爱的master，请看。亲爱的master，你好"),"legacy address replaced");check(Addressing.normalize("故事的主人公与房屋主人").equals("故事的主人公与房屋主人"),"ordinary nouns preserved");check(LearnedMemory.visible("主人，好的 {\"emotion\":\"happy\"}").equals("亲爱的master，好的"),"structured answer display");
  System.out.println("PASS: "+n+" task visibility, widget snapshot and address checks");
 }
}
