export interface SignalStrengthPlugin {
  //Start monitoring the signal strength of the device with the given technology eg. "2G", "3G", "4G", "5G", "All"
  startMonitoring({ technology }: { technology: string }): Promise<void>;
  //Stop monitoring the signal strength of the device
  stopMonitoring(): Promise<void>;
  //Get the current signal strength of the device every second
  addListener(eventName: 'signalUpdate', listenerFunc: (data: any) => void): void;
  //Open the network settings of the device
  openNetworkSettings(): Promise<void>;
  //Open the wifi settings of the device
  openWifiSettings(): Promise<void>;
  //Open the mobile data settings of the device
  isMultiSim(): Promise<{ isMultiSim: boolean }>;
  //Get the active sim count of the device
  getActiveSIMCount(): Promise<{ activeSimCount: number }>;
  //make a call to the given number
  makeCall({ number }: { number: string }): Promise<void>;
  //Open the dialer to disconnect the call
  disconnectCall(): Promise<void>;
  //Set network type to monitor the signal strength for web platform(testing)
  setNetworkType({ networkType }: { networkType: "2G" | "3G" | "4G" | "5G" | "random" }): Promise<void>;

}

export interface SignalStrengthResult {
  status: "success" | "error";
  message?: string; // Present only in case of an error
  isMultiSim?: boolean;
  simCount?: number;
  isOnCall?: boolean;
  networkType: string; // "2G", "3G", "4G", "5G", "WiFi", "No Connection"
  speed?: {
    download: number; // in Kbps
    upload: number; // in Kbps
  };
  currentCell?: CurrentCellInfo;
  neighboringCells?: NeighborCellInfo[];
}

/**
 * Information about the currently connected cell
 */
export interface CurrentCellInfo {
  type: "GSM" | "WCDMA" | "LTE" | "NR"; // 2G, 3G, 4G, 5G
  technology: "2G" | "3G" | "4G" | "5G";
  mcc?: string; // Mobile Country Code
  mnc?: string; // Mobile Network Code
  operator?: string; // Operator Name
  cid?: number; // Cell ID
  pci?: number; // Physical Cell ID
  tac?: number; // Tracking Area Code / Location Area Code
  arfcn?: number; // Absolute Radio Frequency Channel Number
  dbm?: number; // Signal strength in dBm
  asulevel?: number; // Arbitrary Strength Unit (ASU)
  level?: number; // Signal level

  // Technology-specific fields:
  rsrp?: number; // Reference Signal Received Power (LTE/5G)
  rsrq?: number; // Reference Signal Received Quality (LTE/5G)
  sssinr?: number; // Signal-to-Noise and Interference Ratio (5G)
  band?: number | number[]; // Bandwidth (LTE/5G)
  cqi?: number | number[]; // Channel Quality Indicator (LTE/5G)
  ecno?: number; // Energy per Chip to Noise Ratio (WCDMA)
  psc?: number; // Primary Scrambling Code (WCDMA)
  rssi?: number; // Reference Signal Strength Indicator (LTE)
  ber?: number; // Bit Error Rate (GSM)
}

/**
 * Information about neighboring cells
 */
export interface NeighborCellInfo {
  type: "GSM" | "WCDMA" | "LTE" | "NR"; // 2G, 3G, 4G, 5G
  mcc?: string; // Mobile Country Code
  mnc?: string; // Mobile Network Code
  cid?: number; // Cell ID
  pci?: number; // Physical Cell ID
  tac?: number; // Tracking Area Code / Location Area Code
  arfcn?: number; // Absolute Radio Frequency Channel Number
  dbm?: number; // Signal strength in dBm
  asulevel?: number; // Arbitrary Strength Unit (ASU)
  level?: number; // Signal level
}