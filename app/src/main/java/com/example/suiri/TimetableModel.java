package com.example.suiri;
import java.time.*;
import java.util.*;
public final class TimetableModel {
 public static LocalDate base(List<Schedule.Event> courses){LocalDate earliest=courses.stream().map(e->e.start.toLocalDate()).min(LocalDate::compareTo).orElse(LocalDate.of(2026,9,14));SchoolCalendar.Period p=SchoolCalendar.current(earliest);return TaskDates.weekStart(p!=null&&!p.gap?p.start:earliest);}
 public static List<Schedule.Event> events(List<Schedule.Event> all,int weekday,LocalDate base,int week){List<Schedule.Event> out=new ArrayList<>();for(Schedule.Event e:all)if(week==0?e.start.getDayOfWeek().getValue()==weekday:e.on(base.plusWeeks(week-1).plusDays(weekday-1)))out.add(e);out.sort(Comparator.comparing(e->e.start.toLocalTime()));return out;}
 public static String weeks(Schedule.Event e,LocalDate base){List<Integer> ns=new ArrayList<>();for(int n=1;n<=104;n++)if(e.on(base.plusWeeks(n-1).plusDays(e.start.getDayOfWeek().getValue()-1)))ns.add(n);if(ns.isEmpty())return e.start.toLocalDate().toString();StringBuilder s=new StringBuilder();for(int i=0;i<ns.size();){int first=ns.get(i),last=first,step=i+1<ns.size()?ns.get(i+1)-first:1;int k=i+1;if(step<=2)while(k<ns.size()&&ns.get(k)-last==step){last=ns.get(k++);}if(s.length()>0)s.append("、");s.append(first);if(last>first)s.append("—").append(last).append(step==2?(first%2==1?"单":"双"):"");i=k;}return "第"+s+"周";}
}
