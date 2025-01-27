# elsapiens-signal-strength

To get signal strength details from Android devices for each technology (5G, 4G, 3G, 2G) including neighboring cell info every second.

## Install

```bash
npm install elsapiens-signal-strength
npx cap sync
```

## API

<docgen-index>

* [`startMonitoring(...)`](#startmonitoring)
* [`stopMonitoring()`](#stopmonitoring)
* [`addListener('signalUpdate', ...)`](#addlistenersignalupdate-)
* [`openNetworkSettings()`](#opennetworksettings)
* [`openWifiSettings()`](#openwifisettings)
* [`isMultiSim()`](#ismultisim)
* [`getActiveSIMCount()`](#getactivesimcount)
* [`makeCall(...)`](#makecall)
* [`disconnectCall()`](#disconnectcall)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### startMonitoring(...)

```typescript
startMonitoring({ technology }: { technology: string; }) => Promise<void>
```

| Param     | Type                                 |
| --------- | ------------------------------------ |
| **`__0`** | <code>{ technology: string; }</code> |

--------------------


### stopMonitoring()

```typescript
stopMonitoring() => Promise<void>
```

--------------------


### addListener('signalUpdate', ...)

```typescript
addListener(eventName: 'signalUpdate', listenerFunc: (data: any) => void) => void
```

| Param              | Type                                |
| ------------------ | ----------------------------------- |
| **`eventName`**    | <code>'signalUpdate'</code>         |
| **`listenerFunc`** | <code>(data: any) =&gt; void</code> |

--------------------


### openNetworkSettings()

```typescript
openNetworkSettings() => Promise<void>
```

--------------------


### openWifiSettings()

```typescript
openWifiSettings() => Promise<void>
```

--------------------


### isMultiSim()

```typescript
isMultiSim() => Promise<{ isMultiSim: boolean; }>
```

**Returns:** <code>Promise&lt;{ isMultiSim: boolean; }&gt;</code>

--------------------


### getActiveSIMCount()

```typescript
getActiveSIMCount() => Promise<{ activeSimCount: number; }>
```

**Returns:** <code>Promise&lt;{ activeSimCount: number; }&gt;</code>

--------------------


### makeCall(...)

```typescript
makeCall({ number }: { number: string; }) => Promise<void>
```

| Param     | Type                             |
| --------- | -------------------------------- |
| **`__0`** | <code>{ number: string; }</code> |

--------------------


### disconnectCall()

```typescript
disconnectCall() => Promise<void>
```

--------------------

</docgen-api>
