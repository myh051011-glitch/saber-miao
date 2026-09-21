package com.example.suiri;
public final class Addressing {
 public static String normalize(String s){return s.replace("御主","亲爱的master").replaceAll("(?m)(^|[\\s，。！？：、])主人(?=$|[\\s，。！？：、您你请的])", "$1亲爱的master");}
}
