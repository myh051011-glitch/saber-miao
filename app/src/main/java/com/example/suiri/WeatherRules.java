package com.example.suiri;
import org.json.*;
import java.time.*;
import java.util.*;
public final class WeatherRules {
 public static final long HOUR=3600000L;
 public static boolean due(long last,long now,long gap){return last==0||now-last>=gap;}
 public static boolean budget(String stored,int used,String month){return !stored.equals(month)||used<3000;}
 public static String host(String s){s=s.trim().toLowerCase(Locale.ROOT);if(s.startsWith("https://"))s=s.substring(8);if(!s.matches("[a-z0-9]+(?:[.-][a-z0-9]+)*\\.qweatherapi\\.com"))throw new IllegalArgumentException("请填写账号专属 qweatherapi.com 主机，不带路径或端口");return s;}
 public static String coordinate(String s,boolean lat){double v=Double.parseDouble(s);if(!Double.isFinite(v)||Math.abs(v)>(lat?90:180))throw new IllegalArgumentException("经纬度超出范围");return String.format(Locale.ROOT,"%.2f",v);}
 static JSONObject obj(JSONObject o,String k){JSONObject v=o.optJSONObject(k);return v==null?new JSONObject():v;}
 static JSONObject today(JSONObject daily,LocalDate date){JSONArray days=daily.optJSONArray("days");if(days==null)return new JSONObject();for(int i=0;i<days.length();i++){JSONObject d=days.optJSONObject(i);try{if(OffsetDateTime.parse(d.optString("forecastStartTime")).atZoneSameInstant(Schedule.ZONE).toLocalDate().equals(date))return d;}catch(Exception ignored){}}return new JSONObject();}
 public static String risks(JSONObject current,JSONObject daily,boolean alert,LocalDate day){
  JSONObject d=today(daily,day),am=obj(d,"daytime"),pm=obj(d,"nighttime");String text=condition(current)+condition(am)+condition(pm);StringBuilder out=new StringBuilder();
  if(alert)out.append("所在地有气象预警，请查看发布机构的防御指南。");
  if(text.contains("雷"))out.append("有雷雨风险，请减少户外停留。");
  if(text.contains("雪")||text.contains("冻")||obj(am,"precipitation").optString("type").matches("snow|ice|mixed")||obj(pm,"precipitation").optString("type").matches("snow|ice|mixed"))out.append("留意雨雪和结冰，出行慢一点。");
  if(text.contains("雨")||obj(am,"precipitation").optDouble("probability",0)>=0.5||obj(pm,"precipitation").optDouble("probability",0)>=0.5)out.append("有降雨可能，请备好雨伞，留意路面。");
  if(obj(current,"wind").optInt("scale",0)>=6||obj(am,"wind").optInt("scale",0)>=6||obj(pm,"wind").optInt("scale",0)>=6||obj(am,"windGustMax").optDouble("value",0)>=10.8||obj(pm,"windGustMax").optDouble("value",0)>=10.8)out.append("有大风风险，请留意高空坠物。");
  if(text.contains("雾")||text.contains("霾"))out.append("能见度或空气状况不佳，出行请多留意。");
  if(obj(current,"temperature").optDouble("value",20)>=35||obj(d,"temperatureMax").optDouble("value",20)>=35)out.append("天气炎热，请及时补水，避开暴晒。");
  if(obj(current,"temperature").optDouble("value",20)<=5||obj(d,"temperatureMin").optDouble("value",20)<=5)out.append("气温较低，请记得添衣。");
  return out.toString();
 }
 public static String advice(JSONObject current,JSONObject daily,boolean alert,LocalDate day){JSONObject d=today(daily,day);String report=d.length()==0?"今日预报尚未更新。":"今天白天"+obj(obj(d,"daytime"),"condition").optString("text","天气待更新")+"，夜间"+obj(obj(d,"nighttime"),"condition").optString("text","天气待更新")+"。";String risk=risks(current,daily,alert,day);return "亲爱的master，"+report+(risk.isEmpty()?"也请给自己留些休息时间。":risk);}
 public static JSONArray active(JSONObject warning,Instant now){JSONArray all=warning.optJSONArray("alerts"),out=new JSONArray();if(all==null)return out;for(int i=0;i<all.length();i++){JSONObject a=all.optJSONObject(i);if(a==null||obj(a,"messageType").optString("code").equals("cancel"))continue;try{if(!a.optString("expireTime").isEmpty()&&!OffsetDateTime.parse(a.optString("expireTime")).toInstant().isAfter(now))continue;}catch(Exception ignored){continue;}out.put(a);}return out;}
 public static String condition(JSONObject o){return obj(o,"condition").optString("text","天气待更新");}
 public static String temperature(JSONObject o){double t=obj(o,"temperature").optDouble("value",Double.NaN);return Double.isFinite(t)?Math.round(t)+"°":"--°";}
}
