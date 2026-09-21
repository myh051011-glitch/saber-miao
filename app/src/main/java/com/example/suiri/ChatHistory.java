package com.example.suiri;
import org.json.*;
public final class ChatHistory {
 public static JSONArray forRequest(JSONArray records,String session)throws JSONException{
  JSONArray reverse=new JSONArray();int budget=0;
  for(int i=records.length()-1;i>=0&&reverse.length()<6;i--){JSONObject m=records.getJSONObject(i);if(!session.equals(m.optString("session"))||!"saved".equals(m.optString("state")))continue;String role=m.optString("role");if(!role.equals("user")&&!role.equals("assistant"))continue;String text=role.equals("assistant")?LearnedMemory.visible(m.optString("content")):m.optString("content");
   if(role.equals("user")){JSONArray files=new JSONArray(m.optString("attachments","[]"));for(int j=0;j<files.length();j++){JSONObject file=files.getJSONObject(j);text+="\n历史附件 "+file.optString("name")+"："+MemoryRecall.clip(file.optString("text"),800);JSONArray images=file.optJSONArray("images");if(images!=null&&images.length()>0)text+="（本轮未重复上传历史图片；如需重新看图请重新附图）";}}
   text=MemoryRecall.clip(text,1500);if(budget+text.length()>6500)continue;budget+=text.length();reverse.put(Api.message(role,text));
  }JSONArray result=new JSONArray();for(int i=reverse.length()-1;i>=0;i--)result.put(reverse.getJSONObject(i));return result;
 }
}
