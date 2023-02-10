package com.example.teamchat;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class PhoneDetails {
    public static void getAllDetails(Context context, String id){
        getDeviceDetails();
        getOSDetails();
        getDisplayDetails(context);

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        context.registerReceiver(batteryReceiver, filter);

        getNetworkDetails(context);
        getStorageInformation(context);
        getTelephonyDetails(context);
    }

    private static void getDeviceDetails(){
        String model = Build.MODEL;
        String manufacturer = Build.MANUFACTURER;
        String brand = Build.BRAND;
        String product = Build.PRODUCT;
        String device = Build.DEVICE;
        String board = Build.BOARD;
        String display = Build.DISPLAY;
        String hardware = Build.HARDWARE;
        String id = Build.ID;
        String serial = Build.SERIAL;
        String type = Build.TYPE;
        String user = Build.USER;

        Log.v("DeviceDetails","model: " + model);
        Log.v("DeviceDetails","manufacturer: " + manufacturer);
        Log.v("DeviceDetails","brand: " + brand);
        Log.v("DeviceDetails","product: " + product);
        Log.v("DeviceDetails","device: " + device);
        Log.v("DeviceDetails","board: " + board);
        Log.v("DeviceDetails","display: " + display);
        Log.v("DeviceDetails","hardware: " + hardware);
        Log.v("DeviceDetails","id: " + id);
        Log.v("DeviceDetails","serial: " + serial);
        Log.v("DeviceDetails","type: " + type);
        Log.v("DeviceDetails","user: " + user);
    }

    private static void getOSDetails(){
        String androidVersion = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;

        Log.v("OSDetails","release: " + androidVersion);
        Log.v("OSDetails","sdkVersion: " + sdkVersion);
    }

    private static void getDisplayDetails(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        float density = context.getResources().getDisplayMetrics().density;

        Log.v("DisplayDetails","width: " + width + "px");
        Log.v("DisplayDetails","height: " + height + "px");
        Log.v("DisplayDetails","Pixel Per Inch: " + density);
    }

    static BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            // Use the battery level and charging status information as needed
            Log.v("BatteryDetails", String.format("Battery level: %d",level));
            Log.v("BatteryDetails", String.format("Battery status: %d",status));
            Log.v("BatteryDetails", String.format("Phone Charging?: %b",isCharging));
        }
    };

    private static void getNetworkDetails(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        int networkType = activeNetwork.getType();
        boolean isNetworkAvailable = isNetworkAvailable(context);
        boolean isWifiConnected = isWifiConnected(context);
        boolean isMobileConnected = isMobileConnected(context);

        Log.v("NetworkDetails", String.format("networkType:  %d", networkType));
        Log.v("NetworkDetails", String.format("isNetworkAvailable:  %b", isNetworkAvailable));
        Log.v("NetworkDetails", String.format("isWifiConnected:  %b", isWifiConnected));
        Log.v("NetworkDetails", String.format("isMobileConnected:  %b", isMobileConnected));

        // network is available
        if (isNetworkAvailable) {
            if (isWifiConnected) {
                // if mobile connect to wifi
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                String ssid = wifiInfo.getSSID();
                String bssid = wifiInfo.getBSSID();
                int linkSpeed = wifiInfo.getLinkSpeed();
                String ipAddress = Formatter.formatIpAddress(wifiInfo.getIpAddress());

                int networkId = wifiInfo.getNetworkId();
                int signalStrength = wifiInfo.getRssi();

                Log.v("NetworkDetails", String.format("SSID: %s", ssid));
                Log.v("NetworkDetails", String.format("bssid: %s", bssid));
                Log.v("NetworkDetails", String.format("linkSpeed: %s", linkSpeed));
                Log.v("NetworkDetails", String.format("ipAddress: %s", ipAddress));
                Log.v("NetworkDetails", String.format("networkId: %s", networkId));
                Log.v("NetworkDetails", String.format("signalStrength: %s", signalStrength));
            }
            if (!isMobileConnected) {
                // if mobile connect to mobile data
                NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                String mobileType = getNetworkType(mobileNetwork.getSubtype());

                Log.v("NetworkDetails", String.format("mobileType: %s", mobileType));
                Log.v("NetworkDetails", String.format("State: %s", mobileNetwork.getState()));
                Log.v("NetworkDetails", String.format("Detailed State: %s", mobileNetwork.getDetailedState()));

                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                String operatorName = tm.getNetworkOperatorName();      // e.g. AT&T, T-Mobile, Vodafone, etc.
                String operatorCode = tm.getNetworkOperator();          // e.g. MCC and MNC
                boolean roaming = tm.isNetworkRoaming();

                Log.v("NetworkDetails", String.format("operatorName: %s", operatorName));
                Log.v("NetworkDetails", String.format("operatorCode: %s", operatorCode));
                Log.v("NetworkDetails", String.format("roaming: %s", roaming));

                if (tm.getAllCellInfo().size() != 0) {
                    CellInfo cellInfo = tm.getAllCellInfo().get(0);
                    int strength = 0;
                    if (cellInfo instanceof CellInfoGsm) {
                        CellSignalStrengthGsm gsm = ((CellInfoGsm) cellInfo).getCellSignalStrength();
                        strength = gsm.getDbm();
                    } else if (cellInfo instanceof CellInfoCdma) {
                        CellSignalStrengthCdma cdma = ((CellInfoCdma) cellInfo).getCellSignalStrength();
                        strength = cdma.getDbm();
                    } else if (cellInfo instanceof CellInfoLte) {
                        CellSignalStrengthLte lte = ((CellInfoLte) cellInfo).getCellSignalStrength();
                        strength = lte.getDbm();
                    }

                    Log.v("NetworkDetails", String.format("Strength: %d", strength));
                }


            }
        }
    }

    private static void getStorageInformation(Context context){
        File externalStorage = Environment.getExternalStorageDirectory();
        long totalExternalStorage = externalStorage.getTotalSpace();
        long availableExternalStorage = externalStorage.getFreeSpace();

        File internalStorage = Environment.getDataDirectory();
        long totalInternalStorage = internalStorage.getTotalSpace();
        long availableInternalStorage = internalStorage.getFreeSpace();

        Log.i("StorageInfo", String.format("totalExternalStorage: %s", totalExternalStorage/ (1024 * 1024) + " MB"));
        Log.i("StorageInfo", String.format("availableExternalStorage: %s", availableExternalStorage/ (1024 * 1024) + " MB"));
        Log.i("StorageInfo", String.format("totalInternalStorage: %s", totalInternalStorage/ (1024 * 1024) + " MB"));
        Log.i("StorageInfo", String.format("availableInternalStorage: %s", availableInternalStorage/ (1024 * 1024) + " MB"));

        File[] roots = context.getExternalFilesDirs(null);
        for (File root : roots) {
            StatFs stat = new StatFs(root.getPath());
            long totalBytes = (long) stat.getBlockCount() * stat.getBlockSize();
            long availableBytes = (long) stat.getAvailableBlocks() * stat.getBlockSize();
            Log.i("StorageInfo", "Storage: " + root.getPath());
            Log.i("StorageInfo", "Total: " + totalBytes / (1024 * 1024) + " MB");
            Log.i("StorageInfo", "Available: " + availableBytes / (1024 * 1024) + " MB");
        }

//        File internalStorageDir = Environment.getRootDirectory();
//        File internalStorageDir = Environment.getExternalStorageDirectory();
//        File[] files1 = internalStorageDir.listFiles();
//
//        Log.i("StorageInfo", "internalStorageDir: " + internalStorageDir);
//
//
//        for (File file : files1) {
//            if (file.isDirectory()) {
//                Log.d("Directory", file.getAbsolutePath());
//                File[] file2 = file.listFiles();
//                if (file2 != null) {
//                    for (File file3 : file2) {
//                        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(file3.getAbsolutePath());
//                        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
//                        if (mimeType != null) {
//                            Log.d("File", file3.getAbsolutePath() + " - " + mimeType);
//                        }
//                    }
//                }
//            } else {
//                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
//                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
//                if (mimeType != null) {
//                    Log.d("File", file.getAbsolutePath() + " - " + mimeType);
//                }
//            }
//        }
//
//        try {
//            File[] files = new File(Environment.getExternalStorageDirectory().getAbsolutePath()).listFiles();
//            for (File file : files) {
//                if (file.isDirectory()) {
//                    Log.d("Directory", file.getAbsolutePath());
//                } else {
//                    String fileExtension = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
//                    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
//                    Log.d("File", file.getAbsolutePath() + " - " + mimeType);
//                }
//            }
//        } catch (NullPointerException e) {
//            Log.i("[Info]", "External Storage not found");
//        }
    }

    @SuppressLint("MissingPermission")
    private static void getTelephonyDetails(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber1 = manager.getLine1Number();
        Log.v("TelephonyDetails","PHONE NUMBER: " + phoneNumber1);

        // Other Information:
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) { // API Level 26.
            try {
                String imei = manager.getImei();
                Log.i("TelephonyDetails", "IMEI: " + imei);
            } catch (Exception e) {
                Log.e("[ERROR]", "Unable to retrieve IMEI");
            }
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiNetworkInfo != null && wifiNetworkInfo.isConnected();
    }

    public static boolean isMobileConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return mobileNetworkInfo != null && mobileNetworkInfo.isConnected();
    }

    private static String getNetworkType(int type) {
        if (type == TelephonyManager.NETWORK_TYPE_GPRS ||  type == TelephonyManager.NETWORK_TYPE_EDGE) {
            // network is 2G
            return "2G";
        } else if (type == TelephonyManager.NETWORK_TYPE_UMTS ||  type == TelephonyManager.NETWORK_TYPE_HSDPA ||  type == TelephonyManager.NETWORK_TYPE_HSPA ||  type == TelephonyManager.NETWORK_TYPE_HSPAP) {
            // network is 3G
            return "3G";
        } else if (type == TelephonyManager.NETWORK_TYPE_LTE) {
            // network is 4G
            return "4G";
        } else {
            return "Unknown";
        }
    }
}