// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "ElsapiensSignalStrength",
    platforms: [.iOS(.v14)],
    products: [
        .library(
            name: "ElsapiensSignalStrength",
            targets: ["SignalStrengthPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "7.0.0")
    ],
    targets: [
        .target(
            name: "SignalStrengthPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/SignalStrengthPlugin"),
        .testTarget(
            name: "SignalStrengthPluginTests",
            dependencies: ["SignalStrengthPlugin"],
            path: "ios/Tests/SignalStrengthPluginTests")
    ]
)