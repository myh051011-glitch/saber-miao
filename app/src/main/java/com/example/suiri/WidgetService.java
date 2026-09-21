package com.example.suiri;
import android.content.*;
import android.widget.*;
import android.view.View;
import org.json.*;
import java.time.*;
import java.util.*;
public class WidgetService extends RemoteViewsService {
 @Override public RemoteViewsFactory onGetViewFactory(Intent intent){return new Factory(getApplicationContext(),intent.getIntExtra("widget",-1));}
 static class Row {String title,detail,id="";boolean heading;Row(String t,String d){title=t;detail=d;}}
 static void header(List<Row> rows,String s){Row r=new Row(s,"");r.heading=true;rows.add(r);}
 static void tasks(List<Row> rows,JSONArray data,boolean canvas,String scope,LocalDate now){
  List<JSONObject> list=Agenda.section(data,canvas,scope,now,now,false);if(list.isEmpty())return;header(rows,canvas?"Canvas 作业":"个人待办");
  for(JSONObject t:list){Row r=new Row(t.optString("title"),Agenda.focus(t,now)?Agenda.focusLabel(t,now):t.optString("due","")+" "+t.optString("plannedTime","")+(canvas?" · Canvas":""));r.id=t.optString("id");rows.add(r);}
 }
 static List<Row> rows(JSONArray data,List<Schedule.Event> courses,String scope,Instant instant){
  List<Row> result=new ArrayList<>();if(scope.equals("month"))return result;LocalDate now=instant.atZone(Schedule.ZONE).toLocalDate();tasks(result,data,false,scope,now);
  header(result,scope.equals("week")?"未来7天课程":"今日课程");LocalDate start=now,end=scope.equals("week")?start.plusDays(6):now;int count=0;
  List<Schedule.Event> sorted=new ArrayList<>(courses);sorted.sort(Comparator.comparing(e->e.start.toLocalTime()));
  for(LocalDate day=start;!day.isAfter(end);day=day.plusDays(1))for(Schedule.Event e:sorted)if(e.on(day)&&CourseStatus.end(e,day).isAfter(instant)){result.add(new Row((scope.equals("today")?"":day.getMonthValue()+"/"+day.getDayOfMonth()+" ")+e.start.toLocalTime()+" "+e.title,CourseStatus.label(e,day,instant)+" · "+e.end.toLocalTime()+" 下课 · "+e.location));count++;}
  if(count==0)result.add(new Row("暂无剩余课程",""));tasks(result,data,true,scope,now);return Collections.unmodifiableList(result);
 }
 static List<Row> load(Context c,String scope){List<Schedule.Event> courses;try{courses=Schedule.parse(TaskStore.prefs(c).getString("ics",""));}catch(Exception e){courses=Collections.emptyList();}return rows(TaskStore.load(c),courses,scope,Instant.now());}
 static RemoteViews view(Context c,Row item,String scope){RemoteViews v=new RemoteViews(c.getPackageName(),R.layout.widget_row);v.setTextViewText(R.id.row_title,item.title);v.setTextViewText(R.id.row_detail,item.detail);v.setTextColor(R.id.row_title,item.heading?Saber.GOLD:Saber.INK);v.setViewVisibility(R.id.row_detail,item.heading||item.detail.trim().isEmpty()?View.GONE:View.VISIBLE);v.setViewVisibility(R.id.row_done,item.id.isEmpty()?View.GONE:View.VISIBLE);v.setOnClickFillInIntent(R.id.row_open,new Intent().putExtra("operation","open").putExtra("scope",scope).putExtra("task",item.id));if(!item.id.isEmpty()){v.setOnClickFillInIntent(R.id.row_done,new Intent().putExtra("operation","done").putExtra("task",item.id));v.setContentDescription(R.id.row_done,"完成："+item.title);}return v;}
 static class Snapshot {final String scope;final List<Row> rows;Snapshot(String s,List<Row> r){scope=s;rows=r;}}
 static class Factory implements RemoteViewsFactory {
  final Context c;final int widget;volatile Snapshot snapshot=new Snapshot("today",Collections.emptyList());Factory(Context c,int widget){this.c=c;this.widget=widget;}
  public void onCreate(){onDataSetChanged();}public void onDestroy(){}public int getCount(){return snapshot.rows.size();}public long getItemId(int i){return i;}public boolean hasStableIds(){return false;}public int getViewTypeCount(){return 1;}public RemoteViews getLoadingView(){return null;}
  public void onDataSetChanged(){String scope=TaskStore.prefs(c).getString("scope"+widget,"today");snapshot=new Snapshot(scope,load(c,scope));}
  public RemoteViews getViewAt(int position){Snapshot s=snapshot;return position<0||position>=s.rows.size()?null:view(c,s.rows.get(position),s.scope);}
 }
}
