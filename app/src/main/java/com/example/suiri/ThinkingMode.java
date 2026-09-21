package com.example.suiri;
public final class ThinkingMode {
 public static String valid(String mode){return java.util.Arrays.asList("none","low","high","max").contains(mode)?mode:"none";}
 public static String label(String mode){switch(valid(mode)){case "low":return "轻度思考";case "high":return "深入思考";case "max":return "最高强度";default:return "快速回答";}}
 public static int tokens(String mode){switch(valid(mode)){case "low":return 6000;case "high":return 16000;case "max":return 32000;default:return 4000;}}
}
