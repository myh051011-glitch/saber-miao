package com.example.suiri;
import java.time.*;
import org.json.*;
public class FeatureTest {
 static int checks=0;
 static void check(boolean value,String reason){checks++;if(!value)throw new AssertionError(reason);}
 static JSONObject task(String id,String day)throws Exception{return new JSONObject().put("id",id).put("title","作业").put("due",day).put("plannedTime","18:00").put("canvasDue",day+"T10:00:00Z").put("source","canvas").put("minutes",60);}
 public static void main(String[] args)throws Exception{
  LocalDate now=LocalDate.of(2026,9,16);
  check(TaskDates.weekStart(now).equals(LocalDate.of(2026,9,14)),"Monday start");
  check(TaskDates.visible("2026-09-20","week",now),"Sunday included");
  check(!TaskDates.visible("2026-09-21","week",now),"next Monday excluded");
  check(TaskDates.visible("2026-09-01","month",now),"month first day");
  check(!TaskDates.visible("2026-10-01","month",now),"month boundary");
  check(TaskDates.visible("2024-02-29","month",LocalDate.of(2024,2,10)),"leap day");
  check(TaskDates.weekStart(LocalDate.of(2027,1,1)).equals(LocalDate.of(2026,12,28)),"cross year week");
  check(TaskDates.visible("","today",now)&&!TaskDates.visible("","week",now),"undated only today");
  check(TaskDates.visible("2026-09-15","today",now)&&!TaskDates.visible("2026-09-17","today",now),"overdue included future excluded");
  long at=TaskDates.reminderMillis("2026-09-16","09:00");
  check(at==Instant.parse("2026-09-16T01:00:00Z").toEpochMilli(),"Shanghai timezone");
  check(TaskDates.nextAlarm(false,true,at,0,at-100)==at,"future reminder");
  check(TaskDates.nextAlarm(true,true,at,0,at-100)==0,"done cancels reminder");
  check(TaskDates.nextAlarm(false,false,at,0,at-100)==0,"disabled reminder");
  check(TaskDates.nextAlarm(false,true,at,at,at+100)==0,"already delivered no repeat");
  check(TaskDates.nextAlarm(false,true,at,0,at+100)==at+5100,"missed reminder recovery");
  check(TaskDates.nextAlarm(false,true,at,0,at+86400001)==0,"old reminders not replayed");
  JSONObject old=task("canvas:1:2","2026-09-17").put("done",true).put("minutes",95).put("reminder",true).put("reminderDate","2026-09-17").put("reminderTime","09:00");
  JSONObject fresh=task("canvas:1:2","2026-09-18").put("submitted",true);
  JSONArray result=CanvasMerge.merge(new JSONArray().put(old),new JSONArray().put(fresh));JSONObject merged=result.getJSONObject(0);
  check(result.length()==1&&merged.optBoolean("done"),"no duplicate / completion preserved");
  check(merged.optInt("minutes")==95,"manual estimate preserved");
  check(merged.optString("due").equals("2026-09-18"),"teacher deadline synchronized");
  check(!merged.optBoolean("reminder")&&merged.optBoolean("deadlineChanged"),"changed deadline invalidates reminder");
  check(old.optString("due").equals("2026-09-17"),"merge does not mutate saved source");
  old.put("dateCustomized",true);result=CanvasMerge.merge(new JSONArray().put(old),new JSONArray().put(fresh));check(result.getJSONObject(0).optString("due").equals("2026-09-17"),"personal planned date preserved");
  result=CanvasMerge.merge(new JSONArray(),new JSONArray().put(fresh));check(result.length()==0,"new submitted assignment not added as todo");
  result=CanvasMerge.merge(new JSONArray().put(old),new JSONArray());check(result.length()==1&&result.getJSONObject(0).optBoolean("sourceMissing"),"missing source retained for review");
  result=CanvasMerge.merge(new JSONArray().put(old.put("done",false)),new JSONArray().put(fresh));check(!result.getJSONObject(0).optBoolean("done")&&result.getJSONObject(0).optBoolean("submitted"),"submission independent from local completion");
  for(String url:new String[]{"http://canvas.tongji.edu.cn/api/v1/courses","https://evil.example/","https://canvas.tongji.edu.cn.evil.example/","https://api.deepseek.com:444/","https://bad@api.deepseek.com/"}){boolean rejected=false;try{Api.request(url,"TEST_ONLY",null);}catch(java.io.IOException e){rejected=true;}check(rejected,"credential endpoint guard "+url);}
  JSONObject draft=TaskProposal.parse("建议如下\n```json\n{ \"task\" : {\"title\":\"报告\",\"due\":\"2026-09-20\",\"minutes\":45,\"reminder\":true}}\n```");
  check(draft.optString("title").equals("报告")&&draft.optInt("minutes")==45,"AI draft whitespace parsing");
  check(!draft.has("reminder"),"AI cannot silently enable reminders");
  check(TaskProposal.parse("{\"task\":{\"title\":\"bad\",\"due\":\"2026-99-99\"}}").length()==0,"AI invalid date rejected");
  check(TaskProposal.parse("普通回答").length()==0,"plain response does not create task");
  System.out.println("PASS: "+checks+" date, reminder, Canvas merge, endpoint and AI draft checks");
 }
}
