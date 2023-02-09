package com.example.teamchat;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class PhoneDetails {
    public static void getAllDetails(Context context, String id){
        getDeviceDetails();
        getOSDetails();
        getDisplayDetails(context);

        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        context.registerReceiver(batteryReceiver, filter);

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

    private static void getBatteryDetails(Context context){

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
            Log.v("BatteryDetails", String.format("Phone Charging? %b",isCharging));
        }
    };

    @SuppressLint("MissingPermission")
    private static void getTelephonyDetails(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber1 = manager.getLine1Number();
        Log.v("TelephonyDetails","PHONE NUMBER: " + phoneNumber1);

        // Other Informations:
//        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) { // API Level 26.
//            String imei = manager.getImei();
//            int phoneCount = manager.getPhoneCount();
//
//            Log.i("","Phone Count: ");
//            Log.i("","EMEI: " + imei);
//        }
    }
}
