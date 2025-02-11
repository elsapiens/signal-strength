package com.elsapiens.plugins.signalstrength;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrengthWcdma;
import androidx.annotation.NonNull;
import com.getcapacitor.JSObject;
import org.json.JSONArray;
public class WCDMACellProcessor implements CellProcessor {
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

                putIfValid(currentCellData, "lac", cell.getLac()); // Location Area Code
                putIfValid(currentCellData, "cid", cell.getCid()); // Cell ID
                putIfValid(currentCellData, "psc", cell.getPsc()); // Primary Scrambling Code
                putIfValid(currentCellData, "uarfcn", cell.getUarfcn()); // Frequency Number

                // Signal Strength Metrics
                putIfValid(currentCellData, "dbm", signal.getDbm()); // Signal strength in dBm
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
    private static JSObject getNeighborObject(CellIdentityWcdma cell, CellSignalStrengthWcdma signal) {
        JSObject neighbor = new JSObject();
        neighbor.put("lac", cell.getLac()); // Location Area Code
        neighbor.put("cid", cell.getCid()); // Cell ID
        neighbor.put("psc", cell.getPsc()); // Primary Scrambling Code
        neighbor.put("uarfcn", cell.getUarfcn()); // Frequency Number
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

    private static void putBandFromUARFCN(JSObject json, int uarfcn) {
        String bandName = "Unknown Band";
        String uplinkFrequency = "Unknown Uplink";
        String downlinkFrequency = "Unknown Downlink";

        if (uarfcn >= 10562 && uarfcn <= 10838) {
            bandName = "Band 1 (2100 MHz)";
            uplinkFrequency = "1920–1980 MHz";
            downlinkFrequency = "2110–2170 MHz";
        } else if (uarfcn >= 9662 && uarfcn <= 9938) {
            bandName = "Band 2 (1900 MHz)";
            uplinkFrequency = "1850–1910 MHz";
            downlinkFrequency = "1930–1990 MHz";
        } else if (uarfcn >= 1162 && uarfcn <= 1513) {
            bandName = "Band 5 (850 MHz)";
            uplinkFrequency = "824–849 MHz";
            downlinkFrequency = "869–894 MHz";
        } else if (uarfcn >= 2937 && uarfcn <= 3088) {
            bandName = "Band 8 (900 MHz)";
            uplinkFrequency = "880–915 MHz";
            downlinkFrequency = "925–960 MHz";
        } else if (uarfcn >= 412 && uarfcn <= 687) {
            bandName = "Band 4 (AWS 1700/2100 MHz)";
            uplinkFrequency = "1710–1755 MHz";
            downlinkFrequency = "2110–2155 MHz";
        } else if (uarfcn >= 2012 && uarfcn <= 2338) {
            bandName = "Band 3 (1800 MHz)";
            uplinkFrequency = "1710–1785 MHz";
            downlinkFrequency = "1805–1880 MHz";
        } else if (uarfcn >= 2237 && uarfcn <= 2563) {
            bandName = "Band 9 (1800 MHz)";
            uplinkFrequency = "1749.9–1784.9 MHz";
            downlinkFrequency = "1844.9–1879.9 MHz";
        } else if (uarfcn >= 3112 && uarfcn <= 3388) {
            bandName = "Band 10 (AWS 1700/2100 MHz)";
            uplinkFrequency = "1710–1770 MHz";
            downlinkFrequency = "2110–2170 MHz";
        } else if (uarfcn >= 3712 && uarfcn <= 3787) {
            bandName = "Band 11 (1500 MHz)";
            uplinkFrequency = "1427.9–1447.9 MHz";
            downlinkFrequency = "1475.9–1495.9 MHz";
        } else if (uarfcn >= 3842 && uarfcn <= 3903) {
            bandName = "Band 19 (850 MHz)";
            uplinkFrequency = "830–845 MHz";
            downlinkFrequency = "875–890 MHz";
        }

        json.put("band", bandName);
        json.put("uplink_frequency", uplinkFrequency);
        json.put("downlink_frequency", downlinkFrequency);
    }
}