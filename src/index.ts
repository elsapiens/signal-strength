import { registerPlugin } from '@capacitor/core';

import type { SignalStrengthPlugin } from './definitions';

const SignalStrength = registerPlugin<SignalStrengthPlugin>('SignalStrength', {
  web: () => import('./web').then((m) => new m.SignalStrengthWeb()),
});

export * from './definitions';
export { SignalStrength };
