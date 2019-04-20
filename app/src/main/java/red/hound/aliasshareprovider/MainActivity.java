package red.hound.aliasshareprovider;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.util.Enumeration;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private byte[] getBytesForFile(int resId) {
        InputStream ins = getApplicationContext().getResources().openRawResource(resId);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int size;
        byte[] buffer = new byte[1024];
        try {
            while ((size = ins.read(buffer, 0, 1024)) >= 0) {
                outputStream.write(buffer, 0, size);
            }
            ins.close();
            buffer = outputStream.toByteArray();
            return buffer;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private int[] getResourceIdArray(int arrayResId) {
        TypedArray ar = getApplicationContext().getResources().obtainTypedArray(arrayResId);
        int len = ar.length();
        int[] resIds = new int[len];
        for (int i = 0; i < len; i++) {
            resIds[i] = ar.getResourceId(i, 0);
        }
        ar.recycle();
        return resIds;
    }

    public void importSamplePkcs12(View view) {
        KeyStore keyStore = null;
        char[] password = "password".toCharArray();
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            int[] image_keysIds = getResourceIdArray(R.array.image_keys);
            for (int resId : image_keysIds) {
                KeyStore p12Store = KeyStore.getInstance("PKCS12");
                byte[] p12 = getBytesForFile(resId);
                InputStream stream = new ByteArrayInputStream(p12);
                p12Store.load(stream, password);

                Enumeration e = p12Store.aliases();
                while (e.hasMoreElements()) {
                    String keyStoreAlias = (String) e.nextElement();
                    Key k = p12Store.getKey(keyStoreAlias, password);

                    keyStore.setKeyEntry(keyStoreAlias, k, null, p12Store.getCertificateChain(keyStoreAlias));
                    Log.v(TAG, "Installed PKCS #12 with alias " + keyStoreAlias);
                }
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Failed to load AndroidKeyStore", e);
        }
    }

    public void clearKeyStore(View view)
    {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            Enumeration e = keyStore.aliases();
            while (e.hasMoreElements()) {
                String alias = (String) e.nextElement();
                Log.i(TAG, "Deleting key with alias: " + alias);
                keyStore.deleteEntry(alias);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void simulateBroadcast(View view)
    {
        String broadcastType = "red.hound.purebred.broadcast.UPDATE_COMPLETED";
        //String broadcastType = "red.hound.purebred.broadcast.RECOVERY_COMPLETED";
        //String broadcastType = "red.hound.purebred.broadcast.UPDATE_COMPLETED";
        Intent intent = new Intent();
        intent.setAction(broadcastType);

        PackageManager pm=getApplicationContext().getPackageManager();
        List<ResolveInfo> matches=pm.queryBroadcastReceivers(intent, 0);

        for (ResolveInfo resolveInfo : matches) {
            Intent explicit=new Intent(broadcastType);
            ComponentName cn=
                    new ComponentName(resolveInfo.activityInfo.applicationInfo.packageName,
                            resolveInfo.activityInfo.name);

            explicit.setComponent(cn);
            getApplicationContext().sendBroadcast(explicit);
        }

        /*
        Intent intent = new Intent();
        intent.setAction(broadcastType);
        sendBroadcast(intent);
        */
    }
}
