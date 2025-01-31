import type { ListenerCallback, PluginListenerHandle } from '@capacitor/core';
import { WebPlugin } from '@capacitor/core';

import type { SignalStrengthPlugin, SignalStrengthResult } from './definitions';

export class SignalStrengthWeb extends WebPlugin implements SignalStrengthPlugin {
  private intervalId: number | null = null;

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
  }

  async disconnectCall(): Promise<void> {
    console.log('disconnectCall');
  }

  async startMonitoring(): Promise<void> {
    console.log('startMonitoring');

    if (this.intervalId !== null) {
      console.warn('Monitoring is already running');
      return;
    }

    this.intervalId = window.setInterval(() => {
      const signalStrength = this.generateRandomSignalStrength();
      this.notifyListeners('signalUpdate', signalStrength);
    }, 1000);
  }

  async stopMonitoring(): Promise<void> {
    if (this.intervalId !== null) {
      clearInterval(this.intervalId);
      this.intervalId = null;
      console.log('Stopped monitoring');
    }
  }

  addListener(eventName: 'signalUpdate', listenerFunc: ListenerCallback): Promise<PluginListenerHandle> {
    return super.addListener(eventName, listenerFunc);
  }

  private generateRandomSignalStrength(): SignalStrengthResult {
    const networkTypes: ("2G"| "3G" | "4G" | "5G" )[] = [ '2G', '3G', '4G', '5G' ];
    const selectedNetwork = networkTypes[Math.floor(Math.random() * networkTypes.length)];

    return {
      status: 'success',
      networkType: selectedNetwork,
      speed: {
        download: Math.floor(Math.random() * 1000),
        upload: Math.floor(Math.random() * 500),
      },
      currentCell: {
        type: 'LTE',
        technology: selectedNetwork,
        mcc: '310',
        mnc: '260',
        operator: 'T-Mobile',
        cid: Math.floor(Math.random() * 100000),
        pci: Math.floor(Math.random() * 100),
        tac: Math.floor(Math.random() * 5000),
        arfcn: Math.floor(Math.random() * 2000),
        dbm: -1 * (Math.floor(Math.random() * 50) + 50),
        asulevel: Math.floor(Math.random() * 30),
        level: Math.floor(Math.random() * 5),
        rsrp: -1 * (Math.floor(Math.random() * 50) + 80),
        rsrq: -1 * (Math.floor(Math.random() * 10) + 10),
        sssinr: Math.floor(Math.random() * 30),
        band: Math.floor(Math.random() * 100),
        cqi: Math.floor(Math.random() * 15),
      },
      neighboringCells: [
        {
          type: 'LTE',
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
      ],
    };
  }
}