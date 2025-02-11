package com.elsapiens.plugins.signalstrength;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrengthLte;
import androidx.annotation.NonNull;
import com.getcapacitor.JSObject;
import org.json.JSONArray;
public class LteCellProcessor implements CellProcessor {
    static Object[][] earfcnBands = {
            {0, 599, "L1 (2100 MHz)", "1920–1980 MHz", "2110–2170 MHz"},
            {600, 1199, "L2 (1900 MHz)", "1850–1910 MHz", "1930–1990 MHz"},
            {1200, 1949, "L3 (1800 MHz)", "1710–1785 MHz", "1805–1880 MHz"},
            {1950, 2399, "L4 (AWS 1700/2100 MHz)", "1710–1755 MHz", "2110–2155 MHz"},
            {2400, 2649, "L5 (850 MHz)", "824–849 MHz", "869–894 MHz"},
            {3450, 3799, "L8 (900 MHz)", "880–915 MHz", "925–960 MHz"},
            {23000, 23999, "L40 (2300 MHz)", "2300–2400 MHz", "2300–2400 MHz"},
            {39650, 41589, "L41 (2500 MHz)", "2496–2690 MHz", "2496–2690 MHz"},
            {27500, 27999, "L45 (1500 MHz)", "1447–1467 MHz", "1447–1467 MHz"},
            {28000, 28999, "L46 (5200 MHz)", "5150–5925 MHz", "5150–5925 MHz"},
    };

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

        for (Object[] band : earfcnBands) {
            int lowerBound = (int) band[0];
            int upperBound = (int) band[1];
            if (earfcn >= lowerBound && earfcn <= upperBound) {
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
    private static void putSINR(JSObject json, int rsrp, int rsrq) {
        if (rsrp == Integer.MAX_VALUE || rsrq == Integer.MAX_VALUE) {
            return; // Invalid values
        }
        double sinr = rsrp - rsrq;
        json.put("sinr", sinr);
    }
}