package com.example.suiri;
import org.json.*;
import java.util.*;
public final class CanvasMerge {
 public static JSONArray merge(JSONArray old,JSONArray fresh)throws JSONException{
  JSONArray result=new JSONArray(old.toString());Set<String> present=new HashSet<>();
  for(int i=0;i<fresh.length();i++){JSONObject incoming=fresh.getJSONObject(i);String id=incoming.getString("id");present.add(id);JSONObject t=TaskStore.find(result,id);
   if(t==null){if(incoming.optBoolean("submitted"))continue;result.put(new JSONObject(incoming.toString()));continue;}
   boolean dateChanged=!t.optString("canvasDue").equals(incoming.optString("canvasDue"));
   for(String field:Arrays.asList("title","source","course","url","canvasDue","submitted","submissionState","description","termId","periodKey"))t.put(field,incoming.opt(field));
   if(!t.optBoolean("dateCustomized")){t.put("due",incoming.optString("due"));t.put("plannedTime",incoming.optString("plannedTime"));}
   // Never silently move an already enabled reminder after a teacher changes the deadline.
   if(dateChanged&&t.optBoolean("reminder")){t.put("reminder",false);t.put("deadlineChanged",true);}t.put("sourceMissing",false);
  }
  for(int i=0;i<result.length();i++){JSONObject t=result.getJSONObject(i);if(t.optString("source").equals("canvas")&&!present.contains(t.optString("id")))t.put("sourceMissing",true);}
  return result;
 }
 public static JSONArray current(JSONArray old,JSONArray fresh,String term,String period)throws JSONException{
  JSONArray result=merge(old,fresh);for(int i=0;i<result.length();i++){JSONObject t=result.getJSONObject(i);if(!Agenda.canvas(t))continue;boolean archive=!term.equals(t.optString("termId"))||!period.equals(t.optString("periodKey"));t.put("archived",archive);if(archive)t.put("reminder",false);}return result;
 }
}
