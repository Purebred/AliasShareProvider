package red.hound.aliasshareprovider;

import android.net.Uri;
import android.provider.BaseColumns;

public class AliasShareContract {
    //content://red.hound.android.samplekeyprovider/alias
    public static final String CONTENT_AUTHORITY = "red.hound.aliasshareprovider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String COLUMN_ALIAS = "alias";
    public static final String COLUMN_CERTIFICATE = "certificate";
    public static final String COLUMN_TYPE = "type";

    public static final class AliasEntry implements BaseColumns {

        public static final String TABLE_NAME = "aliases";

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(TABLE_NAME).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd."+CONTENT_AUTHORITY+".aliases";
    }
}
