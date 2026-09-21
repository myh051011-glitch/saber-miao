package com.example.suiri;
import android.app.*;
import android.content.*;
import android.view.*;
import android.widget.*;
/** Shared styling, preserving each editor's existing validation and listeners. */
public final class UiDialog {
 public static class Builder extends AlertDialog.Builder {
  public Builder(Context c){super(c,R.style.DialogTheme);}
  @Override public AlertDialog create(){AlertDialog d=super.create();View decor=d.getWindow().getDecorView();decor.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener(){public void onViewAttachedToWindow(View v){v.post(()->style(d));}public void onViewDetachedFromWindow(View v){}});return d;}
 }
 static int dp(Context c,int n){return (int)(c.getResources().getDisplayMetrics().density*n);}
 static void style(AlertDialog d){if(d.getWindow()==null)return;Context c=d.getContext();d.getWindow().setBackgroundDrawable(new android.graphics.drawable.InsetDrawable(Saber.shape(c,Saber.SURFACE,26,Saber.LINE),dp(c,12)));walk(d.getWindow().getDecorView());}
 static void walk(View v){Context c=v.getContext();if(v instanceof EditText){EditText e=(EditText)v;e.setTextColor(Saber.INK);e.setHintTextColor(Saber.MUTED);e.setBackground(Saber.shape(c,Saber.BG,16,Saber.LINE));e.setPadding(dp(c,12),dp(c,10),dp(c,12),dp(c,10));e.setMinimumHeight(dp(c,48));}else if(v instanceof Button && !(v instanceof CompoundButton)){Button b=(Button)v;b.setTextColor(Saber.GOLD);b.setAllCaps(false);b.setBackground(Saber.shape(c,Saber.BG,16,0));b.setPadding(dp(c,12),dp(c,8),dp(c,12),dp(c,8));}else if(v instanceof TextView)((TextView)v).setTextColor(Saber.INK);if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)walk(g.getChildAt(i));}}
}
