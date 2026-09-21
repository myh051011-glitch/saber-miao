package com.example.suiri;
import org.json.*;
import java.time.*;
import java.util.*;
public final class LearnedMemory {
 public static JSONObject structured(String reply,String key){for(int i=reply.indexOf('{');i>=0;i=reply.indexOf('{',i+1))try{Object value=new JSONTokener(reply.substring(i)).nextValue();if(value instanceof JSONObject&&((JSONObject)value).has(key))return (JSONObject)value;}catch(Exception ignored){}return new JSONObject();}
 public static JSONArray learn(JSONArray old,String reply,String user,LocalDate day)throws JSONException{
  JSONArray result=new JSONArray(old.toString()),updates=structured(reply,"memory_updates").optJSONArray("memory_updates");if(updates==null)return result;
  for(int i=0;i<Math.min(3,updates.length());i++){JSONObject update=updates.optJSONObject(i);if(update==null)continue;String topic=update.optString("topic").trim(),value=update.optString("value").trim(),evidence=update.optString("evidence").trim();
   if(topic.isEmpty()||topic.length()>40||value.isEmpty()||value.length()>300||evidence.length()<4||evidence.length()>300||!user.contains(evidence))continue;
   JSONObject memory=null;for(int j=0;j<result.length();j++)if(result.getJSONObject(j).optString("topic").equals(topic))memory=result.getJSONObject(j);
   if(memory==null){memory=new JSONObject().put("topic",topic);result.put(memory);}if(memory.optBoolean("locked"))continue;
   boolean same=value.equals(memory.optString("value"));JSONArray proofs=same?memory.optJSONArray("proofs"):null;if(proofs==null)proofs=new JSONArray();boolean duplicate=false;for(int j=0;j<proofs.length();j++)if(proofs.getJSONObject(j).optString("quote").equals(evidence))duplicate=true;if(!duplicate)proofs.put(new JSONObject().put("date",day.toString()).put("quote",evidence));while(proofs.length()>5)proofs.remove(0);
   memory.put("value",value).put("proofs",proofs).put("updated",day.toString()).put("level",proofs.length()>=2?"多次提及":"待观察");
  }return result;
 }
 public static String context(JSONArray memories,LocalDate today){StringBuilder b=new StringBuilder();for(int i=memories.length()-1;i>=0&&b.length()<6000;i--){JSONObject m=memories.optJSONObject(i);if(m==null)continue;try{if(!m.optBoolean("locked")&&m.optString("level").equals("待观察")&&LocalDate.parse(m.optString("updated")).isBefore(today.minusDays(30)))continue;}catch(Exception ignored){}b.append(m.optString("topic")).append("：").append(m.optString("value")).append("（").append(m.optString("level","待观察")).append("；更新 ").append(m.optString("updated")).append("）\n");}return b.toString();}
 public static String behavior(JSONArray tasks,Instant now){int count=0;int[] bands=new int[4];for(int i=0;i<tasks.length();i++){JSONObject t=tasks.optJSONObject(i);if(t==null||!t.optBoolean("done"))continue;try{Instant at=Instant.ofEpochMilli(t.optLong("completedAt"));if(at.isAfter(now)||at.isBefore(now.minusSeconds(30*86400)))continue;int hour=at.atZone(Schedule.ZONE).getHour();bands[hour<6?0:hour<12?1:hour<18?2:3]++;count++;}catch(Exception ignored){}}return "最近30天有时间记录的完成打勾："+count+" 次；凌晨/上午/下午/晚间分别 "+Arrays.toString(bands)+"。这些是点击完成的时刻，不是实际工作时长或固定习惯；样本少时不要作结论。";}
 public static String visible(String reply){String s=reply;for(String key:new String[]{"memory_updates","note_change","task","task_actions","emotion"}){JSONObject o=structured(s,key);if(o.length()==0)continue;for(int i=s.indexOf('{');i>=0;i=s.indexOf('{',i+1))try{JSONTokener t=new JSONTokener(s.substring(i));Object v=t.nextValue();if(v instanceof JSONObject&&((JSONObject)v).has(key)){int end=i;boolean string=false,escape=false;int depth=0;for(;end<s.length();end++){char ch=s.charAt(end);if(string){if(escape)escape=false;else if(ch=='\\')escape=true;else if(ch=='"')string=false;}else if(ch=='"')string=true;else if(ch=='{')depth++;else if(ch=='}'&&--depth==0){end++;break;}}s=s.substring(0,i)+s.substring(end);break;}}catch(Exception ignored){}}return Addressing.normalize(s.replace("```json","").replace("```","").trim());}
}
