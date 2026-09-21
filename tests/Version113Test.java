package com.example.suiri;
import java.io.*;
import java.util.*;
import java.util.zip.*;
public class Version113Test {
 static int n;static void check(boolean b){n++;if(!b)throw new AssertionError("1.1.3 check "+n);}
 public static void main(String[] args)throws Exception{
  check(WeatherLocation.shanghai("上海市"));check(WeatherLocation.shanghai("Shanghai"));check(!WeatherLocation.shanghai("江苏省"));check(!WeatherLocation.shanghai(""));
  String source="# 学习计划\n- 周一复习机械设计\n- A < B & C > D\n# 实施步骤\n- 每天学习45分钟\n- 结束后复盘";
  List<PptxExporter.Slide> slides=PptxExporter.parse(source);check(slides.size()==2);check(slides.get(0).title.equals("学习计划"));check(slides.get(1).lines.size()==2);
  StringBuilder longText=new StringBuilder("# 长回答\n");for(int i=0;i<35;i++)longText.append("第").append(i).append("项学习任务\n");List<PptxExporter.Slide> many=PptxExporter.parse(longText.toString());check(many.size()==4);check(many.stream().mapToInt(s->s.lines.size()).sum()==35);
  ByteArrayOutputStream bytes=new ByteArrayOutputStream();PptxExporter.write(source,bytes);Map<String,String> entries=new HashMap<>();try(ZipInputStream z=new ZipInputStream(new ByteArrayInputStream(bytes.toByteArray()))){ZipEntry e;while((e=z.getNextEntry())!=null){ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] buf=new byte[4096];int count;while((count=z.read(buf))!=-1)b.write(buf,0,count);entries.put(e.getName(),b.toString("UTF-8"));}}check(entries.containsKey("[Content_Types].xml"));check(entries.containsKey("ppt/slides/slide2.xml"));check(entries.get("ppt/slides/slide1.xml").contains("&lt; B &amp; C &gt;"));check(entries.get("ppt/presentation.xml").contains("screen16x9"));check(entries.containsKey("ppt/theme/theme1.xml"));boolean rejected=false;try{PptxExporter.parse(" ");}catch(IOException expected){rejected=true;}check(rejected);
  if(args.length>0)try(OutputStream out=new FileOutputStream(args[0])){PptxExporter.write(source,out);}System.out.println("PASS: "+n+" location region and editable PPTX checks");
 }
}
