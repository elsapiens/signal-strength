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

}
