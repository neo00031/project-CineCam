# Project Cinecom v0.4

A clean Android Camera2 foundation for a professional mobile cinema camera.

## What this version actually implements
- Camera2 rear-camera preview.
- HEVC recording through MediaCodec + MediaMuxer.
- Device capability detection for Android 13+ 10-bit dynamic-range capture.
- HLG10/HDR10 selection only when Camera2 reports support.
- Main10 HEVC encoder profile and BT.2020 transfer metadata for supported HDR modes.
- No fake 10-bit toggle: unsupported profiles remain unavailable.

## Important
Cinecom Log is NOT claimed as implemented in v0.4. Generic Android cannot promise a sensor-native Log feed across phones. The next engine stage will implement a separate Log pipeline and device-specific capability handling. The reference app's Log system is more extensive than Android's public HDR API.

## Build
Open in Android Studio or run the included GitHub Actions workflow.
