package com.example.suiri;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.*;
public final class StoredText {
 static final String PREFIX="\u001fSUIRI_GZIP:";
 public static String pack(String text){if(!text.startsWith(PREFIX)&&text.length()<800)return text;try{ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(GZIPOutputStream zip=new GZIPOutputStream(bytes)){zip.write(text.getBytes(StandardCharsets.UTF_8));}String packed=PREFIX+Base64.getEncoder().encodeToString(bytes.toByteArray());return text.startsWith(PREFIX)||packed.length()<text.length()?packed:text;}catch(IOException e){return text;}}
 public static String unpack(String text){if(!text.startsWith(PREFIX))return text;try(InputStream zip=new GZIPInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(text.substring(PREFIX.length()))))){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[4096];int n;while((n=zip.read(b))!=-1){if(out.size()+n>2_000_000)throw new IOException("record too large");out.write(b,0,n);}return out.toString("UTF-8");}catch(Exception e){throw new IllegalStateException("聊天记录解压失败，原记录保留",e);}}
}
