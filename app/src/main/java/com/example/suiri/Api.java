package com.example.suiri;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;
import org.json.*;
import android.content.Context;
import android.text.Html;
import java.time.*;

/** No cookies, no credential-bearing redirects, HTTPS with platform trust validation. */
public final class Api {
 public static final String CANVAS="https://canvas.tongji.edu.cn";
 public static final class Reply{public String body,next;Reply(String b,String n){body=b;next=n;}}
 public static Reply request(String address,String key,JSONObject body)throws Exception{
  URL url=new URL(address);if(!url.getProtocol().equals("https")||(!url.getHost().equals("canvas.tongji.edu.cn")&&!url.getHost().equals("api.deepseek.com"))||url.getUserInfo()!=null||(url.getPort()!=-1&&url.getPort()!=443))throw new IOException("仅支持同济 Canvas 和 DeepSeek 官方 HTTPS 接口");
  HttpURLConnection c=(HttpURLConnection)url.openConnection();c.setInstanceFollowRedirects(false);c.setConnectTimeout(15000);c.setReadTimeout(90000);c.setRequestProperty("Authorization","Bearer "+key);c.setRequestProperty("Accept","application/json");
  try{if(body!=null){c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json; charset=utf-8");try(OutputStream out=c.getOutputStream()){out.write(body.toString().getBytes(StandardCharsets.UTF_8));}}
   int status=c.getResponseCode();if(status>=300&&status<400)throw new IOException("接口跳转到登录或其他地址；请检查令牌和学校访问条件");
   if(status==401)throw new IOException("HTTP 401：令牌无效、过期或被撤销，请到 Canvas 账户设置核对有效期；不会自动清除已保存令牌");if(status==403)throw new IOException("HTTP 403：该令牌缺少权限或学校限制访问，重新生成令牌未必能解决");if(status==402)throw new IOException("API 余额不足");if(status==429)throw new IOException("请求过于频繁或额度不足，请稍后重试");if(status<200||status>=300)throw new IOException("服务返回 HTTP "+status+"，请稍后重试");
   String result;try(InputStream in=c.getInputStream()){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1){out.write(buf,0,n);if(out.size()>4_000_000)throw new IOException("响应过大，已停止读取");}result=out.toString("UTF-8");}
   if(!result.trim().startsWith("{")&&!result.trim().startsWith("["))throw new IOException("接口返回非 JSON 内容，可能需要重新登录或学校限制 API");
   String next=null,link=c.getHeaderField("Link");if(link!=null){Matcher m=Pattern.compile("<([^>]+)>\\s*;\\s*rel=\"next\"").matcher(link);if(m.find()){URL target=new URL(url,m.group(1));if(!target.getProtocol().equals("https")||!target.getHost().equals(url.getHost())||target.getUserInfo()!=null||(target.getPort()!=-1&&target.getPort()!=443))throw new IOException("分页地址不属于当前服务，已停止");next=target.toString();}}
   return new Reply(result,next);
  }finally{c.disconnect();}
 }
 static JSONArray pages(String path,String key)throws Exception{JSONArray all=new JSONArray();Set<String> seen=new HashSet<>();String url=CANVAS+path;int page=0;while(url!=null){if(++page>50||!seen.add(url))throw new IOException("分页超出限制或重复，未保存本次同步");Reply r=request(url,key,null);JSONArray a=new JSONArray(r.body);for(int n=0;n<a.length();n++){all.put(a.getJSONObject(n));if(all.length()>5000)throw new IOException("数据超过单次限制，未保存本次同步");}url=r.next;}return all;}
 public static JSONArray canvasCourses(String key)throws Exception{return pages("/api/v1/courses?enrollment_state=active&include%5B%5D=term&per_page=100",key);}
 public static JSONArray canvas(String key,JSONArray courses,String term,SchoolCalendar.Period period)throws Exception{
  JSONArray result=new JSONArray();courses=CanvasTerms.filter(courses,term);
  if(courses.length()>80)throw new IOException("课程超过本版同步限制，未保存本次同步");
  for(int n=0;n<courses.length();n++){JSONObject course=courses.getJSONObject(n);String cid=course.optString("id");if(!cid.matches("[0-9]+"))continue;
   JSONArray assignments=pages("/api/v1/courses/"+cid+"/assignments?include%5B%5D=submission&per_page=100",key);
   for(int j=0;j<assignments.length();j++){JSONObject a=assignments.getJSONObject(j);if(!a.optBoolean("published",true))continue;
    String aid=a.optString("id");if(!aid.matches("[0-9]+"))continue;JSONObject t=new JSONObject();t.put("id","canvas:"+cid+":"+aid);t.put("title",a.optString("name","Canvas 作业"));t.put("source","canvas");t.put("course",course.optString("name","课程"));t.put("url",CANVAS+"/courses/"+cid+"/assignments/"+aid);
    t.put("termId",term);t.put("periodKey",period.key);
    String deadline=a.isNull("due_at")?"":a.optString("due_at");t.put("canvasDue",deadline);String due="",time="";if(!deadline.isEmpty()){ZonedDateTime z=OffsetDateTime.parse(deadline).atZoneSameInstant(Schedule.ZONE);due=z.toLocalDate().toString();time=z.toLocalTime().withSecond(0).withNano(0).toString();}if(period.gap&&(due.isEmpty()||!period.contains(LocalDate.parse(due))))continue;t.put("due",due);t.put("plannedTime",time);
    JSONObject submission=a.optJSONObject("submission");String state=submission==null?"未知":submission.optString("workflow_state","未知");boolean submitted=submission!=null&&(!submission.isNull("submitted_at")||state.equals("submitted")||state.equals("pending_review")||state.equals("graded"));t.put("submitted",submitted);t.put("submissionState",state);t.put("done",false);t.put("minutes",60);t.put("estimateUnknown",true);
    String desc=a.isNull("description")?"":Html.fromHtml(a.optString("description"),Html.FROM_HTML_MODE_LEGACY).toString();t.put("description",desc.length()>8000?desc.substring(0,8000):desc);result.put(t);
   }
  }return result;
 }
 public static JSONObject message(String role,String text)throws JSONException{return new JSONObject().put("role",role).put("content",text);}
 public static JSONObject chat(String key,String model,JSONArray messages)throws Exception{JSONObject body=new JSONObject().put("model",model).put("messages",messages).put("stream",false).put("thinking",new JSONObject().put("type","disabled")).put("max_tokens",1600);return new JSONObject(request("https://api.deepseek.com/chat/completions",key,body).body);}
}
