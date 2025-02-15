package com.elsapiens.plugins.signalstrength;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthWcdma;
import androidx.annotation.NonNull;
import com.getcapacitor.JSObject;
import org.json.JSONArray;
public class WCDMACellProcessor extends CellProcessor {

    static Object[][] wcdmaBands = {
            {10562, 10838, "Band 1 (2100 MHz)", "1920–1980 MHz", "2110–2170 MHz"},
            {9662, 9938, "Band 2 (1900 MHz)", "1850–1910 MHz", "1930–1990 MHz"},
            {1162, 1513, "Band 5 (850 MHz)", "824–849 MHz", "869–894 MHz"},
            {2937, 3088, "Band 8 (900 MHz)", "880–915 MHz", "925–960 MHz"},
            {412, 687, "Band 4 (AWS 1700/2100 MHz)", "1710–1755 MHz", "2110–2155 MHz"},
            {2012, 2338, "Band 3 (1800 MHz)", "1710–1785 MHz", "1805–1880 MHz"},
            {2237, 2563, "Band 9 (1800 MHz)", "1749.9–1784.9 MHz", "1844.9–1879.9 MHz"},
            {3112, 3388, "Band 10 (AWS 1700/2100 MHz)", "1710–1770 MHz", "2110–2170 MHz"},
            {3712, 3787, "Band 11 (1500 MHz)", "1427.9–1447.9 MHz", "1475.9–1495.9 MHz"},
            {3842, 3903, "Band 19 (850 MHz)", "830–845 MHz", "875–890 MHz"},
    };
    @Override
    public void processCell(CellInfo cellInfo, JSObject currentCellData, JSONArray neighboringCells) {
        CellIdentityWcdma cell = (CellIdentityWcdma) cellInfo.getCellIdentity();
        CellSignalStrengthWcdma signal = (CellSignalStrengthWcdma) cellInfo.getCellSignalStrength();
        try {
            if (cellInfo.isRegistered()) {
                putIfValid(currentCellData, "type", "WCDMA");
                putIfValid(currentCellData, "technology", "3G");
                putIfValid(currentCellData, "mcc", cell.getMccString());
                putIfValid(currentCellData, "mnc", cell.getMncString());
                putIfValid(currentCellData, "operator", cell.getOperatorAlphaLong());
                putIfValid(currentCellData, "cid", cell.getCid()); // Cell ID
                putIfValid(currentCellData, "cellId", cell.getCid()); // Cell ID
                putIfValid(currentCellData, "lac", cell.getLac()); // Location Area Code
                putIfValid(currentCellData, "psc", cell.getPsc()); // Primary Scrambling Code
                putIfValid(currentCellData, "uarfcn", cell.getUarfcn()); // Frequency Number
                putIfValid(currentCellData, "rxlev", signal.getDbm()); // Signal strength in dBm
                putIfValid(currentCellData, "asulevel", signal.getAsuLevel()); // Arbitrary Strength Unit
                putIfValid(currentCellData, "level", signal.getLevel()); // Signal Level (0-4)
                putBandFromUARFCN(currentCellData, cell.getUarfcn()); // Determine UMTS Band
            } else {
                JSObject neighbor = getNeighborObject(cell, signal);
                neighboringCells.put(neighbor);
            }
        } catch (Exception ignored) {
        }
    }

    @NonNull
    protected JSObject getNeighborObject(CellIdentity cell, CellSignalStrength signal) {
        CellIdentityWcdma wcdmaCell = (CellIdentityWcdma) cell;
        JSObject neighbor = new JSObject();
        putIfValid(neighbor, "cid", wcdmaCell.getCid());
        putIfValid(neighbor, "cellId", wcdmaCell.getCid());
        putIfValid(neighbor, "lac", wcdmaCell.getLac());
        putIfValid(neighbor, "psc", wcdmaCell.getPsc());
        putIfValid(neighbor, "uarfcn", wcdmaCell.getUarfcn());
        putIfValid(neighbor, "rxlev", signal.getDbm());
        putIfValid(neighbor, "asulevel", signal.getAsuLevel());
        return neighbor;
    }

    private static void putBandFromUARFCN(JSObject json, int uarfcn) {
        String bandName = "Unknown Band";
        String uplinkFrequency = "Unknown Uplink";
        String downlinkFrequency = "Unknown Downlink";
        for (Object[] band : wcdmaBands) {
            int start = (int) band[0];
            int end = (int) band[1];
            if (uarfcn >= start && uarfcn <= end) {
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