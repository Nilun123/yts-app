# YTS 4K Movies Android App

An Android WebView application that automatically opens the latest 4K YTS movie collection.

## Automatic APK Build with GitHub Actions

This repository includes a pre-configured **GitHub Actions Workflow** (`.github/workflows/build-apk.yml`).

### How to get the APK from GitHub:
1. Push / Upload this repository to **GitHub**.
2. Go to the **Actions** tab in your GitHub repository.
3. You will see the **Build Android APK** workflow running automatically.
4. Once completed (green checkmark), click on the workflow run.
5. Under **Artifacts** at the bottom, download **`YTS-4K-Movies-App.zip`** which contains your ready-to-install **`app-debug.apk`**!

## Features
- Direct 4K YTS latest movies loader.
- Pull-to-refresh support.
- Built-in handling for Magnet links, Torrent downloads, and external downloaders (uTorrent, LibreTorrent, Flud).
- Hardware-accelerated smooth WebView.
- Back navigation support.
