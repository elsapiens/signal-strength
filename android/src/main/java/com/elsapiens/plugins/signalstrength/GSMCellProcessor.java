package com.elsapiens.plugins.signalstrength;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityGsm;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import com.getcapacitor.JSObject;
import org.json.JSONArray;
public class GSMCellProcessor extends CellProcessor {
    private Object[][] arfcnBands = {
            {1, 124, "PGSM 900", "890–915 MHz", "935–960 MHz"},
            {0, 0, "E-GSM 900", "880–915 MHz", "925–960 MHz"},
            {955, 974, "E-GSM 900", "880–915 MHz", "925–960 MHz"},
            {128, 251, "GSM 850", "824–849 MHz", "869–894 MHz"},
            {259, 293, "GSM 450", "450.6–457.6 MHz", "460.6–467.6 MHz"},
            {306, 340, "GSM 480", "479–486 MHz", "489–496 MHz"},
            {512, 885, "DCS 1800", "1710–1785 MHz", "1805–1880 MHz"},
            {975, 1023, "PCS 1900", "1850–1910 MHz", "1930–1990 MHz"},
    };
    @Override
    public void processCell(CellInfo cellInfo, JSObject currentCellData, TelephonyManager telephonyManager, JSONArray neighboringCells) {
        CellIdentityGsm cell = (CellIdentityGsm) cellInfo.getCellIdentity();
        CellSignalStrengthGsm signal = (CellSignalStrengthGsm) cellInfo.getCellSignalStrength();
        try {
            if (cellInfo.isRegistered()) {
                putIfValid(currentCellData, "type", "GSM");
                putIfValid(currentCellData, "technology", "2G");
                putIfValid(currentCellData, "mcc", cell.getMccString());
                putIfValid(currentCellData, "mnc", cell.getMncString());
                putIfValid(currentCellData, "operator", cell.getOperatorAlphaLong());
                putIfValid(currentCellData, "cid", cell.getCid()); // Cell ID
                putIfValid(currentCellData, "cellId", cell.getCid()); // Cell ID
                putIfValid(currentCellData, "lac", cell.getLac()); // Location Area Code
                putIfValid(currentCellData, "tac", cell.getLac()); // Tracking Area Code
                putIfValid(currentCellData, "bsic", cell.getBsic()); // Base Station Identity Code
                putIfValid(currentCellData, "arfcn", cell.getArfcn()); // Frequency Number
                putIfValid(currentCellData, "rxlev", signal.getDbm()); // Signal strength in dBm
                putIfValid(currentCellData, "ta", signal.getTimingAdvance()); // Timing Advance
                int ber = signal.getBitErrorRate();
                if (ber >= 0 && ber <= 7) {
                    currentCellData.put("rxqual", ber); // Bit Error Rate
                }
                putIfValidAsu(currentCellData, signal.getAsuLevel()); // Arbitrary Strength Unit
                putIfValid(currentCellData, "level", signal.getLevel()); // Signal Level (0-4)
                putBandFromARFCN(currentCellData, cell.getArfcn()); // Determine GSM Band
            } else if (cell.getCid() > 0 && cell.getCid() != Integer.MAX_VALUE) {
                JSObject neighbor = getNeighborObject(cell, signal);
                neighboringCells.put(neighbor);
            }
        } catch (Exception ignored) {
        }
    }
    @NonNull
    public JSObject getNeighborObject(CellIdentity cell, CellSignalStrength signal) {
        JSObject neighbor = new JSObject();
        CellIdentityGsm gsmCell = (CellIdentityGsm) cell;
        CellSignalStrengthGsm  gsmSignal = (CellSignalStrengthGsm) signal;
        putIfValid(neighbor, "type", "GSM");
        putIfValid(neighbor, "technology", "2G");
        putIfValid(neighbor, "mcc", gsmCell.getMccString());
        putIfValid(neighbor, "mnc", gsmCell.getMncString());
        putIfValid(neighbor, "operator", gsmCell.getOperatorAlphaLong());
        putIfValid(neighbor, "cid", gsmCell.getCid()); // Cell ID
        putIfValid(neighbor, "cellId", gsmCell.getCid()); // Cell ID
        putIfValid(neighbor, "lac", gsmCell.getLac()); // Location Area Code
        putIfValid(neighbor, "bsic", gsmCell.getBsic()); // Base Station Identity Code
        putIfValid(neighbor, "arfcn", gsmCell.getArfcn()); // Frequency Number
        putIfValid(neighbor, "rxlev", signal.getDbm()); // Signal strength in dBm
        int ber = gsmSignal.getBitErrorRate();
        if (ber >= 0 && ber <= 7) {
            neighbor.put("rxqual", ber); // Bit Error Rate
        }
        putIfValidAsu(neighbor, gsmSignal.getAsuLevel()); // Arbitrary Strength Unit
        return neighbor;
    }
    private void putBandFromARFCN(JSObject json, int arfcn) {
        String bandName = "Unknown Band";
        String uplinkFrequency = "Unknown Uplink";
        String downlinkFrequency = "Unknown Downlink";
        for (Object[] band : arfcnBands) {
            int lowerBound = (int) band[0];
            int upperBound = (int) band[1];
            if (arfcn >= lowerBound && arfcn <= upperBound) {
                bandName = (String) band[2];
                uplinkFrequency = (String) band[3];
                downlinkFrequency = (String) band[4];
                break;
            }
        }
        json.put("band", bandName);
        json.put("uplinkFrequency", uplinkFrequency);
        json.put("downlinkFrequency", downlinkFrequency);
    }

}