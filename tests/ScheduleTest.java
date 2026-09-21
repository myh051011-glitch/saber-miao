package com.example.suiri;
import java.nio.file.*;
import java.time.*;
import java.util.*;
public class ScheduleTest {
 static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 static long count(List<Schedule.Event> es,String day,String name){return es.stream().filter(e->e.on(LocalDate.parse(day))&&(name==null||name.equals(e.title))).count();}
 public static void main(String[] args)throws Exception{
  String raw=new String(Files.readAllBytes(Paths.get(args[0])),java.nio.charset.StandardCharsets.UTF_8);
  List<Schedule.Event> es=Schedule.parse(raw);
  check(es.size()==36,"event count");check(count(es,"2026-09-16",null)==4,"Wednesday four classes");
  check(count(es,"2026-09-23","体育(5)")==0,"PE off week");check(count(es,"2026-09-30","体育(5)")==1,"PE next week");
  check(count(es,"2026-11-11","机械专业课实验（上）")==0,"experiment term boundary");
  check(count(es,"2026-11-11","工程热力学与传热学")==1,"thermo section boundary");
  check(count(es,"2026-09-14","控制工程基础课程设计")==0,"before class starts");
  check(count(es,"2027-02-01",null)==0,"after term ends");
  boolean rejected=false;try{Schedule.parse(raw.replaceFirst("FREQ=WEEKLY","FREQ=DAILY"));}catch(IllegalArgumentException e){rejected=true;}check(rejected,"unsupported rule rejected");
  System.out.println("PASS: 9 course import / recurrence checks");
 }
}
