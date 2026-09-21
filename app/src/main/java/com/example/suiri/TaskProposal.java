package com.example.suiri;
import java.time.LocalDate;
import java.util.regex.*;
import org.json.*;
/** Parse a suggestion into a draft only. Does not write data or enable reminders. */
public final class TaskProposal {
 public static JSONObject parse(String answer){try{Matcher m=Pattern.compile("\\{\\s*\"task\"\\s*:").matcher(answer);if(!m.find())return new JSONObject();JSONObject p=((JSONObject)new JSONTokener(answer.substring(m.start())).nextValue()).getJSONObject("task"),d=new JSONObject();String title=p.optString("title","");d.put("title",title.substring(0,Math.min(title.length(),200)));String date=p.optString("due");if(!date.isEmpty())LocalDate.parse(date);d.put("due",date);d.put("minutes",Math.max(1,Math.min(1440,p.optInt("minutes",30))));return d;}catch(Exception e){return new JSONObject();}}
}
