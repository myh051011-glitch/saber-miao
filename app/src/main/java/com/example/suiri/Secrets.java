package com.example.suiri;
import android.content.Context;
import android.security.keystore.*;
import android.util.Base64;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.security.KeyStore;
import java.nio.charset.StandardCharsets;

public final class Secrets {
 static synchronized javax.crypto.SecretKey key()throws Exception{KeyStore s=KeyStore.getInstance("AndroidKeyStore");s.load(null);if(!s.containsAlias("suiri.secrets")){KeyGenerator g=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");g.init(new KeyGenParameterSpec.Builder("suiri.secrets",KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());g.generateKey();}return (javax.crypto.SecretKey)s.getKey("suiri.secrets",null);}
 public static void set(Context c,String name,String value)throws Exception{if(value.isEmpty()){TaskStore.prefs(c).edit().remove("secret."+name).commit();return;}Cipher f=Cipher.getInstance("AES/GCM/NoPadding");f.init(Cipher.ENCRYPT_MODE,key());byte[] encrypted=f.doFinal(value.getBytes(StandardCharsets.UTF_8));String text=Base64.encodeToString(f.getIV(),Base64.NO_WRAP)+":"+Base64.encodeToString(encrypted,Base64.NO_WRAP);if(!TaskStore.prefs(c).edit().putString("secret."+name,text).commit())throw new Exception("密钥保存失败");}
 public static String get(Context c,String name)throws Exception{String text=TaskStore.prefs(c).getString("secret."+name,"");if(text.isEmpty())return "";String[] p=text.split(":",2);Cipher f=Cipher.getInstance("AES/GCM/NoPadding");f.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,Base64.decode(p[0],Base64.NO_WRAP)));return new String(f.doFinal(Base64.decode(p[1],Base64.NO_WRAP)),StandardCharsets.UTF_8);}
 public static boolean has(Context c,String name){return TaskStore.prefs(c).contains("secret."+name);}
}
