# Device test protocol

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.zamcan.nisaacare.debug/com.zamcan.nisaacare.MainActivity
```

For the second device, install the same debug artifact only after confirming
its package ID and consent. Test first-run language selection, Arabic RTL,
cycle entry, fertility estimate, relationship permission toggles, airplane
mode, notification lock-screen privacy, and pairing expiry. Do not use real
health records on a development device.
