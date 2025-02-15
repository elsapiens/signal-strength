import type { PluginListenerHandle} from '@capacitor/core';
import { WebPlugin } from '@capacitor/core';

import type {  SignalStrengthPlugin, SignalStrengthResult } from './definitions';
import { CellType, DataConnectionType, NetworkType } from './definitions';

export class SignalStrengthWeb extends WebPlugin implements SignalStrengthPlugin {
  private selectedNetworkType: NetworkType = NetworkType.FourG;
  private isOnCall = false;
  private simCount = 1;
  private dataConnectionType: DataConnectionType = DataConnectionType.MOBILE;

  async setNetworkType({ networkType }: { networkType: NetworkType }): Promise<void> {
    this.selectedNetworkType = networkType;
    console.log(`Network type set to ${networkType}`);
  }

  async setSimCount({ simCount }: { simCount: number }): Promise<void> {
    this.simCount = simCount;
    console.log(`Sim count set to ${simCount}`);
  }

  private intervalId: NodeJS.Timeout | null = null;

  async openNetworkSettings(): Promise<void> {
    console.log('openNetworkSettings');
  }

  async openWifiSettings(): Promise<void> {
    console.log('openWifiSettings');
  }

  async isMultiSim(): Promise<{ isMultiSim: boolean }> {
    return { isMultiSim: true };
  }

  async getActiveSIMCount(): Promise<{ activeSimCount: number }> {
    return { activeSimCount: 1 };
  }

  async makeCall({ number }: { number: string }): Promise<void> {
    console.log('makeCall', number);
    this.isOnCall = true;
  }

  async disconnectCall(): Promise<void> {
    console.log('disconnectCall');
    this.isOnCall = false;
  }

  async startMonitoring({ technology }: { technology: NetworkType }): Promise<void> {
    console.log('start Monitoring....');
    this.selectedNetworkType = technology;
    if (this.intervalId !== null) {
      console.warn('Monitoring is already running');
      return;
    }

    this.intervalId = setInterval(() => {
      console.log('Monitoring signal strength ....');
      
      const signalStrength = this.generateRandomSignalStrength();
      this.notifyListeners('signalUpdate', signalStrength);
    }, 1000);
    console.log('Started monitoring');
    
  }

  async removeAllListeners(): Promise<void> {
    console.log('removeAllListeners');
    super.removeAllListeners();
  }

  async addListener(eventName: string, listenerFunc: (data: any) => void): Promise<PluginListenerHandle> {
    console.log('addListener', eventName);
    return super.addListener(eventName, listenerFunc);
  }

  async stopMonitoring(): Promise<void> {
    if (this.intervalId !== null) {
      clearInterval(this.intervalId);
      this.intervalId = null;
      console.log('Stopped monitoring');
    }
  }

  async setDataConnectionType({ dataConnectionType }: { dataConnectionType: DataConnectionType }): Promise<void> {
    this.dataConnectionType = dataConnectionType;
    console.log('setDataConnectionType', dataConnectionType);
  }

  private generateRandomSignalStrength(): SignalStrengthResult {
    let selectedNetwork:NetworkType = Object.values(NetworkType)[Math.floor(Math.random() * Object.values(NetworkType).length)] as NetworkType;
    if (this.selectedNetworkType !== NetworkType.All) {
      selectedNetwork = this.selectedNetworkType;
    }
    const forreturn: SignalStrengthResult = {
      status: 'success',
      isMultiSim: true,
      simCount: this.simCount,
      isOnCall: this.isOnCall,
      dataConnectionType: this.dataConnectionType,
    };
    if (forreturn.dataConnectionType !== DataConnectionType.NO_CONNECTION) {
      forreturn.speed = {
        download: Math.floor(Math.random() * 1000),
        upload: Math.floor(Math.random() * 500),
      };
    }
    (forreturn.currentCell = {
      type: CellType.LTE,
      technology: selectedNetwork,
      mcc: '310',
      mnc: '260',
      operator: 'T-Mobile',
      cid: Math.floor(Math.random() * 100000),
      pci: Math.floor(Math.random() * 100),
      tac: Math.floor(Math.random() * 5000),
      arfcn: Math.floor(Math.random() * 2000),
      rxlev: -1 * (Math.floor(Math.random() * 50) + 50),
      asulevel: Math.floor(Math.random() * 30),
      level: Math.floor(Math.random() * 5),
      rsrp: -1 * (Math.floor(Math.random() * 50) + 80),
      rsrq: -1 * (Math.floor(Math.random() * 10) + 10),
      sssinr: Math.floor(Math.random() * 30),
      band: Math.floor(Math.random() * 100),
      cqi: Math.floor(Math.random() * 15),
    }),
      (forreturn.neighboringCells = [
        {
          type: CellType.LTE,
          mcc: '310',
          mnc: '260',
          cid: Math.floor(Math.random() * 100000),
          pci: Math.floor(Math.random() * 100),
          tac: Math.floor(Math.random() * 5000),
          arfcn: Math.floor(Math.random() * 2000),
          dbm: -1 * (Math.floor(Math.random() * 50) + 50),
          asulevel: Math.floor(Math.random() * 30),
          level: Math.floor(Math.random() * 5),
        },
      ]);
    return forreturn;
  }
}
