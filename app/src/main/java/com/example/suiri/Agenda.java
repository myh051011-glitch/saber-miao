package com.example.suiri;
import org.json.*;
import java.time.*;
import java.util.*;

/** Shared ordering for the app, month marks, widget and deadline notification. */
public final class Agenda {
 public static boolean canvas(JSONObject t){return "canvas".equals(t.optString("source"));}
 public static LocalDate deadline(JSONObject t){try{return OffsetDateTime.parse(t.optString("canvasDue")).atZoneSameInstant(Schedule.ZONE).toLocalDate();}catch(Exception e){return null;}}
 public static boolean focus(JSONObject t,LocalDate now){
  if(t.optBoolean("done")||t.optBoolean("deleted")||t.optBoolean("archived")||t.optBoolean("sourceMissing"))return false;
  if(canvas(t)){LocalDate d=deadline(t);return !t.optBoolean("submitted")&&d!=null&&!d.isAfter(now.plusDays(7));}
  return t.optBoolean("important");
 }
 public static int rank(JSONObject t,LocalDate now){return focus(t,now)?0:canvas(t)?3:2;}
 public static List<JSONObject> ordered(JSONArray data,LocalDate now){List<JSONObject> list=TaskStore.sorted(data);list.sort(Comparator.comparing((JSONObject t)->t.optBoolean("done")).thenComparingInt(t->rank(t,now)).thenComparing(t->{LocalDate d=canvas(t)?deadline(t):null;return d==null?t.optString("due","9999"):d.toString();}));return list;}
 public static boolean visible(JSONObject t,String scope,LocalDate now){return !t.optBoolean("archived")&&(focus(t,now)||TaskDates.visible(t.optString("due"),scope,now));}
 public static List<JSONObject> section(JSONArray data,boolean canvas,String scope,LocalDate view,LocalDate now,boolean includeDone){
  List<JSONObject> result=new ArrayList<>();LocalDate start=view,end=scope.equals("week")?start.plusDays(6):view;
  boolean current=scope.equals("today")||start.equals(now);
  for(JSONObject t:ordered(data,now)){
   if(canvas(t)!=canvas||(!includeDone&&t.optBoolean("done")))continue;
   LocalDate deadline=canvas?deadline(t):null;String due=deadline==null?t.optString("due"):deadline.toString();
   boolean show=scope.equals("today")?TaskDates.visible(due,"today",view):TaskDates.inRange(due,start,end);
   if(current&&(due.isEmpty()||(!t.optBoolean("done")&&TaskDates.visible(due,"today",now))||(scope.equals("today")&&focus(t,now))))show=true;
   if(show)result.add(t);
  }return result;
 }
 public static String focusLabel(JSONObject t,LocalDate now){LocalDate d=deadline(t);if(!canvas(t)||d==null)return "重点提醒";long days=java.time.temporal.ChronoUnit.DAYS.between(now,d);return days<0?"作业已逾期 "+(-days)+" 天":days==0?"作业今天截止":"作业 "+days+" 天后截止";}
 public static String monthMark(JSONArray tasks,List<Schedule.Event> courses,LocalDate day,LocalDate now){int personal=0,canvas=0,classes=0,focus=0;for(JSONObject t:TaskStore.sorted(tasks)){if(t.optBoolean("done"))continue;LocalDate due=canvas(t)?deadline(t):null;boolean here=(due!=null?due.toString():t.optString("due")).equals(day.toString());if(!here)continue;if(focus(t,now))focus++;if(canvas(t))canvas++;else personal++;}for(Schedule.Event e:courses)if(e.on(day))classes++;String s="";if(focus>0)s+="!"+focus+" ";if(personal>0)s+="事"+personal+" ";if(canvas>0)s+="作"+canvas+" ";if(classes>0)s+="课"+classes;return s.trim();}
}
