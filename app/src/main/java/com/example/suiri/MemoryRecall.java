package com.example.suiri;
import org.json.*;
import java.util.*;
public final class MemoryRecall {
 public static String compact(JSONArray memories,String query,int budget){List<JSONObject> rows=new ArrayList<>();for(int i=0;i<memories.length();i++)if(memories.optJSONObject(i)!=null)rows.add(memories.optJSONObject(i));rows.sort(Comparator.comparingInt((JSONObject m)->score(m,query)).reversed().thenComparing(m->m.optString("updated"),Comparator.reverseOrder()));StringBuilder out=new StringBuilder();for(JSONObject m:rows){String line=m.optString("topic")+"："+m.optString("value")+"（"+m.optString("level","待观察")+"）\n";if(out.length()+line.length()>budget)continue;out.append(line);}return out.toString();}
 static int score(JSONObject m,String query){int score=m.optBoolean("locked")?8:m.optString("level").equals("多次提及")?4:0;String hay=m.optString("topic")+m.optString("value");Set<String> seen=new HashSet<>();for(int i=0;i+1<query.length();i++){String pair=query.substring(i,i+2);if(seen.add(pair)&&hay.contains(pair))score+=3;}return score;}
 public static String clip(String s,int n){return s.length()<=n?s:s.substring(0,n)+"…（本次摘要省略余下内容）";}
}
