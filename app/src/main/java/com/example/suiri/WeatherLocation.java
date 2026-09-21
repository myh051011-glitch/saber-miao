package com.example.suiri;
import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.location.*;
import android.os.*;
import java.util.*;
public final class WeatherLocation {
 public static final int REQUEST=4310;
 private final MainActivity a;private LocationListener listener;
 WeatherLocation(MainActivity a){this.a=a;}
 static boolean permitted(Context c){return c.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED||c.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED;}
 void start(boolean ask){stop();if(!a.prefs.getBoolean("weatherLocation",true))return;if(!permitted(a)){if(ask)a.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},REQUEST);return;}
  LocationManager manager=a.getSystemService(LocationManager.class);listener=new LocationListener(){public void onLocationChanged(Location l){accept(l);}public void onProviderEnabled(String s){}public void onProviderDisabled(String s){}public void onStatusChanged(String s,int n,Bundle b){}};
  boolean any=false;for(String provider:new String[]{LocationManager.NETWORK_PROVIDER,LocationManager.GPS_PROVIDER})try{if(manager.isProviderEnabled(provider)){manager.requestLocationUpdates(provider,300000,1500,listener,Looper.getMainLooper());Location cached=manager.getLastKnownLocation(provider);if(cached!=null)accept(cached);any=true;}}catch(SecurityException ignored){}
  if(ask)a.toast(any?"正在定位，首次获取可能需要片刻":"请先开启手机定位服务");
 }
 void stop(){if(listener!=null){try{a.getSystemService(LocationManager.class).removeUpdates(listener);}catch(Exception ignored){}listener=null;}}
 void accept(Location location){if(!a.foreground||!a.prefs.getBoolean("weatherLocation",true))return;long age=System.currentTimeMillis()-location.getTime();if(age<0||age>600000||location.hasAccuracy()&&location.getAccuracy()>20000)return;
  String lat=WeatherRules.coordinate(Double.toString(location.getLatitude()),true),lon=WeatherRules.coordinate(Double.toString(location.getLongitude()),false);boolean changed=!lat.equals(a.prefs.getString("weatherLat",""))||!lon.equals(a.prefs.getString("weatherLon",""))||!a.prefs.getBoolean("weatherLocated",false);
  SharedPreferences.Editor edit=a.prefs.edit().putLong("weatherLocationTime",location.getTime()).putBoolean("weatherLocated",true);
  if(changed){edit.putString("weatherLat",lat).putString("weatherLon",lon).putString("weatherCity","所在地").putString("weatherRegion","").putBoolean("weatherGeoNeeded",true).remove("weatherGeoAttempt").remove("weatherNotified").putLong("weatherRevision",System.currentTimeMillis());for(String k:Weather.KINDS)edit.remove("weather."+k).remove("weather.time."+k).remove("weather.attempt."+k);}
  edit.putLong("weatherUpdated",System.currentTimeMillis()).apply();Weather.ensure(a);if(changed)SuiRiWidget.refresh(a);
 }
 public static boolean shanghai(String region){return "上海".equals(region)||"上海市".equals(region)||"Shanghai".equalsIgnoreCase(region);}
}
