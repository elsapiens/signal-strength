import { WebPlugin } from '@capacitor/core';

import type { SignalStrengthPlugin } from './definitions';

export class SignalStrengthWeb extends WebPlugin implements SignalStrengthPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
