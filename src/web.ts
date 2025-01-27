import type { ListenerCallback, PluginListenerHandle} from '@capacitor/core';
import { WebPlugin } from '@capacitor/core';

import type { SignalStrengthPlugin } from './definitions';

export class SignalStrengthWeb extends WebPlugin implements SignalStrengthPlugin {
  async openNetworkSettings(): Promise<void> {
    throw new Error('Method not implemented.');
  }
  openWifiSettings(): Promise<void> {
    throw new Error('Method not implemented.');
  }
  isMultiSim(): Promise<{ isMultiSim: boolean; }> {
    throw new Error('Method not implemented.');
  }
  getActiveSIMCount(): Promise<{ activeSimCount: number; }> {
    throw new Error('Method not implemented.');
  }
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  makeCall({ number }: { number: string; }): Promise<void> {
    console.log('makeCall', number);
    throw new Error('Method not implemented.');
  }
  disconnectCall(): Promise<void> {
    throw new Error('Method not implemented.');
  }
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
  async startMonitoring(): Promise<void> {
    console.log('startMonitoring');
  }
  async stopMonitoring(): Promise<void> {
    console.log('stopMonitoring');
  }
  
    
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  addListener(eventName: string, _listenerFunc: ListenerCallback): Promise<PluginListenerHandle> {
      console.log('addListener', eventName);
      return Promise.resolve({
          remove: () => {
              console.log('removeListener', eventName);
              return Promise.resolve();
          }
      });
  }
}
