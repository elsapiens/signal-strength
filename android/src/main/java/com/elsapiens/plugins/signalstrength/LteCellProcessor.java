package com.elsapiens.plugins.signalstrength;

import android.os.Build;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrengthLte;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.getcapacitor.JSObject;

import org.json.JSONArray;

public class LteCellProcessor implements CellProcessor {
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void processCell(CellInfo cellInfo, JSObject currentCellData, JSONArray neighboringCells) {
        CellIdentityLte cell = (CellIdentityLte) cellInfo.getCellIdentity();
        CellSignalStrengthLte signal = (CellSignalStrengthLte) cellInfo.getCellSignalStrength();

        try {
            if (cellInfo.isRegistered()) {
                putIfValid(currentCellData, "type", "LTE");
                putIfValid(currentCellData, "technology", "4G");
                putIfValid(currentCellData, "mcc", cell.getMccString());
                putIfValid(currentCellData, "mnc", cell.getMncString());
                putIfValid(currentCellData, "operator", cell.getOperatorAlphaLong());
                int ci = cell.getCi();
                if (ci != Integer.MAX_VALUE) {
                    int enodeb = ci / 256;
                    int cellId = ci % 256;
                    putIfValid(currentCellData, "cid", cellId);
                    putIfValid(currentCellData, "enodebId", enodeb);
                }
                putIfValid(currentCellData, "pci", cell.getPci());
                putIfValid(currentCellData, "tac", cell.getTac());
                putIfValid(currentCellData, "earfcn", cell.getEarfcn());
                putIfValid(currentCellData, "arfcn", cell.getEarfcn());
                putIfValid(currentCellData, "dbm", signal.getDbm());
                putIfValid(currentCellData, "asulevel", signal.getAsuLevel());
                putIfValid(currentCellData, "level", signal.getLevel());
                putIfValid(currentCellData, "rsrp", signal.getRsrp());
                putIfValid(currentCellData, "rsrq", signal.getRsrq());
                putIfValid(currentCellData, "rssi", signal.getRssi());
                putSINR(currentCellData, signal.getRsrp(), signal.getRsrq());
                putIfValid(currentCellData, "rssnr", signal.getRssnr());
                putIfValid(currentCellData, "cqi", signal.getCqi());
                putBandFromEARFCN(currentCellData, cell.getEarfcn());
            } else {
                JSObject neighbor = getNeighborObject(cell, signal);
                neighboringCells.put(neighbor);
            }
        } catch (Exception ignored) {
        }
    }
    @NonNull
    private static JSObject getNeighborObject(CellIdentityLte cell, CellSignalStrengthLte signal){
        JSObject neighbor = new JSObject();
        int enodeb = cell.getCi() / 256;
        int cellId = cell.getCi() % 256;
        neighbor.put("cid", cellId); // cell id
        neighbor.put("enodebId", enodeb); // eNodeB id
        neighbor.put("pci", cell.getPci()); // physical cell id
        neighbor.put("tac", cell.getTac()); // tracking area code
        neighbor.put("arfcn", cell.getEarfcn()); // absolute radio frequency channel number
        neighbor.put("dbm", signal.getDbm()); // signal strength in dBm
        neighbor.put("level", signal.getLevel()); // signal level
        neighbor.put("asulevel", signal.getAsuLevel()); // signal strength in ASU (arbitrary strength unit)
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
    private static void putBandFromEARFCN(JSObject json, int earfcn) {
        String bandName = "Unknown Band";
        String uplinkFrequency = "Unknown Uplink";
        String downlinkFrequency = "Unknown Downlink";

        if (earfcn >= 0 && earfcn <= 599) {
            bandName = "L1 (2100 MHz)";
            uplinkFrequency = "1920–1980 MHz";
            downlinkFrequency = "2110–2170 MHz";
        } else if (earfcn >= 600 && earfcn <= 1199) {
            bandName = "L2 (1900 MHz)";
            uplinkFrequency = "1850–1910 MHz";
            downlinkFrequency = "1930–1990 MHz";
        } else if (earfcn >= 1200 && earfcn <= 1949) {
            bandName = "L3 (1800 MHz)";
            uplinkFrequency = "1710–1785 MHz";
            downlinkFrequency = "1805–1880 MHz";
        } else if (earfcn >= 1950 && earfcn <= 2399) {
            bandName = "L4 (AWS 1700/2100 MHz)";
            uplinkFrequency = "1710–1755 MHz";
            downlinkFrequency = "2110–2155 MHz";
        } else if (earfcn >= 2400 && earfcn <= 2649) {
            bandName = "L5 (850 MHz)";
            uplinkFrequency = "824–849 MHz";
            downlinkFrequency = "869–894 MHz";
        } else if (earfcn >= 3450 && earfcn <= 3799) {
            bandName = "L8 (900 MHz)";
            uplinkFrequency = "880–915 MHz";
            downlinkFrequency = "925–960 MHz";
        } else if (earfcn >= 23000 && earfcn <= 23999) {
            bandName = "L40 (2300 MHz)";
            uplinkFrequency = "2300–2400 MHz";
            downlinkFrequency = "2300–2400 MHz"; // TDD Band
        } else if (earfcn >= 39650 && earfcn <= 41589) {
            bandName = "L41 (2500 MHz)";
            uplinkFrequency = "2496–2690 MHz";
            downlinkFrequency = "2496–2690 MHz"; // TDD Band
        } else if (earfcn >= 27500 && earfcn <= 27999) {
            bandName = "L45 (1500 MHz)";
            uplinkFrequency = "1447–1467 MHz";
            downlinkFrequency = "1447–1467 MHz"; // TDD Band
        } else if (earfcn >= 28000 && earfcn <= 28999) {
            bandName = "L46 (5200 MHz)";
            uplinkFrequency = "5150–5925 MHz";
            downlinkFrequency = "5150–5925 MHz"; // TDD Band
        }

        json.put("band", bandName);
        json.put("uplink_frequency", uplinkFrequency);
        json.put("downlink_frequency", downlinkFrequency);
    }
    private static void putSINR(JSObject json, int rsrp, int rsrq) {
        if (rsrp == Integer.MAX_VALUE || rsrq == Integer.MAX_VALUE) {
            return; // Invalid values
        }
        double sinr = rsrp - rsrq;
        json.put("sinr", sinr);
    }
}