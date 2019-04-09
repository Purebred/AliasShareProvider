package red.hound.aliasshareprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;


public class AliasShareContentProvider extends ContentProvider {

    public static final String TAG = "AliasShareContentProvider";

    private static UriMatcher m_uriMatcher = buildUriMatcher();
    private static final int ALIASES = 100;
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = AliasShareContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, "country", ALIASES);
        return matcher;
    }

    @Override
    public String getType(Uri uri) {
        final int match = m_uriMatcher.match(uri);
        switch(match){
            case ALIASES:
                return AliasShareContract.AliasEntry.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI : "+ uri);
        }
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    public String getCertificateType(Certificate c) {
        X509Certificate xc = (X509Certificate)c;
        boolean[] ku = xc.getKeyUsage();
        if(null != ku && true == ku[0] && true == ku[2]) {
            return "Device";
        }
        else if(null != ku && true == ku[2]) {
            return "Encryption";
        }

        try {
            Collection<List<?>> subjectAltNames = xc.getSubjectAlternativeNames();
            if(null != subjectAltNames) {
                for (Object subjectAltName : subjectAltNames) {
                    List<?> entry = (List<?>) subjectAltName;
                    if (entry == null || entry.size() < 2) {
                        continue;
                    }
                    Integer altNameType = (Integer) entry.get(0);
                    if (altNameType == null) {
                        continue;
                    }
                    // Look for email then UPN
                    if (altNameType == 1) {
                        return "Digital Signature";
                    }
                    else if (altNameType == 0) {
                        byte[] altName = (byte[])entry.get(1);
                        if (altName != null) {
                            if(altName.length > 32) {
                                return "PIV Authentication";
                            }
                            else {
                                return "Identity";
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e) {
            Log.e("AliasShareProvider", "Failed to read subject alternative name extention: " + e.getMessage());
        }
        return "Other";
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        List l = Arrays.asList(projection);
        boolean wantsAlias = l.contains("alias");
        boolean wantsCert = l.contains("certificate");
        boolean wantsType = l.contains("type");

        String[] columns = null;
        if(wantsAlias && wantsCert && wantsType)
            columns = new String[]{AliasShareContract.COLUMN_ALIAS, AliasShareContract.COLUMN_CERTIFICATE, AliasShareContract.COLUMN_TYPE};
        else if(wantsAlias && wantsCert)
            columns = new String[]{AliasShareContract.COLUMN_ALIAS, AliasShareContract.COLUMN_CERTIFICATE};
        else if(wantsAlias && wantsType)
            columns = new String[]{AliasShareContract.COLUMN_ALIAS, AliasShareContract.COLUMN_TYPE};
        else if(wantsCert && wantsType)
            columns = new String[]{AliasShareContract.COLUMN_CERTIFICATE, AliasShareContract.COLUMN_TYPE};
        else if(wantsCert)
            columns = new String[]{AliasShareContract.COLUMN_CERTIFICATE};
        else if(wantsAlias)
            columns = new String[]{AliasShareContract.COLUMN_ALIAS};
        else
            columns = new String[]{AliasShareContract.COLUMN_TYPE};
        MatrixCursor cursor = new MatrixCursor(columns);

        for(int i = 0; i < projection.length; ++i)
            Log.e("AliasShareProvider", projection[i]);
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            Enumeration e = keyStore.aliases();

            while (e.hasMoreElements()) {
                String alias = (String) e.nextElement();
                Certificate c = keyStore.getCertificate(alias);
                String ct = getCertificateType(c);

                byte[] b = c.getEncoded();
                String b64 = Base64.encodeToString(b, Base64.DEFAULT);
                if(null == projection || 0 == projection.length || (wantsAlias && wantsCert && wantsType))
                    cursor.addRow(new Object[]{alias, b64, ct});
                else if(null == projection || 0 == projection.length || (wantsAlias && wantsCert))
                    cursor.addRow(new Object[]{alias, b64});
                else if(null == projection || 0 == projection.length || (wantsAlias && wantsType))
                    cursor.addRow(new Object[]{alias, ct});
                else if(null == projection || 0 == projection.length || (wantsCert && wantsType))
                    cursor.addRow(new Object[]{b64, ct});
                else if(wantsAlias && !wantsCert && !wantsType)
                    cursor.addRow(new Object[]{alias});
                else if(!wantsAlias && wantsCert && !wantsType)
                    cursor.addRow(new Object[]{b64});
                else if(!wantsAlias && !wantsCert && wantsType)
                    cursor.addRow(new Object[]{ct});
            }
        }
        catch (Exception e2) {
        }

        return cursor;
    }
}