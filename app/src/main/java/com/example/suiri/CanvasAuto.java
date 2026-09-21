package com.example.suiri;
import android.content.*;
import android.net.*;
import java.util.concurrent.*;
public final class CanvasAuto {
 static final ExecutorService executor=Executors.newSingleThreadExecutor();static ConnectivityManager.NetworkCallback callback;
 public static synchronized void watch(Context context){Context c=context.getApplicationContext();CanvasJob.ensure(c);if(callback==null){callback=new ConnectivityManager.NetworkCallback(){@Override public void onAvailable(Network n){refresh(c);}};try{c.getSystemService(ConnectivityManager.class).registerDefaultNetworkCallback(callback);}catch(Exception ignored){callback=null;}}refresh(c);}
 public static void refresh(Context c){if(!TaskStore.prefs(c).getBoolean("canvasAuto",true)||!Secrets.has(c,"canvas")||!CanvasSync.due(c)||CanvasSync.running.get())return;long last=TaskStore.prefs(c).getLong("canvasAttempt",0);if(System.currentTimeMillis()-last<15*60*1000)return;ConnectivityManager manager=c.getSystemService(ConnectivityManager.class);NetworkCapabilities net=manager.getNetworkCapabilities(manager.getActiveNetwork());if(net==null||!net.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))return;executor.execute(()->{if(CanvasSync.running.get()||!CanvasSync.due(c))return;try{CanvasSync.run(c,()->!TaskStore.prefs(c).getBoolean("canvasAuto",true));}catch(Exception e){TaskStore.prefs(c).edit().putString("canvasError",e.getMessage()==null?"自动刷新未完成":e.getMessage()).apply();}});}
}
