package com.elsapiens.plugins.signalstrength;
import android.telephony.CellIdentityNr;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrengthNr;
import androidx.annotation.NonNull;
import com.getcapacitor.JSObject;
import org.json.JSONArray;
public class NrCellProcessor implements CellProcessor {
    static Object[][] nrBands = {
            {422000, 434000, "N1 (2110-2170 MHz)", "1920-1980 MHz", "2110-2170 MHz"},
            {386000, 398000, "N2 (1930-1990 MHz)", "1850-1910 MHz", "1930-1990 MHz"},
            {361000, 376000, "N3 (1805-1880 MHz)", "1710 Mhz -1785 MHz", "1805-1880 MHz"},
            {173800, 178800, "N5 (869-894MHz)", "824 -849 MHz", "869-894MHz"},
            {524000, 538000, "N7 (2620-2690 MHz)", "2500-2570 MHz", "2620-2690 MHz"},
            {185000, 192000, "N8 (925-960 MHz)", "880-915 MHz", "925-960 MHz"},
            {145800, 149200, "N12 (729 Mhz-746 Mhz)", "699 Mhz-716 Mhz", "729 Mhz-746 Mhz"},
            {158200, 164200, "N20 (791-821 MHz)", "832-862 MHz", "791-821 MHz"},
            {386000, 399000, "N25 (1930 Mhz-1995 Mhz)", "1850 Mhz-1915 Mhz", "1930 Mhz-1995 Mhz"},
            {151600, 160600, "N28 (758-803 MHz)", "703-748 MHz", "758-803 MHz"},
            {402000, 405000, "N34 (2010 Mhz-2025 Mhz)", "2010 Mhz-2025 Mhz", "2010 Mhz-2025 Mhz"},
            {514002, 523998, "N38 (2570-2620 MHz)", "2570-2620 MHz", "2570-2620 MHz"},
            {376000, 384000, "N39 (1880 Mhz- 1920 Mhz)", "1880 Mhz - 1920 Mhz", "1880 Mhz- 1920 Mhz"},
            {460000, 480000, "N40 (2300 Mhz - 2400 Mhz)", "2300 Mhz - 2400 Mhz", "2300 Mhz - 2400 Mhz"},
            {499200, 537999, "N41 (2496-2690 MHz)", "2496-2690 MHz", "2496-2690 MHz"},
            {286400, 303400, "N50 (1432-1517 MHz)", "1432-1517 MHz", "1432-1517 MHz"},
            {285400, 286400, "N51 (1427-1432 MHz)", "1427-1432 MHz", "1427-1432 MHz"},
            {422000, 440000, "N66 (2110-2200 MHz)", "1710-1780 MHz", "2110-2200 MHz"},
            {399000, 404000, "N70 (1995-2020 MHz)", "1695-1710 MHz ", "1995-2020 MHz"},
            {123400, 130400, "N71 (617-652 MHz)", "663-698 MHz", "617-652 MHz"},
            {295000, 303600, "N74 (1475-1518 MHz)", "1427-1470 MHz", "1475-1518 MHz"},
            {286400, 303400, "N75 (1432 Mhz -1517 MHz)", "", "1432 Mhz -1517 MHz"},
            {285400, 286400, "N76 (1427 Mhz -1432 MHz)", "", "1427 Mhz -1432 MHz"},
            {620000, 653333, "N78 (3300 MHz - 3800 MHz)", "3300-3800 MHz", "3300 MHz - 3800 MHz"},
            {620000, 680000, "N77 (3300- 4200 MHz)", "3300- 4200 MHz", "3300- 4200 MHz"},
            {693333, 733333, "N79 (4400 MHz - 5000 MHz)", "4400-5000 MHz", "4400 MHz - 5000 MHz"},
            {2054167, 2104166, "N257 (26500 MHz29500 MHz)", "26500 MHz29500 MHz", "26500 MHz29500 MHz"},
            {2016667, 2070833, "N258 (24250 MHz27500 MHz)", "24250 MHz27500 MHz", "24250 MHz27500 MHz"},
            {2229167, 2279166, "N260 (37000 MHz40000 MHz)", "37000 MHz40000 MHz", "37000 MHz40000 MHz"},
            {2070833, 2084999, "N261 (27500 MHz 28350 MH)", "27500 MHz 28350 MH", "27500 MHz 28350 MH"},
            {2399167, 2415831, "N262 (47200 MHz 48200 MHz)", "47200 MHz 48200 MHz", "47200 MHz 48200 MHz"},
    };
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
                currentCellData.put("nrarfcn", cell.getNrarfcn()); // Absolute Frequency Number
                currentCellData.put("arfcn", cell.getNrarfcn()); // Absolute Frequency Number
                // 5G Signal Strength Metrics
                putIfValid(currentCellData, "rsrp", signal.getSsRsrp()); // SS Reference Signal Received Power
                putIfValid(currentCellData, "rsrq", signal.getSsRsrq()); // SS Reference Signal Received Quality
                putIfValid(currentCellData, "sinr", signal.getSsSinr()); // SS Signal-to-Interference-plus-Noise Ratio
                putIfValid(currentCellData, "rssi", getRssiFromSignal(signal)); // RSSI
                putIfValid(currentCellData, "dbm", signal.getDbm()); // Signal strength in dBm
                if(currentCellData.has("sinr")) {
                    currentCellData.put("cqi", estimateCqi(currentCellData.getInteger("sinr")));
                }

                putBandFromNRARFCN(currentCellData, cell.getNrarfcn()); // Determine 5G Band
            } else {
                JSObject neighbor = getNeighborObject(cell, signal);
                neighboringCells.put(neighbor);
            }
        } catch (Exception ignored) {
        }
    }
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
        neighbor.put("arfcn", cell.getNrarfcn()); // Absolute Frequency Number
        neighbor.put("rsrp", signal.getSsRsrp());
        neighbor.put("rsrq", signal.getSsRsrq());
        neighbor.put("sinr", signal.getSsSinr());
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
        for (Object[] band : nrBands) {
            int lowerBound = (int) band[0];
            int upperBound = (int) band[1];
            if (nrarfcn >= lowerBound && nrarfcn <= upperBound) {
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
    private static int getRssiFromSignal(CellSignalStrengthNr signal) {
        int rsrp = signal.getSsRsrp();
        int rsrq = signal.getSsRsrq();
        if (rsrp != Integer.MAX_VALUE && rsrq != Integer.MAX_VALUE) {
            return rsrp + rsrq;
        }
        return -1;
    }
    private static int estimateCqi(int sinr) {
        if (sinr < -6) return 0;
        else if (sinr < -4) return 1;
        else if (sinr < -2) return 2;
        else if (sinr < 0) return 3;
        else if (sinr < 2) return 4;
        else if (sinr < 4) return 5;
        else if (sinr < 6) return 6;
        else if (sinr < 8) return 7;
        else if (sinr < 10) return 8;
        else if (sinr < 12) return 9;
        else if (sinr < 14) return 10;
        else if (sinr < 16) return 11;
        else if (sinr < 18) return 12;
        else if (sinr < 20) return 13;
        else if (sinr < 22) return 14;
        else return 15;
    }
}