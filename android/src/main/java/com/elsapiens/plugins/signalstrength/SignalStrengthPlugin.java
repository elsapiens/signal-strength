package com.elsapiens.plugins.signalstrength;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.telephony.*;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.getcapacitor.annotation.Permission;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@CapacitorPlugin(name = "SignalStrength", permissions = {
        @Permission(alias = "fineLocation", strings = { Manifest.permission.ACCESS_FINE_LOCATION }),
        @Permission(alias = "coarseLocation", strings = { Manifest.permission.ACCESS_COARSE_LOCATION }),
        @Permission(alias = "phoneState", strings = { Manifest.permission.READ_PHONE_STATE }),
        @Permission(alias = "networkState", strings = { Manifest.permission.ACCESS_NETWORK_STATE }),
        @Permission(alias = "phoneNumbers", strings = { Manifest.permission.READ_PHONE_NUMBERS }),
        @Permission(alias = "wifiState", strings = { Manifest.permission.ACCESS_WIFI_STATE }),
})
public class SignalStrengthPlugin extends Plugin {
    private TelephonyManager telephonyManager;
    private String requestedTechnology;
    private PhoneStateListener phoneStateListener;
    private ScheduledExecutorService scheduler;
    private long lastUpdateTime = 0;
    private boolean registerCellInfoListenerRunning = false;
    private boolean isNetworkSpeedMonitoring = false;
    private static final String TAG = "SignalStrength";
    private MyTelephonyCallback telephonyCallback;
    private String currentCallId;
    @Override
    protected void handleOnStart() {
        super.handleOnStart();
    }
    @Override
    protected void handleOnStop() {
        super.handleOnStop();
        unregisterCellInfoListener();
    }
    @SuppressLint("MissingPermission")
    @PluginMethod
    public void startMonitoring(PluginCall call) {
        requestedTechnology = call.getString("technology", "all");
        if (isMissingRequiredPermissions(getContext())) {
            requestPermissions();
            call.reject("Required permissions are missing");
            return;
        }
        registerCellInfoListener();
        call.resolve();
    }
    @PluginMethod
    public void stopMonitoring(PluginCall call) {
        if (scheduler != null) {
            scheduler.shutdown();
            scheduler = null;
        }
        unregisterCellInfoListener();
        registerCellInfoListenerRunning = false;
        call.resolve();
    }

    private void registerCellInfoListener() {
        if (registerCellInfoListenerRunning) {
            return;
        } else if (isMissingRequiredPermissions(getContext())) {
            requestPermissions();
            return;
        }
        getCurrentNetworkSpeed();
        Context context = getContext();
        if (context != null) {
            telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (telephonyCallback == null) {
                telephonyCallback = new MyTelephonyCallback();
            }
            assert context != null;
            telephonyManager.registerTelephonyCallback(context.getMainExecutor(), telephonyCallback);
        } else {
            phoneStateListener = new PhoneStateListener() {
                @Override
                public void onCellInfoChanged(List<CellInfo> cellInfoList) {
                    Log.d(TAG, "onCellInfoChanged triggered (Legacy API)");
                    handleCellInfoChanged(cellInfoList);
                }
            };
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CELL_INFO);
        }
        registerCellInfoListenerRunning = true;
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay(new SignalStrengthTask(), 0, 1, TimeUnit.SECONDS); // Every 1 second
    }

    private class SignalStrengthTask implements Runnable {
        private long lastUpdateTime = 0;

        @Override
        public void run() {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastUpdateTime >= 1000) { // Ensure at least 1 second has passed
                lastUpdateTime = currentTime;
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        telephonyManager.requestCellInfoUpdate(getContext().getMainExecutor(), new TelephonyManager.CellInfoCallback() {
                            @Override
                            public void onCellInfo(@NonNull List<CellInfo> cellInfoList) {
                                handleCellInfoChanged(cellInfoList);
                            }
                        });
                    }
                }
            }
        }
    }

    private void handleCellInfoChanged(List<CellInfo> cellInfoList) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < 1000) {
            return; // Skip if less than 1 second has passed
        }
        lastUpdateTime = currentTime;

        if (cellInfoList != null && !cellInfoList.isEmpty()) {
            JSONArray neighboringCells = new JSONArray();
            JSONObject currentCellData = new JSONObject();

            for (CellInfo cellInfo : cellInfoList) {
                if (cellInfo instanceof CellInfoGsm) {
                    processGsmCell((CellInfoGsm) cellInfo, currentCellData, neighboringCells);
                } else if (cellInfo instanceof CellInfoWcdma) {
                    processWcdmaCell((CellInfoWcdma) cellInfo, currentCellData, neighboringCells);
                } else if (cellInfo instanceof CellInfoLte) {
                    processLteCell((CellInfoLte) cellInfo, currentCellData, neighboringCells);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cellInfo instanceof CellInfoNr) {
                    processNrCell((CellInfoNr) cellInfo, currentCellData, neighboringCells);
                }
            }

            try {
                JSONObject result = new JSONObject();
                if (!Objects.equals(requestedTechnology, "all") && !Objects.equals(requestedTechnology, getNetworkType())) {
                    result.put("status", "error");
                    result.put("message", "Not connected on the requested network type " + requestedTechnology);
                } else {
                    result.put("status", "success");
                    putGeneralData(result);
                    result.put("currentCell", currentCellData);
                    result.put("neighboringCells", neighboringCells);
                }
                notifyListeners("signalUpdate", new JSObject().put("data", result));
            } catch (JSONException ignored) {}
        }
    }

    private void unregisterCellInfoListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && telephonyCallback != null) {
            telephonyManager.unregisterTelephonyCallback(telephonyCallback);
            telephonyCallback = null;
            registerCellInfoListenerRunning = false;
        } else if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
            phoneStateListener = null;
            registerCellInfoListenerRunning = false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private class MyTelephonyCallback extends TelephonyCallback implements TelephonyCallback.CellInfoListener {
        @Override
        public void onCellInfoChanged(@NonNull List<CellInfo> cellInfoList) {
            Log.d(TAG, "onCellInfoChanged triggered (API 31+)");
            handleCellInfoChanged(cellInfoList);
        }
    }

    private boolean isMissingRequiredPermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        if (getActivity() != null) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.READ_PHONE_NUMBERS,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.ANSWER_PHONE_CALLS,
                    Manifest.permission.MODIFY_PHONE_STATE,
                    Manifest.permission.CALL_PRIVILEGED,
            }, 1);
        }
    }
    @SuppressLint("MissingPermission")
    private String getNetworkType() {
        int networkType = telephonyManager.getDataNetworkType();
        return switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE,
                 TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT,
                 TelephonyManager.NETWORK_TYPE_IDEN, TelephonyManager.NETWORK_TYPE_GSM -> "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0,
                 TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_EVDO_B,
                 TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA,
                 TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EHRPD,
                 TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "3G";
            case TelephonyManager.NETWORK_TYPE_LTE, TelephonyManager.NETWORK_TYPE_IWLAN -> "4G";
            case TelephonyManager.NETWORK_TYPE_NR -> "5G";
            case TelephonyManager.NETWORK_TYPE_UNKNOWN -> "UNKNOWN";
            default -> "UNKNOWN";
        };
    }
    @PluginMethod
    public void openNetworkSettings(PluginCall call) {
        Context context = getContext();
        Intent intent = new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
        context.startActivity(intent);
        call.resolve();
    }
    @PluginMethod
    public void openWifiSettings(PluginCall call) {
        Context context = getContext();
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        context.startActivity(intent);
        call.resolve();
    }
    @PluginMethod
    public void isMultiSim(PluginCall call) {
        JSObject result = new JSObject();
        result.put("isMultiSim", isMultiSim());
        call.resolve(result);
    }
    @PluginMethod
    public void getActiveSIMCount(PluginCall call) {
        JSObject result = new JSObject();
        result.put("simCount", getActiveSubscriptionCount());
        call.resolve(result);
    }
    @PluginMethod
    public void makeCall(PluginCall call) {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestCallPermission(getActivity());
            call.reject("Call permission is missing");
            return;
        }

        String number = call.getString("number");
        if (number == null || number.isEmpty()) {
            call.reject("Phone number is missing");
            return;
        }

        TelecomManager telecomManager = (TelecomManager) getContext().getSystemService(Context.TELECOM_SERVICE);
        if (telecomManager != null) {
            Uri uri = Uri.fromParts("tel", number, null);
            Bundle extras = new Bundle();
            extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, true);
            telecomManager.placeCall(uri, extras);
            currentCallId = uri.toString();
            call.resolve();
        } else {
            call.reject("TelecomManager is not available");
        }
    }
    @PluginMethod
    public void disconnectCall(PluginCall call) {
        if (currentCallId == null) {
            call.reject("No active call to disconnect");
            return;
        }

        TelecomManager telecomManager = (TelecomManager) getContext().getSystemService(Context.TELECOM_SERVICE);
        if (telecomManager != null) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions();
                return;
            }
            telecomManager.endCall(); // End the current call
            currentCallId = null; // Clear the call ID
            call.resolve();
        } else {
            call.reject("TelecomManager is not available");
        }
    }
    public void requestCallPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.ANSWER_PHONE_CALLS,
                    Manifest.permission.READ_PHONE_STATE
            }, 1);
        }
    }
    /**
     * check if more than one SIM card is present
     */
    private boolean isMultiSim() {
        return telephonyManager.getPhoneCount() > 1;
    }
    private int getActiveSubscriptionCount() {
        Context context = getContext();
        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        if (subscriptionManager != null) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return -1;
            }
            List<SubscriptionInfo> activeSubscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
            if (activeSubscriptionInfoList != null) {
                return activeSubscriptionInfoList.size(); // Number of active SIMs
            }
        }
        return 0;
    }

    private boolean isOnCall() {
        return telephonyManager.getCallState() != TelephonyManager.CALL_STATE_IDLE;
    }
    private void putGeneralData(JSONObject result) {
        try {
            result.put("isMultiSim", isMultiSim());
        } catch (JSONException ignored) {}
        try {
            result.put("simCount", getActiveSubscriptionCount());
        } catch (JSONException ignored) {}
        try {
            result.put("isOnCall", isOnCall());
        } catch (JSONException ignored) {}

        String networkType = getConnectionType(getContext());
        try {
            result.put("networkType", networkType);
        } catch (JSONException ignored){}

        try {
            if(!networkType.equals("No Connection")){
                result.put("speed", new JSObject().put("download", downloadSpeedKbps).put("upload", uploadSpeedKbps));
            }
        }catch (JSONException ignored){}
    }
    private void processGsmCell(CellInfoGsm cellInfo, JSONObject currentCellData, JSONArray neighboringCells) {
        CellIdentityGsm cell = cellInfo.getCellIdentity();
        CellSignalStrengthGsm signal = cellInfo.getCellSignalStrength();

        try {
            if (cellInfo.isRegistered()) {
                currentCellData.put("type", "GSM");
                currentCellData.put("cid", cell.getCid());
                currentCellData.put("lac", cell.getLac());
                currentCellData.put("mcc", cell.getMccString());
                currentCellData.put("mnc", cell.getMncString());
                currentCellData.put("arfcn", cell.getArfcn());
                currentCellData.put("level", signal.getDbm());
                currentCellData.put("asuLevel", signal.getAsuLevel());
                currentCellData.put("dbm", signal.getDbm());
                currentCellData.put("aa", signal.getAsuLevel());
            } else {
                JSONObject neighbor = new JSONObject();
                neighbor.put("cid", cell.getCid());
                neighbor.put("lac", cell.getLac());
                neighbor.put("arfcn", cell.getArfcn());
                neighbor.put("level", signal.getLevel());
                neighbor.put("asulevel", signal.getAsuLevel());
                neighbor.put("dbm", signal.getDbm());
                neighboringCells.put(neighbor);
            }
        } catch (Exception ignored) {
        }
    }

    private void processWcdmaCell(CellInfoWcdma cellInfo, JSONObject currentCellData, JSONArray neighboringCells) {
        CellIdentityWcdma cell = cellInfo.getCellIdentity();
        CellSignalStrengthWcdma signal = cellInfo.getCellSignalStrength();

        try {
            if (cellInfo.isRegistered()) {
                currentCellData.put("type", "WCDMA");

                currentCellData.put("cid", cell.getCid());
                currentCellData.put("eNodeBId", cell.getCid());
                currentCellData.put("PSC", cell.getPsc());
                currentCellData.put("mcc", cell.getMccString());
                currentCellData.put("mnc", cell.getMncString());
                currentCellData.put("lac", cell.getLac());
                currentCellData.put("uarfcn", cell.getUarfcn());
                currentCellData.put("dbm", signal.getDbm());
                currentCellData.put("asulevel", signal.getAsuLevel());
                currentCellData.put("level", signal.getLevel());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    currentCellData.put("ecno", signal.getEcNo());
                }
            } else {
                JSONObject neighbor = new JSONObject();
                neighbor.put("cid", cell.getCid());
                neighbor.put("lac", cell.getLac());
                neighbor.put("mcc", cell.getMccString());
                neighbor.put("mnc", cell.getMncString());
                neighbor.put("uarfcn", cell.getUarfcn());
                neighbor.put("dbm", signal.getDbm());
                neighbor.put("level", signal.getLevel());
                neighbor.put("asulevel", signal.getAsuLevel());
                neighboringCells.put(neighbor);
            }
        } catch (Exception ignored) {

        }
    }

    private void processLteCell(CellInfoLte cellInfo, JSONObject currentCellData, JSONArray neighboringCells) {
        CellIdentityLte cell = cellInfo.getCellIdentity();
        CellSignalStrengthLte signal = cellInfo.getCellSignalStrength();

        try {
            if (cellInfo.isRegistered()) {
                currentCellData.put("type", "LTE");
                currentCellData.put("asulevel", signal.getAsuLevel());
                currentCellData.put("level", signal.getLevel());
                currentCellData.put("dbm", signal.getDbm());
                currentCellData.put("cqi", signal.getCqi());
                currentCellData.put("rsrq", signal.getRsrq());
                currentCellData.put("rsrp", signal.getRsrp());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    currentCellData.put("rsssi", signal.getRssi());
                }
                currentCellData.put("sinr", signal.getRssnr());
                currentCellData.put("cid", cell.getCi());
                currentCellData.put("tac", cell.getTac());
                currentCellData.put("mcc", cell.getMccString());
                currentCellData.put("mnc", cell.getMncString());
                currentCellData.put("pci", cell.getPci());
                currentCellData.put("earfcn", cell.getEarfcn());
                currentCellData.put("frequency", cell.getEarfcn());
                currentCellData.put("band", cell.getBandwidth());
                currentCellData.put("operator", cell.getOperatorAlphaLong());

                currentCellData.put("arfcn", cell.getEarfcn());
            } else {
                JSONObject neighbor = new JSONObject();
                neighbor.put("cid", cell.getCi());
                neighbor.put("tac", cell.getTac());
                neighbor.put("pci", cell.getPci());
                neighbor.put("arfcn", cell.getEarfcn());
                neighbor.put("dbm", signal.getDbm());
                neighbor.put("level", signal.getLevel());
                neighbor.put("asulevel", signal.getAsuLevel());
                neighboringCells.put(neighbor);
            }
        } catch (Exception ignored) {
        }
    }

    private void processNrCell(CellInfoNr cellInfo, JSONObject currentCellData, JSONArray neighboringCells) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            CellIdentityNr cell = (CellIdentityNr) cellInfo.getCellIdentity();
            CellSignalStrengthNr signal = (CellSignalStrengthNr) cellInfo.getCellSignalStrength();

            try {
                if (cellInfo.isRegistered()) {
                    currentCellData.put("type", "NR");
                    currentCellData.put("rsrp", signal.getSsRsrp());
                    currentCellData.put("rsrq", signal.getSsRsrq());
                    currentCellData.put("sinr", signal.getSsSinr());
                    currentCellData.put("nci", cell.getNci());
                    currentCellData.put("pci", cell.getPci());
                    currentCellData.put("cid", cell.getNci());
                    currentCellData.put("nrarfcn", cell.getNrarfcn());
                    currentCellData.put("mcc", cell.getMccString());
                    currentCellData.put("mnc", cell.getMncString());
                    currentCellData.put("operator", cell.getOperatorAlphaLong());
                    currentCellData.put("tac", cell.getTac());
                    currentCellData.put("arfcn", cell.getNrarfcn());
                    currentCellData.put("mcc", cell.getMccString());
                    currentCellData.put("mnc", cell.getMncString());
                    currentCellData.put("dbm", signal.getDbm());
                    currentCellData.put("asuLevel", signal.getAsuLevel());
                    currentCellData.put("level", signal.getLevel());
                }else{
                    JSONObject neighbor = getNeighborObject(cell, signal);
                    neighboringCells.put(neighbor);
                }
            } catch (Exception ignored) {
            }
        }
    }

    @NonNull
    private static JSONObject getNeighborObject(CellIdentityNr cell, CellSignalStrengthNr signal) throws JSONException {
        JSONObject neighbor = new JSONObject();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            neighbor.put("cid", cell.getNci());
            neighbor.put("tac", cell.getTac());
            neighbor.put("arfcn", cell.getNrarfcn());
            neighbor.put("nci", cell.getNci());
            neighbor.put("pci", cell.getPci());
        }
        neighbor.put("dbm", signal.getDbm());
        neighbor.put("level", signal.getLevel());
        neighbor.put("asulevel", signal.getAsuLevel());
        return neighbor;
    }

    private long previousRxBytes = 0;
    private long previousTxBytes = 0;
    private long previousTime = 0;
    private double downloadSpeedKbps = 0;
    private double uploadSpeedKbps = 0;

    public void getCurrentNetworkSpeed() {
        if (isNetworkSpeedMonitoring) return;
        isNetworkSpeedMonitoring = true;

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable speedMonitor = new Runnable() {
            @Override
            public void run() {
                long currentRxBytes = TrafficStats.getTotalRxBytes();
                long currentTxBytes = TrafficStats.getTotalTxBytes();
                long currentTime = System.currentTimeMillis();

                if (previousRxBytes != TrafficStats.UNSUPPORTED && previousTxBytes != TrafficStats.UNSUPPORTED) {
                    long dataReceived = currentRxBytes - previousRxBytes;
                    long dataSent = currentTxBytes - previousTxBytes;
                    long timeElapsed = currentTime - previousTime; // in milliseconds

                    if (timeElapsed > 0) {
                        downloadSpeedKbps = Math.round((dataReceived * 8.0 / timeElapsed)  / 10.24) / 100.0; // Convert to Kbps
                        uploadSpeedKbps = Math.round((dataSent * 8.0 / timeElapsed) / 10.24) / 100.0; // Convert to Kbps
                    }
                }

                previousRxBytes = currentRxBytes;
                previousTxBytes = currentTxBytes;
                previousTime = currentTime;

                handler.postDelayed(this, 1000); // Update every second
            }
        };
        handler.post(speedMonitor);
    }

    public static String getConnectionType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            android.net.Network network = cm.getActiveNetwork();
            if (network == null) return "No Connection";

            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            if (capabilities == null) return "No Connection";

            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return "WiFi";
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return "Mobile";
            } else {
                return "Unknown";
            }
        }
        return "No Connection";
    }



}