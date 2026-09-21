package com.example.suiri;
import android.app.job.*;
import java.util.concurrent.*;
public class WeatherJob extends JobService {
 final ConcurrentHashMap<Integer,Thread> runs=new ConcurrentHashMap<>();
 public boolean onStartJob(JobParameters p){Thread t=new Thread(()->{try{Weather.sync(this);}finally{if(runs.remove(p.getJobId())!=null)jobFinished(p,false);}},"saber-weather");runs.put(p.getJobId(),t);t.start();return true;}
 public boolean onStopJob(JobParameters p){Thread t=runs.remove(p.getJobId());if(t!=null)t.interrupt();return true;}
}
