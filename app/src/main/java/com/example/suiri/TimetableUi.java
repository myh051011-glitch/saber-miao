package com.example.suiri;
import android.app.*;
import android.view.*;
import android.widget.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
/** Weekly timetable: recurrence uses actual calendar dates, never a guessed ICS week number. */
public final class TimetableUi {
 public static long week(LocalDate date,LocalDate start){return ChronoUnit.WEEKS.between(TaskDates.weekStart(start),TaskDates.weekStart(date))+1;}
 static void show(MainActivity a){
  if(a.courses.isEmpty()){a.body.addView(a.text("尚未导入课表，请在设置中导入 WakeUp ICS。",16));return;}
  LocalDate initial=TimetableModel.base(a.courses);try{initial=LocalDate.parse(a.prefs.getString("timetableStart",initial.toString()));}catch(Exception ignored){}final LocalDate base=TaskDates.weekStart(initial);final int chosen=a.prefs.getInt("timetableWeek",0);
  a.body.addView(a.text(chosen==0?"完整课表":"第 "+chosen+" 周课表",22));
  LinearLayout tools=new LinearLayout(a);tools.addView(a.button(chosen==0?"全部周次 ▾":"第"+chosen+"周 ▾",()->{String[] labels=new String[105];labels[0]="完整课表 · 全部周次";for(int n=1;n<labels.length;n++)labels[n]="第 "+n+" 周";new UiDialog.Builder(a).setTitle("手动选择周次").setSingleChoiceItems(labels,chosen,(dialog,n)->{a.prefs.edit().putInt("timetableWeek",n).apply();dialog.dismiss();a.render();}).setNegativeButton("取消",null).show();}),new LinearLayout.LayoutParams(0,-2,1));tools.addView(a.button("调整第1周",()->a.pickDate(base,d->{a.prefs.edit().putString("timetableStart",TaskDates.weekStart(d).toString()).apply();a.render();})),new LinearLayout.LayoutParams(0,-2,1));a.body.addView(tools);
  
  int begin=8*60,end=22*60;for(Schedule.Event e:a.courses){begin=Math.min(begin,e.start.toLocalTime().toSecondOfDay()/60);end=Math.max(end,e.end.toLocalTime().toSecondOfDay()/60);}
  final int first=begin,last=end; final float scale=1.3f;
  HorizontalScrollView horizontal=new HorizontalScrollView(a);LinearLayout grid=new LinearLayout(a);horizontal.addView(grid);int width=Math.max(a.dp(72),(a.getResources().getDisplayMetrics().widthPixels-a.dp(86))/7);
  LinearLayout times=a.column();times.addView(a.text("时间",12),new LinearLayout.LayoutParams(a.dp(48),a.dp(48)));FrameLayout axis=new FrameLayout(a);times.addView(axis,new LinearLayout.LayoutParams(a.dp(48),a.dp((int)((last-first)*scale))));for(int t=first;t<last;t+=60){TextView tv=a.text(String.format(java.util.Locale.ROOT,"%02d:%02d",t/60,t%60),10);FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-1,a.dp(24));lp.topMargin=a.dp((int)((t-first)*scale));axis.addView(tv,lp);}grid.addView(times);
  int[] colors={0xff426b86,0xff646197,0xff377f7b,0xff946580,0xff886e43,0xff67598a};
  for(int d=0;d<7;d++){final int weekday=d+1;List<Schedule.Event> events=TimetableModel.events(a.courses,weekday,base,chosen);Map<String,List<Schedule.Event>> groups=new LinkedHashMap<>();for(Schedule.Event e:events)groups.computeIfAbsent(e.start.toLocalTime()+"/"+e.end.toLocalTime(),k->new ArrayList<>()).add(e);
   List<List<Schedule.Event>> slots=new ArrayList<>(groups.values());List<Integer> ends=new ArrayList<>();int[] lanes=new int[slots.size()];for(int i=0;i<slots.size();i++){Schedule.Event e=slots.get(i).get(0);int start=e.start.toLocalTime().toSecondOfDay()/60,finish=e.end.toLocalTime().toSecondOfDay()/60;if(finish<=start)finish=last;int lane=0;while(lane<ends.size()&&ends.get(lane)>start)lane++;if(lane==ends.size())ends.add(finish);else ends.set(lane,finish);lanes[i]=lane;}int columns=Math.max(1,ends.size()),columnWidth=Math.max(width,a.dp(68)*columns);
   LinearLayout col=a.column();TextView header=a.text(new String[]{"周一","周二","周三","周四","周五","周六","周日"}[d],13);header.setGravity(Gravity.CENTER);col.addView(header,new LinearLayout.LayoutParams(columnWidth,a.dp(48)));FrameLayout cells=new FrameLayout(a);cells.setBackgroundColor(Saber.BG);col.addView(cells,new LinearLayout.LayoutParams(columnWidth,a.dp((int)((last-first)*scale))));
   for(int t=first;t<last;t+=60){View line=new View(a);line.setBackgroundColor(Saber.LINE);FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-1,a.dp(1));lp.topMargin=a.dp((int)((t-first)*scale));cells.addView(line,lp);}
   for(int n=0;n<slots.size();n++){List<Schedule.Event> slot=slots.get(n);Schedule.Event e=slot.get(0);int start=e.start.toLocalTime().toSecondOfDay()/60,finish=e.end.toLocalTime().toSecondOfDay()/60;if(finish<=start)finish=last;StringBuilder detail=new StringBuilder(),names=new StringBuilder();for(Schedule.Event item:slot){if(detail.length()>0){detail.append("\n\n");names.append(" / ");}names.append(item.title);detail.append(item.title).append("\n").append(item.location).append("\n").append(item.start.toLocalTime()).append("—").append(item.end.toLocalTime()).append("\n").append(TimetableModel.weeks(item,base));}final String message=detail.toString();TextView block=a.text(names+"\n@"+e.location+"\n"+TimetableModel.weeks(e,base)+(slot.size()>1?"\n点击查看其他课程":""),11);block.setPadding(a.dp(4),a.dp(5),a.dp(3),0);block.setMaxLines(18);block.setEllipsize(android.text.TextUtils.TruncateAt.END);block.setTextColor(0xffFFFFFF);block.setBackground(Saber.shape(a,colors[Math.floorMod(e.title.hashCode(),colors.length)],9,0));FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(columnWidth/columns-a.dp(3),a.dp(Math.max(30,(int)((finish-start)*scale)-3)));lp.leftMargin=lanes[n]*columnWidth/columns+a.dp(1);lp.topMargin=a.dp((int)((start-first)*scale));cells.addView(block,lp);block.setContentDescription(message);block.setOnClickListener(v->{TextView text=a.text(message,16);text.setPadding(a.dp(18),0,a.dp(18),0);ScrollView scroll=new ScrollView(a);scroll.addView(text);new UiDialog.Builder(a).setTitle("课程详情").setView(scroll).setPositiveButton("关闭",null).show();});}
   grid.addView(col);
  }a.body.addView(horizontal);
 }
}
