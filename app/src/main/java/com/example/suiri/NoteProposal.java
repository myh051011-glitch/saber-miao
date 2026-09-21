package com.example.suiri;
import org.json.*;
public final class NoteProposal {
 /** Only note text can be changed by this operation; never dates, credentials or completion. */
 public static JSONObject parse(String reply)throws JSONException{
  for(int start=reply.indexOf('{');start>=0;start=reply.indexOf('{',start+1))try{
   JSONObject root=(JSONObject)new JSONTokener(reply.substring(start)).nextValue();JSONObject n=root.optJSONObject("note_change");if(n==null)continue;
   String id=n.getString("id"),op=n.getString("operation"),value=n.optString("text","");
   if(id.isEmpty()||id.length()>200||!java.util.Arrays.asList("append","replace","clear").contains(op)||value.length()>5000||(!op.equals("clear")&&value.trim().isEmpty()))throw new JSONException("备注修改内容无效");
   return new JSONObject().put("id",id).put("operation",op).put("text",value);
  }catch(ClassCastException ignored){}catch(JSONException ignored){}
  throw new JSONException("回复中没有可用的备注修改，请让助手明确事项和修改内容");
 }
 public static String next(String old,JSONObject change)throws JSONException{String op=change.getString("operation"),value=change.optString("text");String next;if(op.equals("clear"))next="";else if(op.equals("replace"))next=value;else if(op.equals("append"))next=old.isEmpty()?value:old+"\n"+value;else throw new JSONException("未知备注操作");if(next.length()>5000)throw new JSONException("备注最多 5000 字");return next;}
}
