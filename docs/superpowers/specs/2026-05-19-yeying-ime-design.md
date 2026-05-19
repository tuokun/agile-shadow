# 曳影 (Yeying) 输入法设计文档

## 项目定位

基于 Rime 输入法框架，从零开发的个人 Android 输入法。参考语燕输入法 (yuyansdk) 的实现思路，但全部 Kotlin 代码自行编写，Compose UI 全新构建。项目身份独立，不沿用 yuyan 的包名、类名或 UI 体系。

唯一的外部编译产物依赖是 `.so` 原生库（Rime 引擎 JNI + 手写识别）。

## Section 1: 架构分层

```
┌─────────────────────────────────────┐
│  Compose UI 层（全新）               │  键盘、候选栏、符号面板、设置
├─────────────────────────────────────┤
│  桥接层（新建）                      │  InputMethodService ↔ Compose
├─────────────────────────────────────┤
│  引擎层（自行编写）                  │  Rime JNI 封装、T9 映射、数据结构
├─────────────────────────────────────┤
│  数据层                             │  rime-ice 默认词库、用户词库、偏好、剪贴板
└─────────────────────────────────────┘
```

## Section 2: Compose UI 架构

### 桥接方式

`InputMethodService.onCreateInputView()` 返回 `ComposeView`，将整个键盘 UI 交给 Compose 渲染。

```
InputMethodService
  └─ onCreateInputView() → ComposeView
       └─ YeyingKeyboard (根 Composable)
            ├─ CandidateBar
            └─ KeyboardArea
                 ├─ T9Keyboard
                 ├─ QwertyKeyboard
                 ├─ SymbolPanel
                 └─ HandwritingBoard
```

### 状态管理

```kotlin
data class KeyboardState(
    val activeKeyboard: KeyboardType,     // T9 / QWERTY / English / Symbol / Handwriting
    val candidates: List<Candidate>,
    val composingText: String,
    val capsState: CapsState,             // None / Once / Lock
    val symbolPage: Int,
)
```

### 输入链路

```
用户按键 → KeyboardAction → 引擎处理
         → 状态更新 → Compose 重组
         → 用户选词 → commitText()
```

## Section 3: Liquid Glass 主题系统

### 实现方式

模拟玻璃效果（半透明渐变 + 高光 + 内发光 + 边框），不使用 RenderEffect 实时模糊。

### 四层绘制

1. **主体渐变** — 垂直方向半透明填充
2. **高光条** — 水平方向顶部反光
3. **内发光** — 底部边缘散射光
4. **边框** — 1dp 描边（仅深色主题）

### 主题适配参数

| 层 | 浅色 | 深色 |
|---|---|---|
| 主体高 | 0.30 | 0.12 |
| 主体低 | 0.10 | 0.04 |
| 高光 | 0.45 | 0.18 |
| 内发光 | 0.08 | 0.03 |
| 边框 | Transparent | 0.08 |

所有层叠加在高 alpha (0.85~0.95) 底色上，确保看不清下方应用内容。

### 应用范围

作为 `Modifier.liquidGlass()` 扩展函数，复用于键盘背景、候选词栏、符号面板、按键按下态。底色支持色调变化（预留主题系统）。

参考实现：`D:/Dev/project/GitHub-Store` 的 BottomNavigation 毛玻璃效果。

## Section 4: 词库系统

### 双目录架构

利用 Rime 原生的双数据目录机制：

```
shared_data_dir (只读)         ← APK 内置默认方案和词库（rime-ice）
user_data_dir   (可写)         ← 用户自定义，优先级更高，同名文件完全覆盖
```

### 内置默认词库

rime-ice 雾凇拼音作为出厂默认，核心文件直接维护在 `assets/rime/` 中。

### 用户词库管理

- 用户可放入任意 Rime 兼容的方案和词库到 user_data_dir
- 支持两种使用方式：`.custom.yaml` 补丁微调，或完整方案替换
- 设置页提供「导入方案」「重新部署」「恢复默认」

### 九键兼容

九键 T9 方案为应用内置，用户一般不需要改。如果用户放入自定义的 `t9_pinyin.schema.yaml`，同样会被优先使用。

## Section 5: 模块结构

### 构建期

```
yeying/
  ├─ app/
  │    ├─ src/main/
  │    │    ├─ java/dev/yeying/ime/
  │    │    │    ├─ YeyingImeService.kt            ← 自有代码
  │    │    │    ├─ bridge/
  │    │    │    ├─ engine/                         ← 自有代码
  │    │    │    ├─ ui/
  │    │    │    │    ├─ theme/
  │    │    │    │    ├─ keyboard/
  │    │    │    │    ├─ candidate/
  │    │    │    │    ├─ symbol/
  │    │    │    │    └─ settings/
  │    │    │    └─ data/
  │    │    ├─ java/com/yuyan/inputmethod/core/     ← 采纳自 yuyan（JNI 桥接）
  │    │    │    ├─ Rime.kt                         ← external fun 声明
  │    │    │    ├─ Structs.kt                      ← Rime 数据结构
  │    │    │    ├─ HandWriting.kt                  ← 手写 JNI 声明
  │    │    │    └─ HandwritingNative.kt            ← 手写引擎封装
  │    │    ├─ assets/rime/
  │    │    │    ├─ cn_dicts/
  │    │    │    ├─ en_dicts/
  │    │    │    ├─ opencc/
  │    │    │    ├─ rime_ice.schema.yaml
  │    │    │    ├─ t9_pinyin.schema.yaml
  │    │    │    └─ default.yaml
  │    │    └─ jniLibs/
  │    │         ├─ libyuyanime.so                  ← Rime 引擎 JNI（yuyan 编译产物）
  │    │         ├─ libhandwriting.so               ← 手写 JNI 封装（yuyan）
  │    │         ├─ libhwInterface.so               ← 手写中间层（搜狗）
  │    │         ├─ libgpen_handwriter.so           ← GPU 笔迹加速
  │    │         └─ libSogouShell.so                ← 手写运行时依赖（搜狗核心）
  │    └─ build.gradle.kts
  ├─ build.gradle.kts
  └─ settings.gradle.kts
```

单模块结构，通过包名分隔职责。`com.yuyan.inputmethod.core` 包为从 yuyan 采纳的 JNI 桥接代码，二进制接口决定，不重写。`dev.yeying.ime` 包为全部自有代码。

### 运行时

```
/data/data/dev.yeying.ime/
  ├─ rime/shared/                    ← 从 assets/rime/ 解压（只读）
  ├─ rime/user/                      ← 用户自定义（可写，优先级更高）
  └─ files/                          ← 剪贴板数据库等
```

## Section 6: 功能范围与裁剪

### 保留功能

- 九键拼音 (T9)
- 全键拼音 (QWERTY)
- 英文输入
- 手写输入
- 符号/Emoji 面板
- 剪贴板管理
- 主题系统
- 键盘布局模式（单手/悬浮/全面屏）

### 移除功能

- 双拼（7 种方案）
- 五笔画输入
- 花漾字
- 常用语/前缀输入
- 语音输入

### 原生库

| 库 | 来源 | 大小 | 用途 |
|---|---|---|---|
| libyuyanime.so | yuyan 编译产物 | ~4.4MB | Rime 引擎 JNI |
| libhandwriting.so | yuyan 编译产物 | — | 手写 JNI 封装 |
| libhwInterface.so | 搜狗 | — | 手写中间层 |
| libgpen_handwriter.so | 搜狗 | ~2.7MB | GPU 笔迹加速 |
| libSogouShell.so | 搜狗核心 | ~7.6MB | 手写运行时依赖 |

> **注意**：`libhwInterface.so` 依赖 `libsogou_interface.so`，该文件在 yuyansdk 中缺失，需从原始编译环境补充，或通过 Android NDK 的 `System.loadLibrary` 顺序加载规避。

### 代码策略

**核心原则：二进制接口决定的代码直接采纳，自有逻辑全部重写。**

| 模块 | 策略 | 原因 |
|------|------|------|
| JNI `external fun` 声明 | **采纳 yuyan** | .so 符号静态注册，包名/类名/函数名不可改 |
| Rime 数据结构 (`RimeContext` 等) | **采纳 yuyan** | JNI 返回值类型，必须匹配 |
| Rime 引擎封装 (`RimeEngine`) | 自行编写 | API 设计、生命周期归我们管 |
| T9 映射逻辑 | 参考 yuyan 但自行实现 | 逻辑不复杂，用自己的代码风格 |
| 键盘按键定义 | 自行定义 | 自己的布局体系 |
| Compose UI | 全部自写 | yuyan 是 View 体系，不兼容 |
| 手写 JNI 封装 | **采纳 yuyan** | .so 依赖链太深，拆解无意义 |
| 设置/偏好/剪贴板 | 全部自写 | 业务逻辑，无需参考 |

### 开源引用

本项目使用了语燕输入法 (yuyansdk) 的开源部分：
- Rime JNI 桥接层（`com.yuyan.inputmethod.core.Rime`）
- 手写识别模块的原生库封装（`com.yuyan.inputmethod.core.HandWriting`）
- 相关 Rime 数据结构定义

以上代码因 JNI 二进制接口限制而采纳，详见项目 `NOTICE` 文件。

### 包名与命名

- 自有代码包名：`dev.yeying.ime`
- 采纳代码包名：`com.yuyan.inputmethod.core`（JNI 桥接，内部实现细节）
- 自有类名使用独立命名体系
