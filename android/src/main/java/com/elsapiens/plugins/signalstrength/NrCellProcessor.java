package com.elsapiens.plugins.signalstrength;
import android.os.Build;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityNr;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthNr;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import com.getcapacitor.JSObject;
import org.json.JSONArray;
public class NrCellProcessor extends CellProcessor {
    static Object[][] nrBands = {
            {422000, 434000, "N1 (2110-2170 MHz)", "1920-1980 MHz", "2110-2170 MHz", 60, 325},
            {386000, 398000, "N2 (1930-1990 MHz)", "1850-1910 MHz", "1930-1990 MHz", 60, 325},
            {361000, 376000, "N3 (1805-1880 MHz)", "1710 Mhz -1785 MHz", "1805-1880 MHz", 75, 400},
            {173800, 178800, "N5 (869-894MHz)", "824 -849 MHz", "869-894MHz", 25, 133},
            {524000, 538000, "N7 (2620-2690 MHz)", "2500-2570 MHz", "2620-2690 MHz", 70, 380},
            {185000, 192000, "N8 (925-960 MHz)", "880-915 MHz", "925-960 MHz", 35, 180},
            {145800, 149200, "N12 (729 Mhz-746 Mhz)", "699 Mhz-716 Mhz", "729 Mhz-746 Mhz", 17, 90},
            {158200, 164200, "N20 (791-821 MHz)", "832-862 MHz", "791-821 MHz", 30, 160},
            {386000, 399000, "N25 (1930 Mhz-1995 Mhz)", "1850 Mhz-1915 Mhz", "1930 Mhz-1995 Mhz", 65, 340},
            {151600, 160600, "N28 (758-803 MHz)", "703-748 MHz", "758-803 MHz", 35, 180},
            {402000, 405000, "N34 (2010 Mhz-2025 Mhz)", "2010 Mhz-2025 Mhz", "2010 Mhz-2025 Mhz", 15, 79},
            {514002, 523998, "N38 (2570-2620 MHz)", "2570-2620 MHz", "2570-2620 MHz", 50, 273},
            {376000, 384000, "N39 (1880 Mhz- 1920 Mhz)", "1880 Mhz - 1920 Mhz", "1880 Mhz- 1920 Mhz", 40, 217},
            {460000, 480000, "N40 (2300 Mhz - 2400 Mhz)", "2300 Mhz - 2400 Mhz", "2300 Mhz - 2400 Mhz", 100, 500},
            {499200, 537999, "N41 (2496-2690 MHz)", "2496-2690 MHz", "2496-2690 MHz", 194, 970},
            {286400, 303400, "N50 (1432-1517 MHz)", "1432-1517 MHz", "1432-1517 MHz", 85, 450},
            {285400, 286400, "N51 (1427-1432 MHz)", "1427-1432 MHz", "1427-1432 MHz", 5, 25},
            {422000, 440000, "N66 (2110-2200 MHz)", "1710-1780 MHz", "2110-2200 MHz", 90, 480},
            {399000, 404000, "N70 (1995-2020 MHz)", "1695-1710 MHz ", "1995-2020 MHz", 25, 133},
            {123400, 130400, "N71 (617-652 MHz)", "663-698 MHz", "617-652 MHz", 35, 180},
            {295000, 303600, "N74 (1475-1518 MHz)", "1427-1470 MHz", "1475-1518 MHz", 43, 230},
            {286400, 303400, "N75 (1432 Mhz -1517 MHz)", "", "1432 Mhz -1517 MHz", 85, 450},
            {285400, 286400, "N76 (1427 Mhz -1432 MHz)", "", "1427 Mhz -1432 MHz", 5, 25},
            {620000, 680000, "N77 (3300- 4200 MHz)", "3300- 4200 MHz", "3300- 4200 MHz", 900, 4500},
            {620000, 653333, "N78 (3300 MHz - 3800 MHz)", "3300-3800 MHz", "3300 MHz - 3800 MHz", 500, 2500},
            {693333, 733333, "N79 (4400 MHz - 5000 MHz)", "4400-5000 MHz", "4400 MHz - 5000 MHz", 600, 3000},
            {2054167, 2104166, "N257 (26500 MHz29500 MHz)", "26500 MHz29500 MHz", "26500 MHz29500 MHz", 3000, 15000},
            {2016667, 2070833, "N258 (24250 MHz27500 MHz)", "24250 MHz27500 MHz", "24250 MHz27500 MHz", 3250, 16250},
            {2229167, 2279166, "N260 (37000 MHz40000 MHz)", "37000 MHz40000 MHz", "37000 MHz40000 MHz", 3000, 15000},
            {2070833, 2084999, "N261 (27500 MHz 28350 MH)", "27500 MHz 28350 MH", "27500 MHz 28350 MH", 850, 4250},
            {2399167, 2415831, "N262 (47200 MHz 48200 MHz)", "47200 MHz 48200 MHz", "47200 MHz 48200 MHz", 1000, 5000}
    };
    @Override
    public void processCell(CellInfo cellInfo, CellInfo nrCellInfo, JSObject currentCellData, TelephonyManager telephonyManager, JSONArray neighboringCells) {
        CellIdentityNr cell = (CellIdentityNr) cellInfo.getCellIdentity();
        CellSignalStrengthNr signal = (CellSignalStrengthNr) cellInfo.getCellSignalStrength();
        String technology = "NR";
        try {
            if (cellInfo.isRegistered()) {
                currentCellData.put("type", "NR");
                currentCellData.put("technology", "5G");
                currentCellData.put("mcc", cell.getMccString());
                currentCellData.put("mnc", cell.getMncString());
                currentCellData.put("operator", cell.getOperatorAlphaLong());
                putIfValid(currentCellData, "cid", cell.getNci());

                if(currentCellData.has("cid")) {
                    long nci = cell.getNci();
                    long gNodeB = nci >> 10; // First 26 bits
                    long cellId = nci & 0x3FF; // Last 10 bits

                    putIfValid(currentCellData, "nci", nci); // Full NR Cell Identity
                    putIfValid(currentCellData, "gnodebId", gNodeB); // gNodeB ID
                    putIfValid(currentCellData, "cellId", cellId); // 5G Cell ID
                }
                putIfValid(currentCellData, "pci", cell.getPci()); // Physical Cell ID
                putIfValid(currentCellData, "tac", cell.getTac()); // Tracking Area Code
                currentCellData.put("nrarfcn", cell.getNrarfcn()); // Absolute Frequency Number
                // 5G Signal Strength Metrics
                putIfValid(currentCellData, "rsrp", signal.getSsRsrp()); // SS Reference Signal Received Power
                putIfValid(currentCellData, "rsrq", signal.getSsRsrq()); // SS Reference Signal Received Quality
                putIfValid(currentCellData, "sssinr", signal.getSsSinr()); // SS Signal to Interference plus Noise Ratio
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    putIfValid(currentCellData, "ta", signal.getTimingAdvanceMicros()); // Timing Advance
                }
                if(currentCellData.has("rsrp") && currentCellData.has("nrarfcn")) {
                    putIfValid(currentCellData, "rssi", getRssiFromSignal(currentCellData.getInteger("rsrp"), currentCellData.getInteger("nrarfcn"))); // RSSI
                }
                putIfValidAsu(currentCellData, signal.getAsuLevel()); // Arbitrary Strength Unit
                if(currentCellData.has("sssinr")) {
                    currentCellData.put("cqi", estimateCqi(currentCellData.getInteger("ssinr")));
                }

                putBandFromNRARFCN(currentCellData, cell.getNrarfcn()); // Determine 5G Band
            } else if (cell.getNci() > 0 && cell.getNci() != Integer.MAX_VALUE) {
                JSObject neighbor = getNeighborObject(cell, signal);
                neighboringCells.put(neighbor);
            }
        } catch (Exception ignored) {
        }
    }
    @NonNull
    protected JSObject getNeighborObject(CellIdentity cell, CellSignalStrength signal) {
        CellIdentityNr nrCell = (CellIdentityNr) cell;
        CellSignalStrengthNr nrSignal = (CellSignalStrengthNr) signal;
        JSObject neighbor = new JSObject();

        long nci = nrCell.getNci();
        long gNodeB = nci >> 10;
        long cellId = nci & 0x3FF;

        neighbor.put("cid", nrCell.getNci()); // NR Cell ID
        neighbor.put("nci", nci); // Full NR Cell ID
        neighbor.put("cellId", cellId); // Cell ID
        neighbor.put("gnodebId", gNodeB); // gNodeB ID
        neighbor.put("pci", nrCell.getPci()); // Physical Cell ID
        neighbor.put("tac", nrCell.getTac()); // Tracking Area Code
        neighbor.put("nrarfcn", nrCell.getNrarfcn()); // Absolute Frequency Number
        neighbor.put("arfcn", nrCell.getNrarfcn()); // Absolute Frequency Number
        neighbor.put("rsrp", nrSignal.getSsRsrp());
        neighbor.put("rsrq", nrSignal.getSsRsrq());
        neighbor.put("sssinr", nrSignal.getSsSinr());

        return neighbor;
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
        json.put("uplinkFrequency", uplinkFrequency);
        json.put("downlinkFrequency", downlinkFrequency);
    }

    public static int gerNBlock(int nrArfcn) {
        for (Object[] band : nrBands) {
            int lowerBound = (int) band[0];
            int upperBound = (int) band[1];
            if (nrArfcn >= lowerBound && nrArfcn <= upperBound) {
                return (int) band[5];
            }
        }
        return 0;
    }
    private static int getRssiFromSignal(int rsrp, int nrArfcn) {
        if (rsrp != Integer.MAX_VALUE) {
            Object[] bandInfo = getNrBandInfo(nrArfcn);
            int bandwidth = (int) bandInfo[1]; // Extract bandwidth
            int n = (int) bandInfo[2]; // Extract noise figure
            return (int) (rsrp + (10 * Math.log10(n)));
        }
        return Integer.MAX_VALUE;
    }

    private static Object[] getNrBandInfo(int nrArfcn) {

        for (Object[] band : nrBands) {
            int lowerBound = (int) band[0];
            int upperBound = (int) band[1];

            if (nrArfcn >= lowerBound && nrArfcn <= upperBound) {
                return new Object[]{band[2], band[3], band[6]};
            }
        }

        return new Object[]{"Unknown NR Band", 20}; // Default bandwidth if unknown
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
