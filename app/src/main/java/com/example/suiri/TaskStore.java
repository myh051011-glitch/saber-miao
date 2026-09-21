package com.example.suiri;
import android.content.*;
import org.json.*;
import java.util.*;

public final class TaskStore {
 public static android.content.SharedPreferences prefs(Context c){return c.getSharedPreferences("suiri",Context.MODE_PRIVATE);}
 public static JSONArray load(Context c){try{return new JSONArray(prefs(c).getString("tasks","[]"));}catch(Exception e){return new JSONArray();}}
 public static JSONObject find(JSONArray a,String id){for(int i=0;i<a.length();i++){JSONObject t=a.optJSONObject(i);if(t!=null&&t.optString("id").equals(id))return t;}return null;}
 public static long at(JSONObject t){return TaskDates.reminderMillis(t.optString("reminderDate"),t.optString("reminderTime"));}
 public static synchronized void save(Context c,JSONArray data){
  JSONArray old=load(c);
  if(!prefs(c).edit().putString("tasks",data.toString()).commit())throw new IllegalStateException("无法保存待办");
  for(int i=0;i<old.length();i++){JSONObject before=old.optJSONObject(i);if(before==null)continue;JSONObject after=find(data,before.optString("id"));
   if(after==null||after.optBoolean("archived")||after.optBoolean("deleted")||after.optBoolean("done")||at(before)!=at(after)||!after.optBoolean("reminder"))Reminders.cancel(c,before.optString("id"),true);
  }
  Reminders.reschedule(c);SuiRiWidget.refresh(c);
 }
 public static synchronized void patch(Context c,String id,JSONObject changes){JSONArray data=load(c);JSONObject t=find(data,id);if(t==null)throw new IllegalStateException("事项已删除，请刷新");try{if(changes.has("done")&&changes.optBoolean("done")!=t.optBoolean("done"))t.put("completedAt",changes.optBoolean("done")?System.currentTimeMillis():0);for(Iterator<String> it=changes.keys();it.hasNext();){String k=it.next();t.put(k,changes.get(k));}}catch(JSONException e){throw new IllegalStateException(e);}save(c,data);}
 public static synchronized void add(Context c,JSONObject t){JSONArray data=load(c);if(find(data,t.optString("id"))!=null)throw new IllegalStateException("事项重复");data.put(t);save(c,data);}
 public static synchronized void delete(Context c,String id){JSONArray a=load(c),b=new JSONArray();for(int i=0;i<a.length();i++)if(!a.optJSONObject(i).optString("id").equals(id))b.put(a.optJSONObject(i));save(c,b);}
 public static List<JSONObject> sorted(JSONArray data){List<JSONObject> list=new ArrayList<>();for(int i=0;i<data.length();i++)if(data.optJSONObject(i)!=null&&!data.optJSONObject(i).optBoolean("archived")&&!data.optJSONObject(i).optBoolean("deleted"))list.add(data.optJSONObject(i));
  list.sort(Comparator.comparing((JSONObject t)->t.optBoolean("done")).thenComparing(t->t.optString("due").isEmpty()?"9999":t.optString("due")).thenComparing(t->t.optString("plannedTime")).thenComparing(t->t.optString("title")));return list;
 }
}
