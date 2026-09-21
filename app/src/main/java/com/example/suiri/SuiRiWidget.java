package com.example.suiri;
import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;
import org.json.*;
import java.time.*;
import java.util.*;
public class SuiRiWidget extends AppWidgetProvider {
 static final String SCOPE="com.example.suiri.WIDGET_SCOPE",REFRESH="com.example.suiri.WIDGET_REFRESH",ITEM="com.example.suiri.WIDGET_ITEM";
 public static void refresh(Context c){AppWidgetManager m=AppWidgetManager.getInstance(c);for(int id:m.getAppWidgetIds(new ComponentName(c,SuiRiWidget.class)))update(c,m,id);}
 static PendingIntent open(Context c,String page,String task,boolean add){Intent i=new Intent(c,MainActivity.class).setAction(Intent.ACTION_VIEW).setData(Uri.parse("suiri://open/"+page+"/"+Uri.encode(task)+"/"+add)).putExtra("page",page).putExtra("task",task).putExtra("add",add);return PendingIntent.getActivity(c,0,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
 static PendingIntent action(Context c,int id,String scope){Intent i=new Intent(c,SuiRiWidget.class).setAction(SCOPE).setData(Uri.parse("suiri://widget/"+id+"/"+scope)).putExtra("widget",id).putExtra("scope",scope);return PendingIntent.getBroadcast(c,0,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
 @Override public void onUpdate(Context c,AppWidgetManager m,int[] ids){Weather.ensure(c);for(int id:ids)update(c,m,id);}
 @Override public void onAppWidgetOptionsChanged(Context c,AppWidgetManager m,int id,Bundle options){update(c,m,id);}
 @Override public void onDeleted(Context c,int[] ids){for(int id:ids)TaskStore.prefs(c).edit().remove("scope"+id).apply();}
 @Override public void onReceive(Context c,Intent i){super.onReceive(c,i);if(SCOPE.equals(i.getAction())){String scope=i.getStringExtra("scope");if(!Arrays.asList("today","week","month").contains(scope))return;int id=i.getIntExtra("widget",-1);TaskStore.prefs(c).edit().putString("scope"+id,scope).apply();update(c,AppWidgetManager.getInstance(c),id);}else if("com.example.suiri.WIDGET_CLOCK".equals(i.getAction())){refresh(c);}else if(REFRESH.equals(i.getAction())){CanvasJob.ensure(c);Weather.ensure(c);refresh(c);}else if(ITEM.equals(i.getAction())){String task=i.getStringExtra("task");if("done".equals(i.getStringExtra("operation"))){try{if(task!=null)TaskStore.patch(c,task,new JSONObject().put("done",true));}catch(Exception ignored){}}else{Intent open=new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("page",i.getStringExtra("scope")).putExtra("task",task);c.startActivity(open);}}}
 static void update(Context c,AppWidgetManager m,int id){String scope=TaskStore.prefs(c).getString("scope"+id,"today");LocalDate today=LocalDate.now(Schedule.ZONE);RemoteViews v=new RemoteViews(c.getPackageName(),R.layout.widget);v.setTextViewText(R.id.widget_title,"saber喵 · "+today.getMonthValue()+"/"+today.getDayOfMonth());int[] tabs={R.id.scope_today,R.id.scope_week,R.id.scope_month};String[] keys={"today","week","month"},labels={"今天","未来7天","本月"};for(int n=0;n<3;n++){v.setOnClickPendingIntent(tabs[n],action(c,id,keys[n]));v.setTextViewText(tabs[n],(scope.equals(keys[n])?"● ":"")+labels[n]);}
  v.setTextViewText(R.id.widget_weather,Weather.summary(c));v.setTextViewText(R.id.widget_advice,Weather.advice(c));PendingIntent weather=PendingIntent.getActivity(c,4300,new Intent(c,PetChatActivity.class).putExtra("weather",true),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);v.setImageViewBitmap(R.id.widget_saber,Saber.widgetHead(c));v.setOnClickPendingIntent(R.id.widget_saber,weather);v.setOnClickPendingIntent(R.id.widget_root,open(c,scope,"",false));v.setOnClickPendingIntent(R.id.widget_blank,open(c,scope,"",false));v.setOnClickPendingIntent(R.id.widget_open_background,open(c,scope,"",false));v.setOnClickPendingIntent(R.id.widget_weather,weather);v.setOnClickPendingIntent(R.id.widget_advice,weather);
  v.setOnClickPendingIntent(R.id.widget_title,open(c,scope,"",false));v.setOnClickPendingIntent(R.id.widget_add,open(c,"today","",true));v.setOnClickPendingIntent(R.id.widget_summary,open(c,scope,"",false));v.setViewVisibility(R.id.widget_summary,android.view.View.GONE);v.setViewVisibility(R.id.widget_rows,scope.equals("month")?android.view.View.GONE:android.view.View.VISIBLE);v.setViewVisibility(R.id.widget_month,scope.equals("month")?android.view.View.VISIBLE:android.view.View.GONE);if(scope.equals("month"))MonthWidget.render(c,v,today);
  if(android.os.Build.VERSION.SDK_INT>=31){
   RemoteViews.RemoteCollectionItems.Builder items=new RemoteViews.RemoteCollectionItems.Builder().setViewTypeCount(1).setHasStableIds(false);
   List<WidgetService.Row> rows=WidgetService.load(c,scope);int limit=Math.min(rows.size(),160);for(int n=0;n<limit;n++)items.addItem(n,WidgetService.view(c,rows.get(n),scope));
   if(rows.size()>limit)items.addItem(limit,WidgetService.view(c,new WidgetService.Row("打开查看全部", ""),scope));v.setRemoteAdapter(R.id.widget_rows,items.build());
  }else{Intent adapter=new Intent(c,WidgetService.class).putExtra("widget",id).setData(Uri.parse("suiri://list/"+id));v.setRemoteAdapter(R.id.widget_rows,adapter);}
  Intent template=new Intent(c,SuiRiWidget.class).setAction(ITEM).setData(Uri.parse("suiri://item/"+id));v.setPendingIntentTemplate(R.id.widget_rows,PendingIntent.getBroadcast(c,id,template,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE));
  v.setOnClickPendingIntent(R.id.widget_footer,PendingIntent.getBroadcast(c,0,new Intent(c,SuiRiWidget.class).setAction(REFRESH),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE));v.setTextViewText(R.id.widget_footer,"刷新");m.updateAppWidget(id,v);if(android.os.Build.VERSION.SDK_INT<31)m.notifyAppWidgetViewDataChanged(id,R.id.widget_rows);scheduleClock(c);
 }
 static void scheduleClock(Context c){Instant now=Instant.now();LocalDate day=LocalDate.now(Schedule.ZONE);Instant next=day.plusDays(1).atStartOfDay(Schedule.ZONE).toInstant();try{for(Schedule.Event e:Schedule.parse(TaskStore.prefs(c).getString("ics","")))if(e.on(day)){Instant end=CourseStatus.end(e,day);if(end.isAfter(now)&&end.isBefore(next))next=end;}}catch(Exception ignored){}PendingIntent tick=PendingIntent.getBroadcast(c,8300,new Intent(c,SuiRiWidget.class).setAction("com.example.suiri.WIDGET_CLOCK"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);c.getSystemService(AlarmManager.class).set(AlarmManager.RTC,next.toEpochMilli(),tick);}

}
