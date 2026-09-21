package com.example.suiri;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import org.json.*;
public final class ChatStream {
 public interface Update{void text(String s);}
 public static final class Result{public String text,finish;public long tokens;}
 public static String publicText(String text){java.util.regex.Matcher m=java.util.regex.Pattern.compile("\\{\\s*\"(?:memory_updates|note_change|task_actions|task)\"\\s*:").matcher(text);return (m.find()?text.substring(0,m.start()):text).replace("```json","").trim();}
 public static Result send(String key,String model,String effort,JSONArray messages,Update update)throws Exception{
  HttpURLConnection c=(HttpURLConnection)new URL("https://api.deepseek.com/chat/completions").openConnection();c.setInstanceFollowRedirects(false);c.setConnectTimeout(15000);c.setReadTimeout(90000);c.setRequestMethod("POST");c.setDoOutput(true);c.setRequestProperty("Authorization","Bearer "+key);c.setRequestProperty("Content-Type","application/json");
  JSONObject body=new JSONObject().put("model",model).put("messages",messages).put("reasoning_effort",ThinkingMode.valid(effort)).put("stream",true).put("stream_options",new JSONObject().put("include_usage",true)).put("max_tokens",ThinkingMode.tokens(effort));
  try{try(OutputStream out=c.getOutputStream()){out.write(body.toString().getBytes(StandardCharsets.UTF_8));}int code=c.getResponseCode();if(code!=200)throw new IOException(code==401?"DeepSeek Key 无效，请在设置中更新":code==402?"DeepSeek 余额不足":code==429?"请求频率或额度受限，请稍后重试":"DeepSeek 返回 HTTP "+code+"；请核对模型是否支持所选附件");Result result=new Result();StringBuilder answer=new StringBuilder();boolean ended=false;try(BufferedReader in=new BufferedReader(new InputStreamReader(c.getInputStream(),StandardCharsets.UTF_8))){String line;while((line=in.readLine())!=null){if(Thread.currentThread().isInterrupted())throw new IOException("请求已中断，可重试");if(!line.startsWith("data:"))continue;String value=line.substring(5).trim();if(value.equals("[DONE]")){ended=true;break;}JSONObject chunk=new JSONObject(value);JSONObject usage=chunk.optJSONObject("usage");if(usage!=null)result.tokens=usage.optLong("total_tokens");JSONArray choices=chunk.optJSONArray("choices");if(choices==null||choices.length()==0)continue;JSONObject choice=choices.getJSONObject(0),delta=choice.optJSONObject("delta");if(delta!=null&&delta.has("reasoning_content")&&answer.length()==0)update.text("正在分析… 可在右上角切换思考强度");if(delta!=null&&!delta.isNull("content")){answer.append(delta.optString("content"));if(answer.length()>100000)throw new IOException("回答超过本次接收上限");update.text(publicText(answer.toString()));}if(!choice.isNull("finish_reason"))result.finish=choice.optString("finish_reason");}}
   if(!ended&&result.finish==null)throw new IOException("回答连接中断，请重试");if(answer.length()==0)throw new IOException("模型未返回正文，可能已耗尽推理输出额度；可切换较浅档位重试");result.text=answer.toString();if("length".equals(result.finish))result.text+="\n（本次回答达到长度上限，可输入“继续”）";return result;
  }finally{c.disconnect();}
 }
}
