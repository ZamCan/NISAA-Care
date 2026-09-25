# Initial environment record

Inspected before project creation (2026-09-25):

- Working project parent: `/data/data/com.termux/files/home`
- Java: OpenJDK 21.0.12
- Gradle executable: 9.7.1 installed; project wrapper targets 8.9
- Kotlin compiler: 2.4.20 installed; project plugin uses 2.0.21 to match the
  locally cached Android build toolchain
- Android SDK: `/data/data/com.termux/files/home/android-sdk`
- Platform: android-35
- Build tools: 34.0.0
- AAPT2: 2.20-android-16.0.0_r4 at the Termux path configured in
  `gradle.properties`
- D8: 9.2.4-dev
- ADB: 1.0.41 / Android Debug Bridge 35.0.2
- Git: 2.55.0

The host filesystem reported low free space during inspection. The project
avoids large media and cloud dependencies; release signing secrets and local
SDK properties remain outside Git.
