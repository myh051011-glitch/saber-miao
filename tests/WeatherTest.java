package com.example.suiri;
import org.json.*;
import java.time.*;
public class WeatherTest {
 static int n;static void yes(boolean b){n++;if(!b)throw new AssertionError("check "+n);}
 public static void main(String[] args)throws Exception{
 yes(WeatherRules.due(0,1,3600000));yes(!WeatherRules.due(1000,1001,3600000));yes(WeatherRules.due(1000,3601000,3600000));yes(!WeatherRules.due(2000,1000,3600000));
 yes(WeatherRules.budget("2026-09",2999,"2026-09"));yes(!WeatherRules.budget("2026-09",3000,"2026-09"));yes(WeatherRules.budget("2026-09",3000,"2026-10"));
 yes(WeatherRules.host("https://example.ab.qweatherapi.com").equals("example.ab.qweatherapi.com"));
 for(String invalid:new String[]{"api.qweather.com","qweatherapi.com.evil.com","abc.qweatherapi.com@evil.com","abc.qweatherapi.com/path","abc.qweatherapi.com:443","http://abc.qweatherapi.com"}){boolean bad=false;try{WeatherRules.host(invalid);}catch(Exception e){bad=true;}yes(bad);}
 yes(WeatherRules.coordinate("31.234",true).equals("31.23"));for(String v:new String[]{"NaN","Infinity","91"}){boolean bad=false;try{WeatherRules.coordinate(v,true);}catch(Exception e){bad=true;}yes(bad);}
 JSONObject rain=new JSONObject("{\"condition\":{\"text\":\"小雨\"},\"temperature\":{\"value\":24}}");LocalDate day=LocalDate.of(2026,9,17);
 yes(WeatherRules.temperature(rain).equals("24°"));yes(WeatherRules.temperature(new JSONObject()).equals("--°"));yes(WeatherRules.advice(rain,new JSONObject(),false,day).contains("雨伞"));yes(WeatherRules.advice(rain,new JSONObject(),true,day).contains("预警"));
 JSONObject daily=new JSONObject("{\"days\":[{\"forecastStartTime\":\"2026-09-17T07:00+08:00\",\"daytime\":{\"condition\":{\"text\":\"晴\"},\"precipitation\":{\"probability\":0.6}}}]}");yes(WeatherRules.advice(new JSONObject(),daily,false,day).contains("雨伞"));yes(!WeatherRules.advice(new JSONObject(),daily,false,day.plusDays(1)).contains("雨伞"));
 JSONObject alerts=new JSONObject("{\"alerts\":[{\"id\":\"expired\",\"expireTime\":\"2026-09-16T12:00+08:00\"},{\"id\":\"cancelled\",\"messageType\":{\"code\":\"cancel\"}},{\"id\":\"ok\",\"expireTime\":\"2026-09-18T12:00+08:00\"}]}");JSONArray active=WeatherRules.active(alerts,Instant.parse("2026-09-17T00:00:00Z"));yes(active.length()==1);yes(active.getJSONObject(0).getString("id").equals("ok"));
 yes(Saber.emotion("你好\n{\"emotion\":\"happy\"}")==1);yes(Saber.emotion("{\"emotion\":\"unknown\"}")==0);yes(Saber.clean("主人，好。\n{\"emotion\":\"happy\",\"task_actions\":[]}").equals("亲爱的master，好。"));yes(Saber.streaming("主人，好。\n```json\n{\"emotion\":\"hap").equals("亲爱的master，好。"));
 System.out.println("PASS: "+n+" weather budget, cache, host, alert and Saber metadata checks");
 }
}
