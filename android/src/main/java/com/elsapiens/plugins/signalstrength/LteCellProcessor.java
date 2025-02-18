package com.elsapiens.plugins.signalstrength;
import android.telephony.CellIdentity;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthLte;
import androidx.annotation.NonNull;
import com.getcapacitor.JSObject;
import org.json.JSONArray;
public class LteCellProcessor extends CellProcessor {
    Object[][] earfcnBands = {
            {0, 599, "2100", "1920-1980 MHz", "2110-2170 MHz", 60},
            {600, 1199, "1900 PCS", "1850-1910 MHz", "1930-1990 MHz", 60},
            {1200, 1949, "1800+", "1710-1785 MHz", "1805-1880 MHz", 75},
            {1950, 2399, "AWS-1", "1710-1755 MHz", "2110-2155 MHz", 45},
            {2400, 2649, "850", "824-849 MHz", "869-894 MHz", 25},
            {2650, 2749, "850 Japan", "830-840 MHz", "875-885 MHz", 10},
            {2750, 3449, "2600", "2500-2570 MHz", "2620-2690 MHz", 70},
            {3450, 3799, "900 GSM", "880-915 MHz", "925-960 MHz", 35},
            {3800, 4149, "1800", "1749.9-1784.9 MHz", "1844.9-1879.9 MHz", 35},
            {4150, 4749, "AWS-3", "1710-1770 MHz", "2110-2170 MHz", 60},
            {4750, 4949, "1500 Lower", "1427.9-1447.9 MHz", "1475.9-1495.9 MHz", 20},
            {5010, 5179, "700 a", "699-716 MHz", "729-746 MHz", 17},
            {5180, 5279, "700 c", "777-787 MHz", "746-756 MHz", 10},
            {5280, 5379, "700 PS", "788-798 MHz", "758-768 MHz", 10},
            {5730, 5849, "700 b", "704-716 MHz", "734-746 MHz", 12},
            {5850, 5999, "800 Lower", "815-830 MHz", "860-875 MHz", 15},
            {6000, 6149, "800 Upper", "830-845 MHz", "875-890 MHz", 15},
            {6150, 6449, "800 DD", "832-862 MHz", "791-821 MHz", 30},
            {6450, 6599, "1500 Upper", "1447.9-1462.9 MHz", "1495.9-1510.9 MHz", 15},
            {6600, 7399, "3500", "3410-3490 MHz", "3510-3590 MHz", 80},
            {7500, 7699, "2000 S-band", "2000-2020 MHz", "2180-2200 MHz", 20},
            {7700, 8039, "1600 L-band", "1626.5-1660.5 MHz", "1525-1559 MHz", 34},
            {8040, 8689, "1900+", "1850-1915 MHz", "1930-1995 MHz", 65},
            {8690, 9039, "850+", "814-849 MHz", "859-894 MHz", 35},
            {9040, 9209, "800 SMR", "807-824 MHz", "852-869 MHz", 17},
            {9210, 9659, "700 APT", "703-748 MHz", "758-803 MHz", 45},
            {9660, 9769, "700 d", "", "717-728 MHz", 11},
            {9770, 9869, "2300 WCS", "2305-2315 MHz", "2350-2360 MHz", 10},
            {9870, 9919, "450", "452.5-457.5 MHz", "462.5-467.5 MHz", 5},
            {9920, 10359, "1500 L-band", "", "1452-1496 MHz", 14},
            {36000, 36199, "TD 1900", "", "1900-1920 MHz", 20},
            {36200, 36349, "TD 2000", "", "2010-2025 MHz", 15},
            {36350, 36949, "TD PCS Lower", "", "1850-1910 MHz", 60},
            {36950, 37549, "TD PCS Upper", "", "1930-1990 MHz", 60},
            {37550, 37749, "TD PCS Center gap", "", "1910-1930 MHz", 20},
            {37750, 38249, "TD 2600", "", "2570-2620 MHz", 50},
            {38250, 38649, "TD 1900+", "", "1880-1920 MHz", 40},
            {38650, 39649, "TD 2300", "", "2300-2400 MHz", 100},
            {39650, 41589, "TD 2600+", "", "2496-2690 MHz", 194},
            {41590, 43589, "TD 3500", "", "3400-3600 MHz", 200},
            {43590, 45589, "TD 3700", "", "3600-3800 MHz", 200},
            {45590, 46589, "TD 700", "", "", 100},
            {46590, 46789, "TD 1500", "", "1447-1467 MHz", 20},
            {46790, 54539, "TD Unlicensed", "", "5150-5925 MHz", 775},
            {54540, 55239, "TD V2X", "", "5855-5925 MHz", 70},
            {55240, 56739, "TD 3600", "", "3550-3700 MHz", 150},
            {56740, 58239, "TD 3600r", "", "3550-3700 MHz", 150},
            {58240, 59089, "TD 1500+", "", "1432-1517 MHz", 85},
            {59090, 59139, "TD 1500-", "", "1427-1432 MHz", 5},
            {59140, 60139, "TD 3300", "", "3300-3400 MHz", 100},
            {60140, 60254, "TD 2500", "", "2483.5-2495 MHz", 11.5},
            {60255, 60304, "TD 1700", "", "1670-1675 MHz", 5},
            {65536, 66435, "2100+", "1920-2010 MHz", "2110-2200 MHz", 90},
            {66436, 67335, "AWS", "1710-1780 MHz", "2110-2200 MHz", 90/70},
            {67336, 67535, "700 EU", "- MHz", "738-758 MHz", 20},
            {67536, 67835, "700 ME", "698-728 MHz", "753-783 MHz", 30},
            {67836, 68335, "DL b38", "", "2570-2620 MHz", 50},
            {68336, 68585, "AWS-4", "1695-1710 MHz", "1995-2020 MHz", 25/15},
            {68586, 68935, "600", "663-698 MHz", "617-652 MHz", 35},
            {68936, 68985, "450 PMR/PAMR", "451-456 MHz", "461-466 MHz", 5},
            {68986, 69035, "450 APAC", "450-455 MHz", "460-465 MHz", 5},
            {69036, 69465, "L-band", "1427-1470 MHz", "1475-1518 MHz", 43},
            {69466, 70315, "DL b50", "", "1432-1517 MHz", 85},
            {70316, 70365, "DL b51", "", "1427-1432 MHz", 5},
            {70366, 70545, "700 a+", "698-716 MHz", "728-728 MHz", 18},
            {70546, 70595, "410", "410-415 MHz", "420-425 MHz", 5},
            {70596, 70645, "410+", "412-417 MHz", "422-427 MHz", 5},
            {70646, 70655, "NB-IoT", "787-788 MHz", "757-758 MHz", 1},
            {70656, 70705, "900", "896-901 MHz", "935-940 MHz", 5},
            {70656, 71055, "DL 600", "", "612-652 MHz", 40},
            {71056, 73335, "DL 500", "", "470-698 MHz", 228}
    };

    @Override
    public void processCell(CellInfo cellInfo, JSObject currentCellData, JSONArray neighboringCells) {
        CellIdentityLte cell = (CellIdentityLte) cellInfo.getCellIdentity();
        CellSignalStrengthLte signal = (CellSignalStrengthLte) cellInfo.getCellSignalStrength();

        try {
            if (cellInfo.isRegistered()) {
                currentCellData.put("type", "LTE");
                currentCellData.put("technology", "4G");
                currentCellData.put("mcc", cell.getMccString());
                currentCellData.put("mnc", cell.getMncString());
                currentCellData.put("operator", cell.getOperatorAlphaLong());
                putIfValid(currentCellData, "cid", cell.getCi());
                int ci = cell.getCi();
                if (ci != Integer.MAX_VALUE) {
                    int enodeb = ci / 256;
                    int cellId = ci % 256;
                    putIfValid(currentCellData, "cellId", cellId);
                    putIfValid(currentCellData, "enodebId", enodeb);
                }
                putIfValid(currentCellData, "pci", cell.getPci());
                putIfValid(currentCellData, "tac", cell.getTac());
                putIfValid(currentCellData, "earfcn", cell.getEarfcn());
                putIfValidAsu(currentCellData, signal.getAsuLevel());
                putIfValid(currentCellData, "level", signal.getLevel());
                putIfValid(currentCellData, "rsrp", signal.getRsrp());
                putIfValid(currentCellData, "rsrq", signal.getRsrq());
                putIfValid(currentCellData, "rssi", signal.getRssi());
                putSsSINR(currentCellData, signal.getRsrp(), signal.getRsrq());
                putIfValid(currentCellData, "rssnr", signal.getRssnr());
                putIfValid(currentCellData, "cqi", signal.getCqi());
                putIfValid(currentCellData, "ta", signal.getTimingAdvance());
                putBandFromEARFCN(currentCellData, cell.getEarfcn());
            } else if(cell.getCi() > 0 && cell.getCi() != Integer.MAX_VALUE) {
                JSObject neighbor = getNeighborObject(cell, signal);
                neighboringCells.put(neighbor);
            }
        } catch (Exception ignored) {
        }
    }
    @NonNull
    protected JSObject getNeighborObject(CellIdentity cell, CellSignalStrength signal){
        CellSignalStrengthLte lteSignal = (CellSignalStrengthLte) signal;
        CellIdentityLte lteCell = (CellIdentityLte) cell;
        JSObject neighbor = new JSObject();
        neighbor.put("cid", lteCell.getCi()); // Cell id
        putIfValid(neighbor, "cid", lteCell.getCi());
        if(neighbor.has("cid")) {
            int enodeb = lteCell.getCi() / 256;
            int lteCellId = lteCell.getCi() % 256;
            neighbor.put("cellId", lteCellId); // Cell id
            neighbor.put("enodebId", enodeb); // eNodeB id
        }
        neighbor.put("pci", lteCell.getPci()); // physical lteCell id
        neighbor.put("tac", lteCell.getTac()); // tracking area code
        putIfValid(neighbor, "earfcn", lteCell.getEarfcn()); // frequency number
        putIfValid(neighbor, "rsrp", lteSignal.getRsrp()); // reference signal received power
        putIfValid(neighbor, "rsrq", lteSignal.getRsrq()); // reference signal received quality
        putIfValid(neighbor, "rssi", lteSignal.getRssi()); // reference signal strength indicator
        putIfValid(neighbor, "sinr", lteSignal.getRssnr()); // signal-to-interference-plus-noise ratio
        putIfValid(neighbor, "cqi", lteSignal.getCqi()); // channel quality indicator
        putIfValidAsu(neighbor, lteSignal.getAsuLevel()); // arbitrary strength unit
        return neighbor;
    }
    private  void putBandFromEARFCN(JSObject json, int earfcn) {
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
        json.put("uplinkFrequency", uplinkFrequency);
        json.put("downlinkFrequency", downlinkFrequency);
    }
    private void putSsSINR(JSObject json, int rsrp, int rsrq) {
        if (rsrp == Integer.MAX_VALUE || rsrq == Integer.MAX_VALUE) {
            return; // Invalid values
        }
        double sssinr = rsrp - rsrq;
        json.put("sssinr", sssinr);
    }
}