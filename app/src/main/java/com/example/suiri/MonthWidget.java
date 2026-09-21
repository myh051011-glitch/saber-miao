package com.example.suiri;
import android.app.*;
import android.content.*;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;
import java.time.*;
import java.util.*;
import org.json.*;
public final class MonthWidget {
 static void render(Context c,RemoteViews v,LocalDate now){YearMonth month=YearMonth.from(now);int offset=month.atDay(1).getDayOfWeek().getValue()-1;List<Schedule.Event> courses;try{courses=Schedule.parse(TaskStore.prefs(c).getString("ics",""));}catch(Exception e){courses=Collections.emptyList();}JSONArray tasks=TaskStore.load(c);
  for(int n=0;n<42;n++){int id=c.getResources().getIdentifier("month_cell_"+n,"id",c.getPackageName()),day=n-offset+1;boolean valid=day>=1&&day<=month.lengthOfMonth();v.setViewVisibility(id,View.VISIBLE);if(!valid){v.setTextViewText(id,"");v.setOnClickPendingIntent(id,SuiRiWidget.open(c,"month","",false));continue;}LocalDate date=month.atDay(day);String marks=Agenda.monthMark(tasks,courses,date,now);v.setTextViewText(id,day+(marks.isEmpty()?"":"\n"+marks));v.setTextColor(id,date.equals(now)?Saber.GOLD:marks.contains("!")?0xffffba8a:Saber.INK);v.setContentDescription(id,date+" "+marks);Intent intent=new Intent(c,MainActivity.class).setData(Uri.parse("suiri://calendar/"+date)).putExtra("page","calendar").putExtra("date",date.toString());v.setOnClickPendingIntent(id,PendingIntent.getActivity(c,0,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE));}
 }
}
