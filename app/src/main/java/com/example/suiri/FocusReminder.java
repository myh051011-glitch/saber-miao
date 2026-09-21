package com.example.suiri;
import android.app.*;
import android.content.*;
import java.time.*;
import java.util.*;
import org.json.*;
public final class FocusReminder {
 static final String ACTION="com.example.suiri.FOCUS",CHANNEL="focus";
 static PendingIntent alarm(Context c){return PendingIntent.getBroadcast(c,700,new Intent(c,ReminderReceiver.class).setAction(ACTION),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
 public static void schedule(Context c){ZonedDateTime now=ZonedDateTime.now(Schedule.ZONE),next=now.toLocalDate().atTime(9,0).atZone(Schedule.ZONE);if(!next.isAfter(now))next=next.plusDays(1);c.getSystemService(AlarmManager.class).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next.toInstant().toEpochMilli(),alarm(c));}
 public static synchronized void check(Context c){schedule(c);LocalDate now=LocalDate.now(Schedule.ZONE);NotificationManager manager=c.getSystemService(NotificationManager.class);manager.createNotificationChannel(new NotificationChannel(CHANNEL,"每日重点待办",NotificationManager.IMPORTANCE_DEFAULT));if(!TaskStore.prefs(c).getBoolean("focusNotify",true)||LocalTime.now(Schedule.ZONE).isBefore(LocalTime.of(9,0))||!manager.areNotificationsEnabled()||manager.getNotificationChannel(CHANNEL).getImportance()==0)return;
  List<JSONObject> items=new ArrayList<>();for(JSONObject t:Agenda.ordered(TaskStore.load(c),now))if(Agenda.focus(t,now))items.add(t);if(items.isEmpty()){manager.cancel("focus",700);return;}if(now.toString().equals(TaskStore.prefs(c).getString("focusDay","")))return;
  StringBuilder details=new StringBuilder();for(int i=0;i<Math.min(6,items.size());i++){JSONObject t=items.get(i);details.append(Agenda.focusLabel(t,now)).append(" · ").append(t.optString("title")).append('\n');}
  Notification n=new Notification.Builder(c,CHANNEL).setSmallIcon(R.drawable.ic_notification).setContentTitle(items.size()+" 件重点待办").setContentText(details.toString()).setStyle(new Notification.BigTextStyle().bigText(details.toString())).setContentIntent(SuiRiWidget.open(c,"today","",false)).setVisibility(Notification.VISIBILITY_PRIVATE).setAutoCancel(true).build();try{manager.notify("focus",700,n);TaskStore.prefs(c).edit().putString("focusDay",now.toString()).apply();}catch(SecurityException ignored){}
 }
}
