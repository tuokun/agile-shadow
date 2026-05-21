# 技术债务

> 2026-05-21 代码审查中发现，尚未修复的架构级问题。

## 1. KeyboardViewModel 职责过重 (God Class)

**文件**: `app/src/main/java/dev/yeying/ime/ui/keyboard/KeyboardViewModel.kt`

ViewModel 混合了 9 种键盘类型的按键处理、T9 输入追踪、Rime 引擎交互、候选词管理、键盘切换、手写/剪贴板候选词管理等职责，约 310 行。

**建议**: 将 T9 相关逻辑封装到 `T9InputController`，键盘切换逻辑提取到 `KeyboardNavigator`。

---

## 2. ViewModel 生命周期管理不当

**文件**: `app/src/main/java/dev/yeying/ime/YeyingKeyboard.kt` 第 41-47 行

`KeyboardViewModel` 继承自 `ViewModel`，但通过 `remember { ViewModel(...) }` 创建，而非 `viewModel()` 函数。导致：
- `onCleared()` 永远不会被调用
- 传入的 lambda（`onCommitText` 等）被首次创建时的闭包捕获，`currentInputConnection` 引用可能失效

**建议**: 改用 `viewModel()` 创建，或改用手动生命周期管理确保资源释放。

---

## 3. Prefs 设置项未被消费

**文件**: `app/src/main/java/dev/yeying/ime/data/Prefs.kt`

定义了 `keyVibration`、`keySound`、`darkTheme`、`defaultKeyboard` 四个设置项，`SettingsScreen` 可修改，但实际功能代码没有消费它们：
- `GlassKeyButton` 触觉反馈始终 `performHapticFeedback`，未读取 `keyVibration`
- 键盘声音未实现
- `YeyingKeyboard` 暗色判断用 `isSystemInDarkTheme()`，未读取 `darkTheme`
- 默认键盘设置未被消费

**建议**: 逐项接入，或移除未实现的设置项 UI。

---

## 4. 按键处理主线程同步 JNI 调用链

**文件**: `app/src/main/java/dev/yeying/ime/ui/keyboard/KeyboardViewModel.kt` 第 54-146 行

每次按键触发至少 4 次 JNI 调用（`processKey` + `getCommit` + `clearComposition` + `getContext`），全部在主线程同步执行。低端设备上可能超过 16ms 阈值导致掉帧。

**建议**: 将 Rime 引擎交互移到 `Dispatchers.Default`，通过 `SharedFlow/Channel` 发回主线程更新 UI。同时在 native 层合并 `processKey + getCommit + getContext` 为一次调用。

---

## 5. HandwritingEngine 顶层可变状态

**文件**: `app/src/main/java/dev/yeying/ime/ui/keyboard/HandwritingBoard.kt` 第 46 行

`private var retainedEngine: HandwritingEngine? = null` 是顶层 `var`，生命周期等同进程。`HandwritingEngine` 持有 ML Kit `DigitalInkRecognizer`，非手写场景也占据内存。

**建议**: 将引擎生命周期绑定到 ViewModel 或使用 `rememberSaveable`，退出手写面板时释放资源。

---

## 6. 颜色/主题系统不统一

**现状**:
- `Theme.kt` 定义了 `LightColors` / `DarkColors` 主题方案
- `KeyButton.kt` 用自己的颜色常量（`TOOLBAR_BG`、`CONFIRM_BG` 等），不读取主题
- `GlassKeyButton` 的 `isDark` 参数从未被显式传递，默认 `false`
- `YeyingKeyboard` 用 `isSystemInDarkTheme()` 而非 Prefs 中的 `darkTheme` 设置

**建议**: 统一颜色定义到 `Theme.kt`，`GlassKeyButton` 从 `MaterialTheme.colorScheme` 读取颜色，移除硬编码色值常量。

---

## 7. 候选词来源混合管理

**文件**: `app/src/main/java/dev/yeying/ime/ui/keyboard/KeyboardViewModel.kt`

手写候选词和 Rime 候选词通过 `handwritingCandidates.isNotEmpty()` 来区分，共享同一个 `CandidateSelect` action。`KeyboardState` 中 `page` 字段是内部实现细节但暴露在公开状态中。

**建议**: 用 `enum class CandidateSource { RIME, HANDWRITING }` 标记当前来源，`page` 降级为 ViewModel 私有变量。

---

## 8. accumulatedCandidates 无上限保护

**文件**: `app/src/main/java/dev/yeying/ime/ui/keyboard/KeyboardViewModel.kt` 第 38 行

用户持续翻页时候选词无限追加到 `accumulatedCandidates`（每页 30 条）。虽然实际不太可能翻很多页，但缺少上限保护。

**建议**: 设置合理上限（如 300 条），超出时移除最早的记录。

---

## 9. Compose 重组效率

- `GlassKeyButton` 的 `onClick` lambda 每次父组件重组都创建新实例，QWERTY 键盘（26+ 键）影响明显
- `GlassKeyButton` 中 `RoundedCornerShape(8.dp)` 每次重组都创建新对象
- `_state.update {}` 无变更检测，即使状态未变也触发 StateFlow 发射

**建议**: 提取 `RoundedCornerShape` 为常量；对 QWERTY 键盘考虑用 `key` 和 `remember` 优化 lambda；`refreshState()` 添加变更检测。
