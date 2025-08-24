package id.tugas.pos.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionHelper {

    public static final int STORAGE_PERMISSION_REQUEST_CODE = 1001;

    private static final String[] STORAGE_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static boolean hasStoragePermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11+, we can use scoped storage without special permissions for app-specific directory
            return true;
        } else {
            // For Android 10 and below, check traditional storage permissions
            for (String permission : STORAGE_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }
    }

    public static void requestStoragePermissions(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            ActivityCompat.requestPermissions(activity, STORAGE_PERMISSIONS, STORAGE_PERMISSION_REQUEST_CODE);
        }
    }

    public static boolean shouldShowRequestPermissionRationale(Activity activity) {
        for (String permission : STORAGE_PERMISSIONS) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }
        return false;
    }
}
