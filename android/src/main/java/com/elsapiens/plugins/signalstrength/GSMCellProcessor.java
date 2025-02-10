package com.elsapiens.plugins.signalstrength;

import android.os.Build;
import android.telephony.CellIdentityGsm;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrengthGsm;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.getcapacitor.JSObject;

import org.json.JSONArray;

public class GSMCellProcessor implements CellProcessor {

    @RequiresApi(api = Build.VERSION_CODES.R) // API 18+ required
    @Override
    public void processCell(CellInfo cellInfo, JSObject currentCellData, JSONArray neighboringCells) {
        CellIdentityGsm cell = (CellIdentityGsm) cellInfo.getCellIdentity();
        CellSignalStrengthGsm signal = (CellSignalStrengthGsm) cellInfo.getCellSignalStrength();

        try {
            if (cellInfo.isRegistered()) {
                putIfValid(currentCellData, "type", "GSM");
                putIfValid(currentCellData, "technology", "2G");
                putIfValid(currentCellData, "mcc", cell.getMccString());
                putIfValid(currentCellData, "mnc", cell.getMncString());
                putIfValid(currentCellData, "operator", cell.getOperatorAlphaLong());

                putIfValid(currentCellData, "lac", cell.getLac()); // Location Area Code
                putIfValid(currentCellData, "tac", cell.getLac()); // Tracking Area Code

                putIfValid(currentCellData, "cid", cell.getCid()); // Cell ID
                putIfValid(currentCellData, "bsic", cell.getBsic()); // Base Station Identity Code
                putIfValid(currentCellData, "arfcn", cell.getArfcn()); // Frequency Number

                int ber = signal.getBitErrorRate();
                if (ber >= 0 && ber <= 99) {
                    putIfValid(currentCellData, "ber", ber); // Bit Error Rate
                }

                // Signal Strength Metrics
                putIfValid(currentCellData, "dbm", signal.getDbm()); // Signal strength in dBm
                putIfValid(currentCellData, "asulevel", signal.getAsuLevel()); // Arbitrary Strength Unit
                putIfValid(currentCellData, "level", signal.getLevel()); // Signal Level (0-4)

                putBandFromARFCN(currentCellData, cell.getArfcn()); // Determine GSM Band
            } else {
                JSObject neighbor = getNeighborObject(cell, signal);
                neighboringCells.put(neighbor);
            }
        } catch (Exception ignored) {
        }
    }

    @NonNull
    private static JSObject getNeighborObject(CellIdentityGsm cell, CellSignalStrengthGsm signal) {
        JSObject neighbor = new JSObject();
        neighbor.put("lac", cell.getLac()); // Location Area Code
        neighbor.put("cid", cell.getCid()); // Cell ID
        neighbor.put("bsic", cell.getBsic()); // Base Station Identity Code
        neighbor.put("arfcn", cell.getArfcn()); // Frequency Number
        neighbor.put("dbm", signal.getDbm()); // Signal strength in dBm
        neighbor.put("level", signal.getLevel()); // Signal Level (0-4)
        neighbor.put("asulevel", signal.getAsuLevel()); // Arbitrary Strength Unit
        return neighbor;
    }

    private static void putIfValid(JSObject json, String key, Object value) {
        if (value instanceof Integer) {
            if ((int) value != Integer.MAX_VALUE) {
                json.put(key, value);
            }
        } else if (value != null) {
            json.put(key, value);
        }
    }

    private static void putBandFromARFCN(JSObject json, int arfcn) {
        String bandName = "Unknown Band";
        String uplinkFrequency = "Unknown Uplink";
        String downlinkFrequency = "Unknown Downlink";

        if (arfcn >= 1 && arfcn <= 124) {
            bandName = "PGSM 900";
            uplinkFrequency = "890–915 MHz";
            downlinkFrequency = "935–960 MHz";
        } else if (arfcn == 0 || (arfcn >= 955 && arfcn <= 974)) {
            bandName = "E-GSM 900";
            uplinkFrequency = "880–915 MHz";
            downlinkFrequency = "925–960 MHz";
        } else if (arfcn >= 128 && arfcn <= 251) {
            bandName = "GSM 850";
            uplinkFrequency = "824–849 MHz";
            downlinkFrequency = "869–894 MHz";
        } else if (arfcn >= 259 && arfcn <= 293) {
            bandName = "GSM 450";
            uplinkFrequency = "450.6–457.6 MHz";
            downlinkFrequency = "460.6–467.6 MHz";
        } else if (arfcn >= 306 && arfcn <= 340) {
            bandName = "GSM 480";
            uplinkFrequency = "479–486 MHz";
            downlinkFrequency = "489–496 MHz";
        } else if (arfcn >= 512 && arfcn <= 885) {
            bandName = "DCS 1800";
            uplinkFrequency = "1710–1785 MHz";
            downlinkFrequency = "1805–1880 MHz";
        } else if (arfcn >= 975 && arfcn <= 1023) {
            bandName = "PCS 1900";
            uplinkFrequency = "1850–1910 MHz";
            downlinkFrequency = "1930–1990 MHz";
        }

        json.put("band", bandName);
        json.put("uplink_frequency", uplinkFrequency);
        json.put("downlink_frequency", downlinkFrequency);
    }
}