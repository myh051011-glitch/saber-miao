package com.example.suiri;
import android.app.job.*;
import android.content.*;
import java.util.concurrent.*;
public class CanvasJob extends JobService {
 static final int PERIODIC=4201,NOW=4202;
 static class Run {volatile boolean stopped;final ExecutorService executor=Executors.newSingleThreadExecutor();}
 final ConcurrentHashMap<Integer,Run> runs=new ConcurrentHashMap<>();
 public static void ensure(Context c){JobScheduler scheduler=c.getSystemService(JobScheduler.class);scheduler.cancel(4101);scheduler.cancel(4102);boolean enabled=TaskStore.prefs(c).getBoolean("canvasAuto",true)&&Secrets.has(c,"canvas");if(!enabled){scheduler.cancel(PERIODIC);scheduler.cancel(NOW);return;}
  if(scheduler.getPendingJob(PERIODIC)==null)scheduler.schedule(new JobInfo.Builder(PERIODIC,new ComponentName(c,CanvasJob.class)).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPersisted(true).setPeriodic(60*60*1000L).setBackoffCriteria(60*60*1000L,JobInfo.BACKOFF_POLICY_EXPONENTIAL).build());
  if(CanvasSync.due(c)&&scheduler.getPendingJob(NOW)==null)scheduler.schedule(new JobInfo.Builder(NOW,new ComponentName(c,CanvasJob.class)).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setMinimumLatency(1000).setBackoffCriteria(60*60*1000L,JobInfo.BACKOFF_POLICY_EXPONENTIAL).build());
 }
 @Override public boolean onStartJob(JobParameters params){if(!TaskStore.prefs(this).getBoolean("canvasAuto",true)||!Secrets.has(this,"canvas")||!CanvasSync.due(this))return false;Run run=new Run();runs.put(params.getJobId(),run);run.executor.execute(()->{boolean retry=false;try{CanvasSync.archivePrevious(this);CanvasSync.run(this,()->run.stopped||!TaskStore.prefs(this).getBoolean("canvasAuto",true));}catch(Exception e){retry=true;if(!run.stopped)TaskStore.prefs(this).edit().putString("canvasError",e.getMessage()==null?"后台刷新失败，请手动重试":e.getMessage()).apply();}finally{if(!run.stopped)jobFinished(params,retry);runs.remove(params.getJobId(),run);run.executor.shutdown();}});return true;}
 @Override public boolean onStopJob(JobParameters params){Run run=runs.remove(params.getJobId());if(run!=null){run.stopped=true;run.executor.shutdownNow();}return true;}
 @Override public void onDestroy(){for(Run run:runs.values()){run.stopped=true;run.executor.shutdownNow();}runs.clear();super.onDestroy();}
}
