package io.github.fi4ir.cordova.v2ray;

import android.content.Context;

import java.util.regex.Pattern;

public class Utils {
    public static String packagePath(Context context) {
        String path = context.getFilesDir().toString();
        path = path.replace("files", "");
        return path;
    }

    public static boolean isIpv4Address(String value) {
        return Pattern.matches("^([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])$", value);
    }

    public static boolean isIpv6Address(String value) {
        if (value.indexOf("[") == 0 && value.lastIndexOf("]") > 0) {
            value = value.substring(0, value.length() - 1).substring(1);
        }
        return Pattern.matches("^((?:[0-9A-Fa-f]{1,4}))?((?::[0-9A-Fa-f]{1,4}))*::((?:[0-9A-Fa-f]{1,4}))?((?::[0-9A-Fa-f]{1,4}))*|((?:[0-9A-Fa-f]{1,4}))((?::[0-9A-Fa-f]{1,4})){7}$", value);
    }
}
