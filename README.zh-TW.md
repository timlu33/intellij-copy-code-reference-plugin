# Copy Code Reference Plugin for Android Studio & IntelliJ IDEA

[English](README.md)

這個 Plugin 可以在 Android Studio 與 IntelliJ IDEA 中，快速複製選取程式碼的參考路徑，格式包含 Module 名稱與行號。

格式範例：
`@app/src/main/java/com/example/app/data/repository/BookingRepository.kt#L123-138`

## 功能

*   **Floating Toolbar (浮動工具列)**：選取程式碼時，浮動工具列會出現 📋 複製按鈕。
*   **Context Menu (右鍵選單)**：在編輯器右鍵選單中，`Copy Path/Reference...` 下方新增以下選項：
    *   `Copy Reference`：複製包含行號的程式碼參考路徑至剪貼簿。
    *   `Copy Reference with ELI5`：複製 ELI5（Explain Like I'm 5）提示詞與程式碼參考路徑，方便直接貼到 AI 助手，取得簡單、逐步的程式碼解說。
    *   `Copy Reference to Terminal`：複製參考路徑，並自動開啟內建 Terminal 貼上（優先尋找名為 `opencode` 或 `claude` 的分頁）。

## 安裝方式

1.  取得 Plugin 檔案：
    *   專案編譯後位於：`build/distributions/copy-code-reference-plugin-1.0.0.zip`
2.  開啟 Android Studio 或 IntelliJ IDEA。
3.  進入 **Settings** (Windows/Linux) 或 **Settings...** (macOS) -> **Plugins**。
4.  點擊右上角的齒輪圖示 ⚙️，選擇 **Install Plugin from Disk...**。
5.  選擇上述的 `.zip` 檔案。
6.  重新啟動 IDE。

## 使用說明

1.  在程式碼編輯器中，圈選一段程式碼。
2.  點擊浮動工具列上的 📋 圖示，或是右鍵選擇 **Copy Reference** / **Copy Reference with ELI5** / **Copy Reference to Terminal**。
3.  路徑與行號即複製到剪貼簿。若選擇 **Copy Reference with ELI5**，剪貼簿也會包含要求 AI 用簡單方式逐步解釋程式碼的提示詞；若選擇 Terminal 選項，則會同步貼至 Terminal 中。

## 開發與編譯

### 前置需求
*   JDK 21 (Gradle 會自動下載 Toolchain，但建議本地安裝)
*   Gradle 8.13 (內附 Wrapper)

### 編譯指令

在專案根目錄執行：

```bash
./gradlew buildPlugin
```

編譯成功後，Plugin 檔案會產生在 `build/distributions/` 目錄下。
