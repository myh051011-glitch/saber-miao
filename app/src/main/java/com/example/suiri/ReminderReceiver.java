package com.example.suiri;
import android.content.*;
import org.json.*;
public class ReminderReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c,Intent i){String id=i.getStringExtra("id");
  if(FocusReminder.ACTION.equals(i.getAction())){CanvasSync.archivePrevious(c);FocusReminder.check(c);SuiRiWidget.refresh(c);return;}
  if(Reminders.FIRE.equals(i.getAction())){Reminders.fire(c,id);return;}
  if(Reminders.DONE.equals(i.getAction())){JSONArray a=TaskStore.load(c);JSONObject t=TaskStore.find(a,id);if(t!=null)try{TaskStore.patch(c,id,new JSONObject().put("done",true));}catch(Exception ignored){}return;}
  CanvasSync.archivePrevious(c);Reminders.reschedule(c);CanvasJob.ensure(c);FocusReminder.check(c);SuiRiWidget.refresh(c);
 }
}
