package com.elsapiens.plugins.signalstrength;

import android.os.Build;
import android.telephony.CellIdentityNr;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrengthNr;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.getcapacitor.JSObject;

import org.json.JSONArray;

public class NrCellProcessor implements CellProcessor {
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void processCell(CellInfo cellInfo, JSObject currentCellData, JSONArray neighboringCells) {
        CellIdentityNr cell = (CellIdentityNr) cellInfo.getCellIdentity();
        CellSignalStrengthNr signal = (CellSignalStrengthNr) cellInfo.getCellSignalStrength();

        try {
            if (cellInfo.isRegistered()) {
                putIfValid(currentCellData, "type", "NR");
                putIfValid(currentCellData, "technology", "5G");
                putIfValid(currentCellData, "mcc", cell.getMccString());
                putIfValid(currentCellData, "mnc", cell.getMncString());
                putIfValid(currentCellData, "operator", cell.getOperatorAlphaLong());

                // Extract gNodeB ID and Cell ID from NCI
                long nci = cell.getNci();
                long gNodeB = nci >> 10; // First 26 bits
                long cellId = nci & 0x3FF; // Last 10 bits

                putIfValid(currentCellData, "nci", nci); // Full NR Cell Identity
                putIfValid(currentCellData, "gnodebId", gNodeB); // gNodeB ID
                putIfValid(currentCellData, "cellId", cellId); // 5G Cell ID
                putIfValid(currentCellData, "pci", cell.getPci()); // Physical Cell ID
                putIfValid(currentCellData, "tac", cell.getTac()); // Tracking Area Code
                putIfValid(currentCellData, "nrarfcn", cell.getNrarfcn()); // Absolute Frequency Number

                // 5G Signal Strength Metrics
                putIfValid(currentCellData, "ss_rsrp", signal.getSsRsrp()); // SS Reference Signal Received Power
                putIfValid(currentCellData, "ss_rsrq", signal.getSsRsrq()); // SS Reference Signal Received Quality
                putIfValid(currentCellData, "ss_sinr", signal.getSsSinr()); // SS Signal-to-Interference-plus-Noise Ratio
                putIfValid(currentCellData, "rsrp", signal.getCsiRsrp()); // CSI RSRP
                putIfValid(currentCellData, "rsrq", signal.getCsiRsrq()); // CSI RSRQ
                putIfValid(currentCellData, "sinr", signal.getCsiSinr()); // CSI SINR
                putIfValid(currentCellData, "rssi", getRssiFromSignal(signal)); // RSSI
                putIfValid(currentCellData, "dbm", signal.getDbm()); // Signal strength in dBm

                putBandFromNRARFCN(currentCellData, cell.getNrarfcn()); // Determine 5G band
            } else {
                JSObject neighbor = getNeighborObject(cell, signal);
                neighboringCells.put(neighbor);
            }
        } catch (Exception ignored) {
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @NonNull
    private static JSObject getNeighborObject(CellIdentityNr cell, CellSignalStrengthNr signal) {
        JSObject neighbor = new JSObject();

        long nci = cell.getNci();
        long gNodeB = nci >> 10;
        long cellId = nci & 0x3FF;

        neighbor.put("nci", nci); // Full NR Cell ID
        neighbor.put("gnodebId", gNodeB); // gNodeB ID
        neighbor.put("cellId", cellId); // Cell ID
        neighbor.put("pci", cell.getPci()); // Physical Cell ID
        neighbor.put("tac", cell.getTac()); // Tracking Area Code
        neighbor.put("nrarfcn", cell.getNrarfcn()); // Absolute Frequency Number
        neighbor.put("ss_rsrp", signal.getSsRsrp());
        neighbor.put("ss_rsrq", signal.getSsRsrq());
        neighbor.put("ss_sinr", signal.getSsSinr());
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

    private static void putBandFromNRARFCN(JSObject json, int nrarfcn) {
        String bandName = "Unknown Band";
        String uplinkFrequency = "Unknown Uplink";
        String downlinkFrequency = "Unknown Downlink";

        if (nrarfcn >= 422000 && nrarfcn <= 434000) {
            bandName = "N1 (2100 MHz)";
            uplinkFrequency = "1920–1980 MHz";
            downlinkFrequency = "2110–2170 MHz";
        } else if (nrarfcn >= 386000 && nrarfcn <= 398000) {
            bandName = "N2 (1900 MHz)";
            uplinkFrequency = "1850–1910 MHz";
            downlinkFrequency = "1930–1990 MHz";
        } else if (nrarfcn >= 361000 && nrarfcn <= 376000) {
            bandName = "N3 (1800 MHz)";
            uplinkFrequency = "1710–1785 MHz";
            downlinkFrequency = "1805–1880 MHz";
        } else if (nrarfcn >= 370000 && nrarfcn <= 379000) {
            bandName = "N5 (850 MHz)";
            uplinkFrequency = "824–849 MHz";
            downlinkFrequency = "869–894 MHz";
        } else if (nrarfcn >= 285400 && nrarfcn <= 286400) {
            bandName = "N7 (2600 MHz)";
            uplinkFrequency = "2500–2570 MHz";
            downlinkFrequency = "2620–2690 MHz";
        } else if (nrarfcn >= 303000 && nrarfcn <= 303600) {
            bandName = "N8 (900 MHz)";
            uplinkFrequency = "880–915 MHz";
            downlinkFrequency = "925–960 MHz";
        } else if (nrarfcn >= 151600 && nrarfcn <= 160580) {
            bandName = "N41 (2500 MHz)";
            uplinkFrequency = "2496–2690 MHz";
            downlinkFrequency = "2496–2690 MHz"; // TDD Band
        } else if (nrarfcn >= 222000 && nrarfcn <= 227000) {
            bandName = "N77 (3300-4200 MHz)";
            uplinkFrequency = "3300–4200 MHz";
            downlinkFrequency = "3300–4200 MHz"; // TDD Band
        } else if (nrarfcn >= 330000 && nrarfcn <= 379000) {
            bandName = "N78 (3500 MHz)";
            uplinkFrequency = "3300–3800 MHz";
            downlinkFrequency = "3300–3800 MHz"; // TDD Band
        } else if (nrarfcn >= 415000 && nrarfcn <= 425000) {
            bandName = "N79 (4700 MHz)";
            uplinkFrequency = "4400–5000 MHz";
            downlinkFrequency = "4400–5000 MHz"; // TDD Band
        } else if (nrarfcn >= 242500 && nrarfcn <= 246500) {
            bandName = "N257 (28 GHz)";
            uplinkFrequency = "26500–29500 MHz";
            downlinkFrequency = "26500–29500 MHz"; // TDD Band
        } else if (nrarfcn >= 300000 && nrarfcn <= 303000) {
            bandName = "N258 (26 GHz)";
            uplinkFrequency = "24250–27500 MHz";
            downlinkFrequency = "24250–27500 MHz"; // TDD Band
        } else if (nrarfcn >= 317000 && nrarfcn <= 322000) {
            bandName = "N260 (39 GHz)";
            uplinkFrequency = "37000–40000 MHz";
            downlinkFrequency = "37000–40000 MHz"; // TDD Band
        } else if (nrarfcn >= 440000 && nrarfcn <= 450000) {
            bandName = "N261 (28 GHz)";
            uplinkFrequency = "27500–28350 MHz";
            downlinkFrequency = "27500–28350 MHz"; // TDD Band
        }

        json.put("band", bandName);
        json.put("uplink_frequency", uplinkFrequency);
        json.put("downlink_frequency", downlinkFrequency);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private static int getRssiFromSignal(CellSignalStrengthNr signal) {
        int rsrp = signal.getSsRsrp();
        int rsrq = signal.getSsRsrq();
        if (rsrp != Integer.MAX_VALUE && rsrq != Integer.MAX_VALUE) {
            return rsrp + rsrq;
        }
        return -1;
    }
}