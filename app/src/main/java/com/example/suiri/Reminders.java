package com.example.suiri;
import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.Build;
import org.json.*;

public final class Reminders {
 public static final String CHANNEL="tasks",FIRE="com.example.suiri.REMIND",DONE="com.example.suiri.DONE";
 static AlarmManager manager(Context c){return (AlarmManager)c.getSystemService(Context.ALARM_SERVICE);}
 public static void channel(Context c){NotificationChannel ch=new NotificationChannel(CHANNEL,"待办提醒",NotificationManager.IMPORTANCE_HIGH);ch.setDescription("仅提醒你设置的待办，不提醒课程");c.getSystemService(NotificationManager.class).createNotificationChannel(ch);}
 public static boolean notifications(Context c){channel(c);NotificationManager n=c.getSystemService(NotificationManager.class);return n.areNotificationsEnabled()&&n.getNotificationChannel(CHANNEL).getImportance()!=NotificationManager.IMPORTANCE_NONE;}
 public static boolean exact(Context c){return Build.VERSION.SDK_INT<31||manager(c).canScheduleExactAlarms();}
 static PendingIntent alarm(Context c,String id){Intent i=new Intent(c,ReminderReceiver.class).setAction(FIRE).setData(Uri.parse("suiri://reminder/"+Uri.encode(id))).putExtra("id",id);return PendingIntent.getBroadcast(c,0,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
 public static PendingIntent done(Context c,String id){Intent i=new Intent(c,ReminderReceiver.class).setAction(DONE).setData(Uri.parse("suiri://done/"+Uri.encode(id))).putExtra("id",id);return PendingIntent.getBroadcast(c,0,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
 public static void cancel(Context c,String id,boolean notification){manager(c).cancel(alarm(c,id));if(notification)c.getSystemService(NotificationManager.class).cancel(id,0);}
 public static void reschedule(Context c){
  channel(c);JSONArray tasks=TaskStore.load(c);long now=System.currentTimeMillis();
  for(int i=0;i<tasks.length();i++){JSONObject t=tasks.optJSONObject(i);if(t==null)continue;String id=t.optString("id");cancel(c,id,false);
   long next=TaskDates.nextAlarm((t.optBoolean("done")||t.optBoolean("deleted")||t.optBoolean("archived")),t.optBoolean("reminder"),TaskStore.at(t),t.optLong("reminderFired"),now);
   if(next==0||!notifications(c))continue;
   try{if(exact(c))manager(c).setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next,alarm(c,id));else manager(c).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next,alarm(c,id));}
   catch(SecurityException e){manager(c).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next,alarm(c,id));}
  }
 }
 public static synchronized void fire(Context c,String id){
  JSONArray tasks=TaskStore.load(c);JSONObject t=TaskStore.find(tasks,id);if(t==null||t.optBoolean("deleted")||t.optBoolean("archived")||t.optBoolean("done")||!t.optBoolean("reminder"))return;
  long at=TaskStore.at(t),now=System.currentTimeMillis();if(at==0||t.optLong("reminderFired")==at)return;
  if(at>now+1000){reschedule(c);return;}if(!notifications(c))return;
  Intent open=new Intent(c,MainActivity.class).setData(Uri.parse("suiri://task/"+Uri.encode(id))).putExtra("task",id);
  PendingIntent pi=PendingIntent.getActivity(c,0,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  String detail="预计 "+t.optInt("minutes",30)+" 分钟"+(t.optString("due").isEmpty()?"":" · 计划 "+t.optString("due"));
  if(now-at>120000)detail="延迟送达 · "+detail;
  Notification n=new Notification.Builder(c,CHANNEL).setSmallIcon(R.drawable.ic_notification).setContentTitle(t.optString("title")).setContentText(detail).setStyle(new Notification.BigTextStyle().bigText(detail)).setContentIntent(pi).setAutoCancel(true).setVisibility(Notification.VISIBILITY_PRIVATE).addAction(new Notification.Action.Builder(null,"完成",done(c,id)).build()).build();
  try{c.getSystemService(NotificationManager.class).notify(id,0,n);t.put("reminderFired",at);TaskStore.patch(c,id,new JSONObject().put("reminderFired",at));}catch(Exception ignored){}
  SuiRiWidget.refresh(c);
 }
}
