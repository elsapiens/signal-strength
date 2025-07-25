export interface SignalStrengthPlugin {
  //Start monitoring the signal strength of the device with the given technology eg. "2G", "3G", "4G", "5G", "All"
  startMonitoring({ technology }: { technology: NetworkType }): Promise<void>;
  //Stop monitoring the signal strength of the device
  stopMonitoring(): Promise<void>;
  //Get the current signal strength of the device every second
  addListener(eventName: 'signalUpdate', listenerFunc: (data: any) => void): void;
  removeAllListeners(): Promise<void>;
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
  setNetworkType({ networkType }: { networkType: NetworkType }): Promise<void>;
  //Get the current signal strength of the device
  setDataConnectionType({ dataConnectionType }: { dataConnectionType: DataConnectionType }): Promise<void>;
}

export interface SignalStrengthResult {
  status: "success" | "error";
  message?: string; // Present only in case of an error
  isMultiSim?: boolean;
  simCount?: number;
  isOnCall?: boolean;
  callState?: 'Dialing' | 'Ringing' | 'Offhook' | 'Idle';
  callType?: '2G' | '3G' | '4G VoLTE' | '5G VoNR' | '4G LTE CA' | '2G GSM' | '3G TD-SCDMA' | 'WiFi Calling' | 'Unknown'
  dataConnectionType?: DataConnectionType;
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
  type: CellType; // GSM, WCDMA, LTE, NR
  technology: NetworkType; // 2G, 3G, 4G, 5G
  mcc?: string; // Mobile Country Code
  mnc?: string; // Mobile Network Code
  operator?: string; // Operator Name
  cid?: number; // Cell ID
  nci?: number; // NR Cell ID
  cellId?: number; // Cell ID
  bsic?: number; // Base Station Identity Code (GSM)
  pci?: number; // Physical Cell ID
  tac?: number; // Tracking Area Code / Location Area Code
  lac?: number; // Location Area Code
  arfcn?: number; // Absolute Radio Frequency Channel Number
  uarfcn?: number; // UTRA Absolute Radio Frequency Channel Number (WCDMA)
  earfcn?: number; // E-UTRA Absolute Radio Frequency Channel Number (LTE)
  nrarfcn?: number; // NR Absolute Radio Frequency Channel Number
  asulevel?: number; // Arbitrary Strength Unit (ASU)
  level?: number; // Signal level
  ta?: number; // Timing Advance (GSM)
  enodebId?: number; // eNodeB ID (LTE)
  gnodebId?: number; // gNodeB ID (5G)
  // Technology-specific fields:
  rsrp?: number; // Reference Signal Received Power (LTE/5G)
  rsrq?: number; // Reference Signal Received Quality (LTE/5G)
  rxlev?: number; // Received Signal Strength Indicator (GSM)
  rxqual?: number; // Received Signal Quality (GSM)
  sssinr?: number; // Signal-to-Noise and Interference Ratio (5G)
  sinr?: number; // Signal-to-Noise Ratio (LTE)
  band?: string | string[] | number | number[]; // Frequency Band (GSM, LTE, 5G)
  cqi?: number | number[]; // Channel Quality Indicator (LTE/5G)
  ecno?: number; // Energy per Chip to Noise Ratio (WCDMA)
  psc?: number; // Primary Scrambling Code (WCDMA)
  rssi?: number; // Reference Signal Strength Indicator (LTE)
  rssnr?: number; // Reference Signal to Noise Ratio (LTE)
  ber?: number; // Bit Error Rate (GSM)
}

/**
 * Information about neighboring cells
 */
export interface NeighborCellInfo {
  type: CellType; // GSM, WCDMA, LTE, NR
  mcc?: string; // Mobile Country Code
  mnc?: string; // Mobile Network Code
  cid?: number; // Cell ID
  nci?: number; // NR Cell ID
  cellId?: number; // Cell ID
  pci?: number; // Physical Cell ID
  tac?: number; // Tracking Area Code / Location Area Code
  lac?: number; // Location Area Code
  bsic?: number; // Base Station Identity Code (GSM)
  rxlev?: number; // Received Signal Strength Indicator (GSM)
  ber?: number; // Bit Error Rate (GSM)
  rxqual?: number; // Received Signal Quality (GSM)
  enodebId?: number; // eNodeB ID (LTE)
  gnodebId?: number; // gNodeB ID (5G)
  rsrp?: number; // Reference Signal Received Power (LTE/5G)
  rsrq?: number; // Reference Signal Received Quality (LTE/5G)
  rssi?: number; // Reference Signal Strength Indicator (LTE)
  sssinr?: number; // Signal-to-Noise and Interference Ratio (5G)
  arfcn?: number; // Absolute Radio Frequency Channel Number
  earfcn?: number; // E-UTRA Absolute Radio Frequency Channel Number (LTE)
  uarfcn?: number; // UTRA Absolute Radio Frequency Channel Number (WCDMA)
  narfcn?: number; // NR Absolute Radio Frequency Channel Number
  dbm?: number; // Signal strength in dBm
  asulevel?: number; // Arbitrary Strength Unit (ASU)
  level?: number; // Signal level
  uplinkFrequency?: number; // Uplink Frequency 
  downlinkFrequency?: number; // Downlink Frequency
}

export enum DataConnectionType {
  WIFI = "Wifi",
  MOBILE = "Mobile",
  UNKNOWN = "Unknown",
  NO_CONNECTION = "No Connection"
 }

export enum NetworkType { 
  TwoG = "2G", 
  ThreeG = "3G", 
  FourG = "4G", 
  FiveG = "5G", 
  UNKNOWN = "UNKNOWN",
  All = "ALL" 
}

export enum CellType { 
  GSM = "GSM", 
  WCDMA = "WCDMA", 
  LTE = "LTE", 
  NR = "NR",
  UNKNOWN = "UNKNOWN"
 }