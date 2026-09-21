package com.example.suiri;
import org.json.*;
import java.time.*;
import java.util.*;
public final class TaskActions {
 public static final class Result {public JSONArray tasks;public final List<String> receipts=new ArrayList<>();}
 public static Result apply(JSONArray old,JSONArray actions,LocalDateTime now)throws JSONException{
  if(actions.length()>8)throw new JSONException("一次最多修改 8 件事项，请拆分请求");Result result=new Result();result.tasks=new JSONArray(old.toString());
  for(int i=0;i<actions.length();i++){JSONObject a=actions.getJSONObject(i);String op=a.getString("operation");if(!Arrays.asList("create","update","complete","delete").contains(op))throw new JSONException("不支持该任务操作");String id=a.optString("id");JSONObject t;
   if(op.equals("create")){t=new JSONObject().put("id",UUID.randomUUID().toString()).put("done",false).put("source","personal").put("minutes",30);result.tasks.put(t);}else{t=TaskStore.find(result.tasks,id);if(t==null||t.optBoolean("archived")||t.optBoolean("deleted"))throw new JSONException("找不到要修改的当前事项，请说明名称");}
   String before=t.optString("title");if(op.equals("delete")&&Agenda.canvas(t)){t.put("deleted",true).put("reminder",false);result.receipts.add("已从待办移除："+before+"（不会删除学校作业）");continue;}if(op.equals("delete")){JSONArray next=new JSONArray();for(int j=0;j<result.tasks.length();j++)if(!result.tasks.getJSONObject(j).optString("id").equals(id))next.put(result.tasks.getJSONObject(j));result.tasks=next;result.receipts.add("已删除："+before);continue;}
   if(op.equals("complete")){if(!t.optBoolean("done"))t.put("done",true).put("completedAt",now.atZone(Schedule.ZONE).toInstant().toEpochMilli());result.receipts.add("已完成："+before);continue;}
   JSONObject fields=a.optJSONObject("fields");if(fields==null)throw new JSONException("缺少事项内容");for(Iterator<String> it=fields.keys();it.hasNext();){String k=it.next();if(!Arrays.asList("title","due","plannedTime","minutes","notes","important","reminder","reminderDate","reminderTime").contains(k))throw new JSONException("不允许修改字段："+k);}
   if(op.equals("create")&&!fields.has("title"))throw new JSONException("请说明事项名称");String title=fields.optString("title",t.optString("title"));if(title.trim().isEmpty()||title.length()>200)throw new JSONException("事项名称需为 1–200 字");
   for(String k:Arrays.asList("due","reminderDate"))if(fields.has(k)&&!fields.optString(k).isEmpty())try{LocalDate.parse(fields.getString(k));}catch(Exception e){throw new JSONException("日期无效，请明确年月日");}
   for(String k:Arrays.asList("plannedTime","reminderTime"))if(fields.has(k)&&!fields.optString(k).isEmpty())try{LocalTime time=LocalTime.parse(fields.getString(k));if(time.getSecond()!=0)throw new Exception();}catch(Exception e){throw new JSONException("时间无效，请明确几点几分");}
   if(fields.has("minutes")&&(fields.optInt("minutes",0)<1||fields.optInt("minutes")>1440))throw new JSONException("耗时需为 1–1440 分钟");if(fields.optString("notes").length()>5000)throw new JSONException("备注超过 5000 字");boolean moved=fields.has("due")||fields.has("plannedTime");for(Iterator<String> it=fields.keys();it.hasNext();){String k=it.next();t.put(k,fields.get(k));}
   if(moved&&Agenda.canvas(t))t.put("dateCustomized",true);if(moved&&t.optBoolean("reminder")&&!fields.has("reminderDate")&&!fields.has("reminderTime")){t.put("reminderDate",t.optString("due")).put("reminderTime",t.optString("plannedTime"));}
   if(t.optBoolean("reminder")&&(moved||fields.has("reminder")||fields.has("reminderDate")||fields.has("reminderTime"))){long at=TaskStore.at(t);if(at<=now.atZone(Schedule.ZONE).toInstant().toEpochMilli())throw new JSONException("提醒时间需在未来，请补充日期和时分");t.remove("reminderFired");}
   result.receipts.add((op.equals("create")?"已添加到本地日程：":"已修改：")+title+" "+t.optString("due")+" "+t.optString("plannedTime")+(t.optBoolean("reminder")?" · 已设置saber喵提醒":""));
  }return result;
 }
}
