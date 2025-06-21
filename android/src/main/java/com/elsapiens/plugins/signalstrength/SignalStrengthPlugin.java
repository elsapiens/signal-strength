package com.elsapiens.plugins.signalstrength;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.telephony.*;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@CapacitorPlugin(name = "SignalStrength", permissions = {
        @Permission(alias = "location", strings = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }),
        @Permission(alias = "backgroundLocation", strings = {
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
        }),
        @Permission(alias = "phone", strings = {
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.ANSWER_PHONE_CALLS,
                Manifest.permission.MANAGE_OWN_CALLS
        }),
        @Permission(alias = "networkState", strings = {
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.READ_PHONE_NUMBERS
        }),
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
    @Override
    protected void handleOnStart() {
        super.handleOnStart();
    }
    @Override
    protected void handleOnStop() {
        super.handleOnStop();
        unregisterCellInfoListener();
    }

    @PluginMethod
    public void startMonitoring(PluginCall call) {
        requestedTechnology = call.getString("technology", "All");
        if (isMissingRequiredPermissions(getContext())) {
            requestPermissions(call, "registerCellInfoListener");
        }else {
            registerCellInfoListener(call);
        }
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

    @PermissionCallback
    private void registerCellInfoListener(PluginCall call) {
        if (registerCellInfoListenerRunning) {
            call.reject("Cell info listener is already running");
        } else if (isMissingRequiredPermissions(getContext())) {
            requestPermissions(call, "executeSignalRegistration");
        }else{
            executeSignalRegistration(call);
        }
    }

    @PermissionCallback
    private void executeSignalRegistration(PluginCall call) {
        getCurrentNetworkSpeed();
        Context context = getContext();
        if (context != null) {
            telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        }
      if (telephonyCallback == null) {
        telephonyCallback = new MyTelephonyCallback();
      }
      assert context != null;
      telephonyManager.registerTelephonyCallback(context.getMainExecutor(), telephonyCallback);
      registerCellInfoListenerRunning = true;
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay(new SignalStrengthTask(), 0, 100, TimeUnit.MILLISECONDS); // Every 1 second
        call.resolve();
    }

    private class SignalStrengthTask implements Runnable {
        @Override
        public void run() {
            if (ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                telephonyManager.requestCellInfoUpdate(getContext().getMainExecutor(),
                        new TelephonyManager.CellInfoCallback() {
                            @Override
                            public void onCellInfo(@NonNull List<CellInfo> cellInfoList) {
                                handleCellInfoChanged(cellInfoList);
                            }
                        });
            }
        }
    }

    private void handleCellInfoChanged(List<CellInfo> cellInfoList) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < 900) {
            return; // Skip if less than 900 milliseconds have passed
        }
        lastUpdateTime = currentTime;

        if (cellInfoList != null && !cellInfoList.isEmpty()) {
            JSONArray neighboringCells = new JSONArray();
            JSObject currentCellData = new JSObject();
            boolean isNsaNR =  this.checkIfNsaNR(cellInfoList);
            CellInfo NrCellInfo = null;
            if (isNsaNR) {
                for (CellInfo cellInfo : cellInfoList) {
                    if (cellInfo instanceof CellInfoNr) {
                        NrCellInfo = cellInfo;
                        break;
                    }
                }
            }
            for (CellInfo cellInfo : cellInfoList) {
                if (cellInfo instanceof CellInfoGsm) {
                    new GSMCellProcessor().processCell(cellInfo, NrCellInfo, currentCellData, this.telephonyManager, neighboringCells);
                } else if (cellInfo instanceof CellInfoWcdma) {
                    new WCDMACellProcessor().processCell(cellInfo, NrCellInfo, currentCellData, this.telephonyManager, neighboringCells);
                } else if (cellInfo instanceof CellInfoLte && isNsaNR){
                    new NrNSACellProcessor().processCell(cellInfo, NrCellInfo, currentCellData, this.telephonyManager, neighboringCells);
                } else if (cellInfo instanceof CellInfoLte) {
                    new LteCellProcessor().processCell(cellInfo, NrCellInfo, currentCellData, this.telephonyManager, neighboringCells);
                } else if (cellInfo instanceof CellInfoNr) {
                    new NrCellProcessor().processCell(cellInfo, NrCellInfo, currentCellData, this.telephonyManager, neighboringCells);
                }
            }
            JSObject result = new JSObject();
            if (
                    !(requestedTechnology.equals("ALL")
                    && !Objects.equals(requestedTechnology, getNetworkType())
            )) {
                result.put("status", "error");
                result.put("message", "Not connected on the requested network type " + requestedTechnology);
            } else {
                result.put("status", "success");
            }
            putGeneralData(result);
            result.put("currentCell", currentCellData);
            result.put("neighboringCells", neighboringCells);
            result.put("timestamp", currentTime);
            result.put("networkType", getNetworkType());
          if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            notifyListeners("signalUpdate", result);
          }
          result.put("networkTypeName", getNetworkTypeName(telephonyManager.getDataNetworkType()));
            notifyListeners("signalUpdate", result);
        }
    }

    private boolean checkIfNsaNR(List<CellInfo> cellInfoList) {
        boolean hasLTE = cellInfoList.stream().anyMatch(cellInfo -> cellInfo instanceof CellInfoLte);
        boolean hasNR = cellInfoList.stream().anyMatch(cellInfo -> cellInfo instanceof CellInfoNr);
        return hasLTE && hasNR;
    }

    private void unregisterCellInfoListener() {
        if (telephonyCallback != null) {
            telephonyManager.unregisterTelephonyCallback(telephonyCallback);
            telephonyCallback = null;
            registerCellInfoListenerRunning = false;
        } else if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, 0);
            phoneStateListener = null;
            registerCellInfoListenerRunning = false;
        }
    }

    private class MyTelephonyCallback extends TelephonyCallback implements TelephonyCallback.CellInfoListener {
        @Override
        public void onCellInfoChanged(@NonNull List<CellInfo> cellInfoList) {
            handleCellInfoChanged(cellInfoList);
        }
    }
    private boolean isMissingRequiredPermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED;

    }
    private void requestPermissions(PluginCall call, String methodName) {
        Log.e("SignalStrength", "Requesting permissions for " + methodName);
        if (getActivity() != null) {
            requestPermissionForAliases(new String[]{"location", "backgroundLocation", "phone", "networkState"}, call, methodName);
        }
    }

    private String getNetworkType() {
      if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
        return "Permission Denied";
      }
      int networkType = telephonyManager.getDataNetworkType();
        return getNetworkTypeString(networkType);
    }

    private String getNetworkTypeString(int networkType) {

        return switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE,
                    TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT,
                    TelephonyManager.NETWORK_TYPE_IDEN, TelephonyManager.NETWORK_TYPE_GSM ->
                "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0,
                    TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_EVDO_B,
                    TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA,
                    TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_EHRPD,
                    TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_TD_SCDMA ->
                "3G";
            case TelephonyManager.NETWORK_TYPE_LTE, TelephonyManager.NETWORK_TYPE_IWLAN -> "4G";
            case TelephonyManager.NETWORK_TYPE_NR -> "5G";
            default -> "UNKNOWN";
        };
    }

    public static String getNetworkTypeName(int networkType) {
        return switch (networkType) {

            case TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS (2G)";
            case TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE (2G)";
            case TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA (2G)";
            case TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT (2G)";
            case TelephonyManager.NETWORK_TYPE_IDEN -> "iDEN (2G)";
            case TelephonyManager.NETWORK_TYPE_GSM -> "GSM (2G)";

            case TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS (3G)";
            case TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO 0 (3G)";
            case TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO A (3G)";
            case TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO B (3G)";
            case TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA (3G)";
            case TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA (3G)";
            case TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA (3G)";
            case TelephonyManager.NETWORK_TYPE_EHRPD -> "eHRPD (3G)";
            case TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+ (3G)";
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "TD-SCDMA (3G)";

            case TelephonyManager.NETWORK_TYPE_LTE -> "LTE (4G)";
            case TelephonyManager.NETWORK_TYPE_IWLAN -> "IWLAN (4G)";

            case TelephonyManager.NETWORK_TYPE_NR -> "5G NR";

            default -> {
                Log.w("NetworkType", "Unknown network type: " + networkType);
                yield "UNKNOWN (" + networkType + ")";
            }
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
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(call, "executeMakeCall");
        }else{
            executeMakeCall(call);
        }
    }
    @PermissionCallback
    private void executeMakeCall(PluginCall call) {
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
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                call.reject("Permission Denied");
                return;
            }
            telecomManager.placeCall(uri, extras);
            call.resolve();
        } else {
            call.reject("TelecomManager is not available");
        }
    }

    @PluginMethod
    public void disconnectCall(PluginCall call) {
        try {
            Context context = getContext();
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                call.reject("Permission Denied");
                return;
            }

            TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            if (telecomManager != null) {
                telecomManager.endCall();
                call.resolve();
                return;
            }

            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            @SuppressLint("DiscouragedPrivateApi") Method endCallMethod = telephonyManager.getClass().getDeclaredMethod("endCall");
            endCallMethod.invoke(telephonyManager);

            call.resolve();
        } catch (Exception e) {
            call.reject("Failed to disconnect call: " + e.getMessage());
        }
    }

    @PluginMethod
    public void getNetworkInfo(PluginCall call) {
        JSObject result = getNetworkInfo();
        call.resolve(result);
    }

    private JSObject getNetworkInfo() {
        return new JSObject();
    }

    private String getNetworkVoiceType() {
        String callType = "UNKNOWN";
        try {
            Context context = getContext();
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            if (telephonyManager != null) {
                if (ActivityCompat.checkSelfPermission(getContext(),
                        Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return "Permission Denied";
                }
                int voiceNetworkType = telephonyManager.getVoiceNetworkType();

                switch (voiceNetworkType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        callType = "2G";
                        break;

                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        callType = "3G";
                        break;

                    case TelephonyManager.NETWORK_TYPE_LTE:
                        callType = "4G VoLTE";
                        break;

                    case TelephonyManager.NETWORK_TYPE_NR:
                        callType = "5G VoNR";
                        break;

                    default:
                        // Handle unknown or newer network types
                        if (voiceNetworkType == TelephonyManager.NETWORK_TYPE_BITMASK_LTE_CA) {
                            callType = "4G LTE CA";
                        } else if (voiceNetworkType == TelephonyManager.NETWORK_TYPE_GSM) {
                            callType = "2G GSM";
                        } else if (voiceNetworkType == TelephonyManager.NETWORK_TYPE_TD_SCDMA) {
                            callType = "3G TD-SCDMA";
                        } else if (voiceNetworkType == TelephonyManager.NETWORK_TYPE_IWLAN) {
                            callType = "WiFi Calling";
                        }else {
                            callType = "Unknown";
                        }
                        break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching network voice type: " + e.getMessage());
        }
        return callType;
    }

    /**
     * check if more than one SIM card is present
     */
    private boolean isMultiSim() {
        return telephonyManager.getActiveModemCount() > 1;
    }

    private int getActiveSubscriptionCount() {
        Context context = getContext();
        SubscriptionManager subscriptionManager = (SubscriptionManager) context
                .getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        if (subscriptionManager != null) {
            if (ActivityCompat.checkSelfPermission(getContext(),
                    Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
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

    private void putGeneralData(JSObject result) {
        result.put("isMultiSim", isMultiSim());
        result.put("simCount", getActiveSubscriptionCount());
        result.put("isOnCall", isOnCall());
        try {
            if (result.getBoolean("isOnCall")) {
                result.put("callType", getNetworkVoiceType());
                String callState = getCallStateName();
                result.put("callState", callState);

            }
        } catch (JSONException ignored) {}
        String networkType = getConnectionType(getContext());
        result.put("dataConnectionType", networkType);
        if (!networkType.equals("No Connection")) {
            result.put("speed", new JSObject().put("download", downloadSpeedKbps).put("upload", uploadSpeedKbps));
        }
    }

    private long previousRxBytes = 0;
    private long previousTxBytes = 0;
    private long previousTime = 0;
    private double downloadSpeedKbps = 0;
    private double uploadSpeedKbps = 0;

    public void getCurrentNetworkSpeed() {
        if (isNetworkSpeedMonitoring)
            return;
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
                        downloadSpeedKbps = Math.round((dataReceived * 8.0 / timeElapsed) / 10.24) / 100.0; // Convert
                                                                                                            // to Kbps
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
            if (network == null)
                return "No Connection";

            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            if (capabilities == null)
                return "No Connection";

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
    private String getCallStateName() {
        int callState = telephonyManager.getCallState();
        return switch (callState) {
            case TelephonyManager.CALL_STATE_IDLE -> "Idle";
            case TelephonyManager.CALL_STATE_RINGING -> "Ringing";
            case TelephonyManager.CALL_STATE_OFFHOOK -> "Offhook";
            default -> "Unknown";
        };
    }
}
