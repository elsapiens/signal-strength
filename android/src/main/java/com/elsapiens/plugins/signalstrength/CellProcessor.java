package com.elsapiens.plugins.signalstrength;
import android.telephony.CellInfo;

import org.json.JSONArray;
import com.getcapacitor.JSObject;

public interface CellProcessor  {
    void processCell(CellInfo cellInfo, JSObject currentCellData, JSONArray neighboringCells);
}