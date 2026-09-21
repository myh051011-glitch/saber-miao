package com.example.suiri;
import org.json.*;
import java.time.*;
import java.util.*;
public final class CanvasTerms {
 public static String id(JSONObject c){JSONObject t=c.optJSONObject("term");return t==null?c.optString("enrollment_term_id"):t.optString("id",c.optString("enrollment_term_id"));}
 public static JSONArray options(JSONArray courses)throws JSONException{JSONArray a=new JSONArray();Set<String> seen=new HashSet<>();for(int i=0;i<courses.length();i++){JSONObject c=courses.getJSONObject(i);String id=id(c);if(!id.matches("[0-9]+")||!seen.add(id))continue;JSONObject term=c.optJSONObject("term");JSONObject item=term==null?new JSONObject():new JSONObject(term.toString());item.put("id",id);if(item.optString("name").isEmpty())item.put("name","学期 "+id);a.put(item);}return a;}
 public static String current(JSONArray terms,Instant now){String selected="";for(int i=0;i<terms.length();i++){JSONObject t=terms.optJSONObject(i);try{Instant start=OffsetDateTime.parse(t.getString("start_at")).toInstant(),end=OffsetDateTime.parse(t.getString("end_at")).toInstant();if(!now.isBefore(start)&&now.isBefore(end)){if(!selected.isEmpty())return "";selected=t.getString("id");}}catch(Exception ignored){}}return selected;}
 public static JSONArray filter(JSONArray courses,String term)throws JSONException{if(!term.matches("[0-9]+"))throw new JSONException("请先选择当前学期");JSONArray a=new JSONArray();for(int i=0;i<courses.length();i++)if(term.equals(id(courses.getJSONObject(i))))a.put(courses.getJSONObject(i));return a;}
}
