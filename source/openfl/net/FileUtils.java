package openfl.net;

import android.content.Intent;
import android.net.Uri;
import android.app.Activity;
import org.haxe.extension.Extension;
import java.io.FileOutputStream;
import android.os.ParcelFileDescriptor;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class FileUtils extends Extension {

    private static final String PREFS_NAME = "OpenFLFileSave";
    private static final String DATA_KEY = "last_data";

    public static void saveFile(String name, String data) {
        if (data == null || data.isEmpty()) {
            Log.e("OPENFL", ">>> JAVA: Erro - Dados recebidos estao vazios!");
            return;
        }

        Log.i("OPENFL", ">>> JAVA: saveFile chamado. Recebido: " + data.length() + " bytes.");

        SharedPreferences prefs = Extension.mainActivity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(DATA_KEY, data).apply();

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json"); 
        intent.putExtra(Intent.EXTRA_TITLE, name);
        
        Extension.mainActivity.startActivityForResult(intent, 1024);
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1024) {
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                Log.i("OPENFL", ">>> JAVA: Local de salvamento escolhido. Gravando...");
                writeToUri(data.getData());
            } else {
                Log.w("OPENFL", ">>> JAVA: O usuario cancelou o salvamento.");
            }
            return true;
        }
        return false;
    }

    private static void writeToUri(Uri uri) {
        try {
            SharedPreferences prefs = Extension.mainActivity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String content = prefs.getString(DATA_KEY, "");

            if (content.isEmpty()) {
                Log.e("OPENFL", ">>> JAVA: Erro critico - Os dados sumiram do cache!");
                return;
            }

            ParcelFileDescriptor pfd = Extension.mainActivity.getContentResolver().openFileDescriptor(uri, "rwt");
            
            if (pfd != null) {
                FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor());
                byte[] bytes = content.getBytes("UTF-8");
                
                fos.write(bytes);
                fos.flush();
                
                pfd.getFileDescriptor().sync(); 
                
                fos.close();
                pfd.close();

                Log.i("OPENFL", ">>> JAVA: SALVO COM SUCESSO! Finalizado: " + bytes.length + " bytes gravados.");
                
                prefs.edit().remove(DATA_KEY).apply();
            }
        } catch (Exception e) {
            Log.e("OPENFL", ">>> JAVA: Erro ao gravar arquivo: " + e.toString());
        }
    }
}