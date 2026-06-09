# Copy Code Reference Plugin for Android Studio & IntelliJ IDEA

[繁體中文](README.zh-TW.md)

This plugin helps you quickly copy the reference path of the selected code in Android Studio and IntelliJ IDEA, including the module name and line numbers.

Example format:
`@app/src/main/java/com/example/app/data/repository/BookingRepository.kt#L123-138`

## Features

*   **Floating Toolbar**: When selecting code in the editor, a 📋 copy button appears in the floating code toolbar.
*   **Context Menu**: Adds the following options under the editor's right-click context menu (right below `Copy Path/Reference...`):
    *   `Copy Reference`: Copies the reference path with line numbers to your clipboard.
    *   `Copy Reference to Terminal`: Copies the reference path, activates the IDE's built-in Terminal, and automatically pastes it into the active terminal tab (prioritizing tabs named `opencode` or `claude`).

## Installation

1.  **Get the Plugin Package**:
    *   After building the project, the plugin zip file is located at: `build/distributions/copy-code-reference-plugin-1.0.0.zip`
2.  Open **Android Studio** or **IntelliJ IDEA**.
3.  Go to **Settings** (Windows/Linux) or **Preferences / Settings...** (macOS) -> **Plugins**.
4.  Click the gear icon ⚙️ in the top right and select **Install Plugin from Disk...**.
5.  Select the zip file generated in step 1.
6.  Restart the IDE.

## Usage

1.  Select a block of code in the editor.
2.  Click the 📋 icon in the floating toolbar, or right-click and choose **Copy Reference** or **Copy Reference to Terminal**.
3.  The reference path (and line numbers) is now copied to your clipboard. If you chose the terminal option, it will also be pasted into the active terminal session.

## Development & Build

### Prerequisites
*   JDK 21 (Gradle Toolchain will automatically download it if needed, but installing it locally is recommended)
*   Gradle 8.13 (Wrapper included)

### Build Command

Run the following command in the project root:

```bash
./gradlew buildPlugin
```

Once built successfully, you can find the plugin package in the `build/distributions/` directory.
