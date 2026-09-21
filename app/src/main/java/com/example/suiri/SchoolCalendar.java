package com.example.suiri;
import java.time.*;
import org.json.*;
public final class SchoolCalendar {
 public static final String SOURCE="https://caup.tongji.edu.cn/caupen/d0/86/c35047a381062/page.htm";
 public static final class Period {
  public final String name,key;public final LocalDate start,end;public final boolean gap;
  Period(String name,String start,String end,boolean gap){this.name=name;this.start=LocalDate.parse(start);this.end=LocalDate.parse(end);this.gap=gap;key=start+"/"+end;}
  public boolean contains(LocalDate d){return !d.isBefore(start)&&!d.isAfter(end);}
 }
 // Official teaching/examination end dates; gaps are the user's personal definition.
 public static Period current(LocalDate d){Period[] known={
  new Period("2025—2026 春季学期","2026-03-02","2026-07-03",false),
  new Period("暑期小学期（个人划分）","2026-07-04","2026-09-13",true),
  new Period("2026—2027 秋季学期","2026-09-14","2027-01-15",false),
  new Period("寒假小学期（个人划分）","2027-01-16","2027-02-21",true)};
  for(Period p:known)if(p.contains(d))return p;return null;
 }
 public static String matching(JSONArray terms,Period p){if(p==null||p.gap)return "";String answer="";for(int i=0;i<terms.length();i++){JSONObject t=terms.optJSONObject(i);String n=t.optString("name").replace(" ","");boolean year=n.contains(Integer.toString(p.start.getYear()));boolean season=p.start.getMonthValue()>7?(n.contains("秋")||n.contains("第一学期")||n.contains("第1学期")||n.contains("Fall")):(n.contains("春")||n.contains("第二学期")||n.contains("第2学期")||n.contains("Spring"));if(year&&season){if(!answer.isEmpty())return "";answer=t.optString("id");}}return answer;}
}
