package com.elsapiens.plugins.signalstrength;
import android.telephony.CellIdentityGsm;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrengthGsm;
import androidx.annotation.NonNull;
import com.getcapacitor.JSObject;
import org.json.JSONArray;
public class GSMCellProcessor implements CellProcessor {
    static Object[][] arfcnBands = {
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
                int rxLev = signal.getDbm();
                putIfValid(currentCellData, "rxlev", rxLev); // Signal strength in dBm
                int ber = signal.getBitErrorRate();
                if (ber >= 0 && ber <= 99) {
                    putIfValid(currentCellData, "ber", ber); // Bit Error Rate
                    int rxQual = calculateRxQual(ber);
                    putIfValid(currentCellData, "rxqual", rxQual); // Signal Quality (0-7)
                }else{
                    putIfValid(currentCellData, "rxqual", estimateRxQualFromRSSI(rxLev)); // Signal Quality (0-7)
                }
                //calculate rx Quality
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
        neighbor.put("rxlev", signal.getDbm()); // Signal strength in dBm
        int ber = signal.getBitErrorRate();
        if (ber >= 0 && ber <= 99) {
            neighbor.put("ber", ber); // Bit Error Rate
            int rxQual = calculateRxQual(ber);
            neighbor.put("rxqual", rxQual); // Signal Quality (0-7)
        }else{
            neighbor.put("rxqual", estimateRxQualFromRSSI(signal.getDbm())); // Signal Quality (0-7)
        }
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
        json.put("uplink_frequency", uplinkFrequency);
        json.put("downlink_frequency", downlinkFrequency);
    }
    private static int calculateRxQual(int ber) {
        if (ber == Integer.MAX_VALUE || ber < 0) {
            return -1; // Invalid BER
        } else if (ber <= 20) {
            return 0;
        } else if (ber <= 40) {
            return 1;
        } else if (ber <= 80) {
            return 2;
        } else if (ber <= 160) {
            return 3;
        } else if (ber <= 320) {
            return 4;
        } else if (ber <= 640) {
            return 5;
        } else if (ber <= 1280) {
            return 6;
        } else {
            return 7;
        }
    }

    private static int estimateRxQualFromRSSI(int rssi) {
        if (rssi >= -70) return 0;
        if (rssi >= -80) return 1;
        if (rssi >= -85) return 2;
        if (rssi >= -90) return 3;
        if (rssi >= -95) return 4;
        if (rssi >= -100) return 5;
        if (rssi >= -107) return 6;
        return 7; // Worst quality
    }
}