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
* [`setNetworkType(...)`](#setnetworktype)
* [`setDataConnectionType(...)`](#setdataconnectiontype)
* [Enums](#enums)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### startMonitoring(...)

```typescript
startMonitoring({ technology }: { technology: NetworkType; }) => Promise<void>
```

| Param     | Type                                                                 |
| --------- | -------------------------------------------------------------------- |
| **`__0`** | <code>{ technology: <a href="#networktype">NetworkType</a>; }</code> |

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


### setNetworkType(...)

```typescript
setNetworkType({ networkType }: { networkType: NetworkType; }) => Promise<void>
```

| Param     | Type                                                                  |
| --------- | --------------------------------------------------------------------- |
| **`__0`** | <code>{ networkType: <a href="#networktype">NetworkType</a>; }</code> |

--------------------


### setDataConnectionType(...)

```typescript
setDataConnectionType({ dataConnectionType }: { dataConnectionType: DataConnectionType; }) => Promise<void>
```

| Param     | Type                                                                                       |
| --------- | ------------------------------------------------------------------------------------------ |
| **`__0`** | <code>{ dataConnectionType: <a href="#dataconnectiontype">DataConnectionType</a>; }</code> |

--------------------


### Enums


#### NetworkType

| Members       | Value                  |
| ------------- | ---------------------- |
| **`TwoG`**    | <code>"2G"</code>      |
| **`ThreeG`**  | <code>"3G"</code>      |
| **`FourG`**   | <code>"4G"</code>      |
| **`FiveG`**   | <code>"5G"</code>      |
| **`UNKNOWN`** | <code>"UNKNOWN"</code> |
| **`All`**     | <code>"ALL"</code>     |


#### DataConnectionType

| Members             | Value                        |
| ------------------- | ---------------------------- |
| **`WIFI`**          | <code>"Wifi"</code>          |
| **`MOBILE`**        | <code>"Mobile"</code>        |
| **`UNKNOWN`**       | <code>"Unknown"</code>       |
| **`NO_CONNECTION`** | <code>"No Connection"</code> |

</docgen-api>
