package com.elsapiens.plugins.signalstrength;
import android.telephony.CellIdentity;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrength;
import android.telephony.TelephonyManager;

import org.json.JSONArray;
import com.getcapacitor.JSObject;

public abstract class CellProcessor  {
    abstract void processCell(CellInfo cellInfo, JSObject currentCellData, TelephonyManager telephonyManager, JSONArray neighboringCells);

    abstract JSObject getNeighborObject(CellIdentity cell, CellSignalStrength signal);

    public static void putIfValid(JSObject json, String key, Object value) {
        if (value instanceof Integer) {
            if ((int) value != Integer.MAX_VALUE) {
                json.put(key, value);
            }
        } else if (value instanceof Long) {
            if ((long) value != Long.MAX_VALUE) {
                json.put(key, value);
            }
        } else if (!value.equals(CellInfo.UNAVAILABLE) && !value.equals(CellInfo.UNAVAILABLE_LONG)  && value != null) {
            json.put(key, value);
        }
    }

    public static void putIfValidAsu(JSObject json, int value) {
        if (value >=0 && value <= 99) {
            json.put("asulevel", value);
        }
    }
}