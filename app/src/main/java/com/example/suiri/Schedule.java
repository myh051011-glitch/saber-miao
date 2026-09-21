package com.example.suiri;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Small strict ICS reader: supports the supplied WakeUp weekly export. */
public final class Schedule {
 public static final ZoneId ZONE=ZoneId.of("Asia/Shanghai");
 static final DateTimeFormatter FORMAT=DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
 public static final class Event {
  public String title="",location="",uid="";
  public LocalDateTime start,end; public Instant until; public int interval=1,count=Integer.MAX_VALUE; public boolean weekly;
  public boolean on(LocalDate date) {
   long days=java.time.temporal.ChronoUnit.DAYS.between(start.toLocalDate(),date);
   if(days<0)return false;
   if(!weekly)return days==0;
   if(days%(7L*interval)!=0 || days/(7L*interval)>=count)return false;
   return until==null || !date.atTime(start.toLocalTime()).atZone(ZONE).toInstant().isAfter(until);
  }
 }
 public static List<Event> parse(String input) {
  if(!input.contains("BEGIN:VCALENDAR"))throw new IllegalArgumentException("请选择 ICS 日历文件");
  List<Event> events=new ArrayList<>();Set<String> ids=new HashSet<>();Event e=null;boolean alarm=false;
  for(String line:input.replaceAll("\\r?\\n[ \\t]", "").split("\\r?\\n")) {
   if(line.equals("BEGIN:VEVENT")){e=new Event();continue;}
   if(line.equals("BEGIN:VALARM")){alarm=true;continue;}
   if(line.equals("END:VALARM")){alarm=false;continue;}
   if(line.equals("END:VEVENT")) {
    if(e==null||e.start==null||e.end==null||!e.end.isAfter(e.start)||e.uid.isEmpty())throw new IllegalArgumentException("课程缺少有效时间或 UID");
    if(ids.add(e.uid))events.add(e);e=null;continue;
   }
   if(e==null||alarm)continue;
   int colon=line.indexOf(':');if(colon<0)continue;String key=line.substring(0,colon),v=line.substring(colon+1),name=key.split(";")[0];
   switch(name){
    case "SUMMARY":e.title=unescape(v);break;case "LOCATION":e.location=unescape(v);break;case "UID":e.uid=v;break;
    case "DTSTART":e.start=time(key,v);break;case "DTEND":e.end=time(key,v);break;
    case "RRULE":
     Map<String,String> r=new HashMap<>();for(String part:v.split(";")){String[] p=part.split("=",2);if(p.length!=2)throw new IllegalArgumentException("重复规则不完整");r.put(p[0],p[1]);}
     if(!"WEEKLY".equals(r.get("FREQ")))throw new IllegalArgumentException("此版本仅支持每周重复的课表");
     for(String k:r.keySet())if(!Arrays.asList("FREQ","INTERVAL","UNTIL","COUNT").contains(k))throw new IllegalArgumentException("暂不支持重复规则："+k);
     e.weekly=true;e.interval=Integer.parseInt(r.getOrDefault("INTERVAL","1"));e.count=Integer.parseInt(r.getOrDefault("COUNT","2147483647"));
     if(e.interval<1||e.count<1)throw new IllegalArgumentException("重复次数或间隔无效");
     if(r.containsKey("UNTIL"))e.until=time("",r.get("UNTIL")).atZone(ZONE).toInstant();break;
    case "EXDATE":case "RDATE":case "RECURRENCE-ID":throw new IllegalArgumentException("此版本暂不支持例外日期，请保留原课表核对");
   }
  }
  if(e!=null||events.isEmpty())throw new IllegalArgumentException("未找到完整课程");return events;
 }
 static LocalDateTime time(String key,String v){
  if(key.contains("VALUE=DATE"))throw new IllegalArgumentException("暂不支持全天课程");
  if(v.endsWith("Z"))return LocalDateTime.parse(v.substring(0,v.length()-1),FORMAT).toInstant(ZoneOffset.UTC).atZone(ZONE).toLocalDateTime();
  if(key.contains("TZID=")&&!key.contains("TZID=Asia/Shanghai"))throw new IllegalArgumentException("此版本课表时区需为 Asia/Shanghai");
  return LocalDateTime.parse(v,FORMAT);
 }
 static String unescape(String v){return v.replace("\\n","\n").replace("\\N","\n").replace("\\,",",").replace("\\;",";").replace("\\\\","\\");}
}
