package com.example.suiri;
import android.content.*;
import org.json.*;
import java.time.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
public final class CanvasSync {
 static final AtomicBoolean running=new AtomicBoolean();
 public static SchoolCalendar.Period period(Context c){LocalDate now=LocalDate.now(Schedule.ZONE);android.content.SharedPreferences p=TaskStore.prefs(c);try{SchoolCalendar.Period custom=new SchoolCalendar.Period(p.getString("periodName","自定义学期"),p.getString("periodStart",""),p.getString("periodEnd",""),p.getBoolean("periodGap",false));if(custom.contains(now))return custom;}catch(Exception ignored){}return SchoolCalendar.current(now);}
 public static boolean due(Context c){android.content.SharedPreferences p=TaskStore.prefs(c);return !LocalDate.now(Schedule.ZONE).toString().equals(p.getString("canvasSuccessDay",""))||System.currentTimeMillis()-p.getLong("canvasSuccessMillis",0)>=60*60*1000;}
 public static int run(Context c,BooleanSupplier cancelled)throws Exception{
  if(!running.compareAndSet(false,true))throw new Exception("Canvas 正在同步，请稍候");
  try{android.content.SharedPreferences p=TaskStore.prefs(c);p.edit().putLong("canvasAttempt",System.currentTimeMillis()).apply();SchoolCalendar.Period period=period(c);if(period==null)throw new Exception("请在设置中核对并填写新学期校历日期");
   String key=Secrets.get(c,"canvas");if(key.isEmpty())throw new Exception("请先配置 Canvas 令牌");
   String original=p.getString("canvasTerm",""),originalPeriod=p.getString("canvasTermPeriod","");
   JSONArray courses=Api.canvasCourses(key),terms=CanvasTerms.options(courses);p.edit().putString("canvasTerms",terms.toString()).apply();
   String term=period.key.equals(originalPeriod)?original:"";if(term.isEmpty())term=SchoolCalendar.matching(terms,period);
   if(term.isEmpty())throw new Exception("已读取学校学期列表，请在设置中选择与「"+period.name+"」对应的 Canvas 学期");
   if(CanvasTerms.filter(courses,term).length()==0)throw new Exception("所选学期没有可访问课程，请重新选择学期");
   JSONArray fresh=Api.canvas(key,courses,term,period);
   synchronized(TaskStore.class){SchoolCalendar.Period current=period(c);if(cancelled.getAsBoolean()||current==null||!current.key.equals(period.key)||!Secrets.get(c,"canvas").equals(key)||!p.getString("canvasTerm","").equals(original)||!p.getString("canvasTermPeriod","").equals(originalPeriod))throw new Exception("配置已变化或同步被中断，本次结果未保存");
    TaskStore.save(c,CanvasMerge.current(TaskStore.load(c),fresh,term,period.key));String name="学期 "+term;for(int i=0;i<terms.length();i++)if(term.equals(terms.getJSONObject(i).optString("id")))name=terms.getJSONObject(i).optString("name");
    p.edit().putString("canvasTerm",term).putString("canvasTermName",name).putString("canvasTermPeriod",period.key).putLong("canvasSuccessMillis",System.currentTimeMillis()).putString("canvasSuccessDay",LocalDate.now(Schedule.ZONE).toString()).putString("canvasSync",LocalDateTime.now(Schedule.ZONE).withSecond(0).withNano(0).toString()).putString("canvasError","").apply();
   }FocusReminder.check(c);return fresh.length();
  }finally{running.set(false);}
 }
 public static void archivePrevious(Context c){SchoolCalendar.Period period=period(c);if(period==null)return;synchronized(TaskStore.class){JSONArray data=TaskStore.load(c);boolean changed=false;try{for(int i=0;i<data.length();i++){JSONObject t=data.getJSONObject(i);if(Agenda.canvas(t)&&!t.optString("periodKey").isEmpty()&&!t.optString("periodKey").equals(period.key)&&!t.optBoolean("archived")){t.put("archived",true);t.put("reminder",false);changed=true;}}if(changed)TaskStore.save(c,data);}catch(JSONException ignored){}}}
}
