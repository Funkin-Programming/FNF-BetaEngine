package openfl.net;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import org.haxe.extension.Extension;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

/** * @Authors LumiCoder, (FNF BR) and StarNova, (Cream.BR)
 * @version: 0.1.5
**/
public class FileUtils extends Extension {

    private static final int CREATE_FILE_CODE = 1024;
    private static final int PICK_FILE_CODE = 1025;
    private static String contentToSave = "";
    
    public static org.haxe.lime.HaxeObject callbackObject;

    public static void saveFile(final String fileName, final String data) {
        if (data == null || data.isEmpty()) return;

        contentToSave = data;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/json");
                    intent.putExtra(Intent.EXTRA_TITLE, fileName);

                    if (Extension.mainActivity != null) {
                        Extension.mainActivity.startActivityForResult(intent, CREATE_FILE_CODE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void browseFiles(final String mimeType, final org.haxe.lime.HaxeObject callback) {
        callbackObject = callback;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType(mimeType != null ? mimeType : "*/*");

                    if (Extension.mainActivity != null) {
                        Extension.mainActivity.startActivityForResult(intent, PICK_FILE_CODE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_FILE_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    writeFileToUri(uri);
                }
            } else {
                contentToSave = ""; 
            }
            return true;
        }
        
        if (requestCode == PICK_FILE_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Uri uri = data.getData();
                if (uri != null && callbackObject != null) {
                    readBytesFromUri(uri);
                }
            }
            return true;
        }
        
        return false;
    }

    private static void writeFileToUri(final Uri uri) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OutputStream fileOutputStream = Extension.mainActivity.getContentResolver().openOutputStream(uri);
                    
                    if (fileOutputStream != null) {
                        byte[] bytesToWrite = contentToSave.getBytes("UTF-8");
                        
                        fileOutputStream.write(bytesToWrite);
                        fileOutputStream.flush();
                        fileOutputStream.close();
                        
                        contentToSave = ""; 
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void readBytesFromUri(final Uri uri) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream inputStream = Extension.mainActivity.getContentResolver().openInputStream(uri);
                    ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                    
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        byteBuffer.write(buffer, 0, len);
                    }

                    byte[] fileBytes = byteBuffer.toByteArray();
                    String fileName = "file.json";

                    callbackObject.call("onFileSelected", new Object[] { fileBytes, fileName });

                    inputStream.close();
                    byteBuffer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}