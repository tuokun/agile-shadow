# 曳影 (Yeying) 项目交接文档

> 本文档为会话间交接使用，包含完整项目背景、设计决策、当前进度和下一步行动。

## 1. 项目概况

**项目名**：曳影 (Yeying)
**项目目录**：`D:/Dev/project/ime/yeying/`
**包名**：`dev.yeying.ime`
**目标**：基于 Rime 输入法引擎，从零开发个人 Android 输入法。Compose UI 全新构建，不沿用任何项目的代码。

## 2. 设计决策（已通过审阅）

### 2.1 架构四层分层

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

### 2.2 功能范围

**保留**：九键拼音(T9)、全键拼音(QWERTY)、英文输入、手写输入、符号/Emoji 面板、剪贴板管理、主题系统、键盘布局模式（单手/悬浮/全面屏）

**移除**：双拼(7种)、五笔画、花漾字、常用语/前缀输入、语音输入

### 2.3 核心原则

- **二进制决定的代码直接采纳**：JNI `external fun` 声明、Rime 数据结构因 .so 静态注册而采纳 yuyan 代码；引擎封装、UI、业务逻辑全部自行编写
- **开源引用**：采纳的 yuyan 代码（`com.yuyan.inputmethod.core` 包）属开源引用，详见项目 NOTICE
- **包名独立**：自有代码使用 `dev.yeying.ime`，采纳代码保留原始包名作为内部实现细节

### 2.4 UI 方案

- **Liquid Glass 毛玻璃效果**（全局）：模拟玻璃（半透明渐变+高光+内发光+边框），不使用 RenderEffect 实时模糊
- 高 alpha 0.85~0.95，看不清下方应用内容
- 作为 `Modifier.liquidGlass()` 扩展函数复用
- 参数化设计，后续可在设置中让用户调整

### 2.5 词库系统

- **双目录架构**：`shared_data_dir`（APK 内置，只读）+ `user_data_dir`（用户自定义，可写，优先级更高）
- 内置默认词库：rime-ice 雾凇拼音，核心文件直接维护在 `assets/rime/` 中
- 用户可放入任意 Rime 兼容方案/词库到 user_data_dir 完全替换
- 九键 T9 方案为应用内置

### 2.6 Emoji 策略

- 系统原生（Unicode 文本 + EmojiCompat 兼容），不引入图片资源

## 3. 当前进度

### 3.1 已完成

- [x] 三个参考项目研究（fcitx5-android、trime、yuyansdk）
- [x] 需求澄清和功能范围确定
- [x] 架构方案选择（方案 B：引擎复用 + Compose UI 重写）
- [x] 项目命名（曳影 Yeying）
- [x] 设计文档 6 个 Section 全部呈现并通过审阅
- [x] 设计文档写入：`docs/superpowers/specs/2026-05-19-yeying-ime-design.md`
- [x] 实现计划写入：`docs/superpowers/plans/2026-05-19-yeying-ime.md`
- [x] 项目 Git 仓库初始化
- [x] CLAUDE.md、曳影基本信息.md、docs/todo/future-ideas.md 已创建

### 3.2 待执行

实现计划共 10 个 Phase、15 个 Task，按顺序执行：

```
Phase 1  项目脚手架 (Task 1-2)           → 可构建空 APK
Phase 2  引擎层 (Task 3-5)               → Rime JNI 可调用
Phase 3  桥接层 (Task 6)                  → 输入法可激活并显示 Compose UI
Phase 4  Liquid Glass 主题 (Task 7)       → 视觉效果可用
Phase 5  QWERTY 键盘 + 候选词 (Task 8-10) → 基本中文输入可用 ← 核心里程碑
Phase 6  T9 九键键盘 (Task 11)            → 九键输入可用
Phase 7  符号/Emoji 面板 (Task 12)        → 符号输入可用
Phase 8  手写输入 (Task 13)               → 手写可用
Phase 9  设置页 (Task 14)                 → 可管理词库和偏好
Phase 10 剪贴板管理 (Task 15)             → 剪贴板功能可用
```

## 4. 参考项目

### 4.1 yuyansdk（语燕输入法）— 引擎实现参考

**路径**：`D:/Dev/project/ime/yuyansdk/`
**用途**：参考其 Rime JNI 接口、T9 映射算法、输入链路。不复制代码。

关键文件：

| 文件 | 路径 | 用途 |
|------|------|------|
| Rime.kt | `src/main/java/com/yuyan/inputmethod/core/Rime.kt` | JNI 接口，所有 `external fun` 签名 |
| RimeEngine.kt | `src/main/java/com/yuyan/inputmethod/RimeEngine.kt` | 引擎封装，输入处理流程 |
| T9PinYinUtils.kt | `src/main/java/com/yuyan/inputmethod/util/T9PinYinUtils.kt` | T9 数字→拼音映射表（~225条规则） |
| Structs.kt | `src/main/java/com/yuyan/inputmethod/core/Structs.kt` | RimeContext/RimeCommit 等数据结构 |
| KeyboardData.kt | `src/main/java/com/yuyan/imemodule/keyboard/KeyboardData.kt` | 键盘布局定义（T9/QWERTY 按键码） |
| ImeService.kt | `src/main/java/com/yuyan/imemodule/service/ImeService.kt` | InputMethodService 实现 |
| T9TextContainer.kt | `src/main/java/com/yuyan/imemodule/keyboard/container/T9TextContainer.kt` | T9 键盘容器 |
| HandWriting.kt | 搜索 `HandWriting.kt` | 手写识别 JNI 接口 |

### 4.2 rime-ice（雾凇拼音）— 默认词库来源

**路径**：`D:/Dev/project/ime/rime-ice/`
**用途**：内置默认词库，只需提取部分核心文件

需要提取的文件：
- `cn_dicts/` — 中文词条
- `en_dicts/` — 英文词条
- `opencc/` — 简繁转换
- `rime_ice.schema.yaml` — 拼音方案
- `default.yaml` — 全局配置
- 安装指南：`others/docs/Installation.md`

### 4.3 GitHub-Store — Liquid Glass 效果参考

**路径**：`D:/Dev/project/GitHub-Store/`
**用途**：Liquid Glass 毛玻璃效果的参考实现

关键文件：
- `composeApp/src/commonMain/kotlin/zed/rainxch/githubstore/app/navigation/BottomNavigation.kt`
- 核心技术：`drawBehind` + 多层渐变（垂直渐变主体 + 水平渐变高光 + 内发光 + 边框）

### 4.4 joy-tune — 依赖版本对齐

**路径**：`D:/Dev/project/joy-tune/`
**用途**：依赖版本参考

版本目录：`gradle/libs.versions.toml`

关键版本：
- AGP 8.9.2, Kotlin 2.1.21, KSP 2.1.21-2.0.2
- Compose BOM 2026.03.01, Room 2.8.4, DataStore 1.1.4
- Lifecycle 2.8.7, Coroutines 1.10.1
- minSdk 31 (Android 12+), targetSdk 35, compileSdk 35, Java 17

### 4.5 fcitx5-android / trime — 辅助参考

- `D:/Dev/project/ime/fcitx5-android/` — Fcitx5 Android 移植，不支持九键
- `D:/Dev/project/ime/trime/` — Rime 原生客户端，不支持九键

## 5. 原生库处理

### 5.1 需要的 .so 文件（全部来自 yuyansdk 编译产物）

| 库 | 大小 | 用途 | JNI 绑定 |
|---|---|---|---|
| libyuyanime.so | ~4.4MB | Rime 引擎 JNI | `com.yuyan.inputmethod.core.Rime` |
| libhandwriting.so | — | 手写 JNI 封装 | `com.yuyan.inputmethod.core.HandWriting` |
| libhwInterface.so | — | 手写中间层 | `com.boyasec.ime.input.sogou.SogouEngine` |
| libgpen_handwriter.so | ~2.7MB | GPU 笔迹加速 | 无（纯 native） |
| libSogouShell.so | ~7.6MB | 手写运行时依赖 | — |

> **依赖链**: `libhandwriting.so` → `libhwInterface.so` → `libgpen_handwriter.so` + `libSogouShell.so` + `libsogou_interface.so`（后者在 yuyansdk 中缺失）。

### 5.2 JNI 符号结论

已确认所有 JNI .so 使用**静态注册**，函数名硬编码包名/类名：
- `libyuyanime.so` → `Java_com_yuyan_inputmethod_core_Rime_*`（16 个）
- `libhandwriting.so` → `Java_com_yuyan_inputmethod_core_HandWriting_*`（7 个）
- `libhwInterface.so` → `Java_com_boyasec_ime_input_sogou_SogouEngine_*`（14 个）

**决策**: .so 文件保留原名，对应 Kotlin JNI 声明从 yuyan 直接采纳，放置于 `com.yuyan.inputmethod.core` 包。自有代码通过 `dev.yeying.ime.engine.RimeEngine` / `HandwritingEngine` 封装层间接调用。

### 5.3 架构支持

仅支持 arm64-v8a（minSdk 31+ 设备全为 64 位）。

## 6. yuyansdk 九键实现要点（实现时参考）

- 应用层：`T9PinYinUtils.kt` 维护 T9 数字序列→拼音组合映射表（~225条规则）
- Rime 层：`t9_pinyin.schema.yaml` 专用方案 + `t9_pinyin.prism.bin` 棱镜文件
- 输入链路：九键按键 → T9PinYinUtils 查表得候选拼音 → 用户选择 → Rime.replaceKey() → Rime 查词库返回候选词
- 九键 vs 全键使用不同 Rime 方案和键盘容器

## 7. 项目文件索引

```
D:/Dev/project/ime/yeying/
  ├─ CLAUDE.md                                    ← 项目 Claude Code 指令
  ├─ 曳影基本信息.md                              ← 项目基本信息（非代码文档）
  ├─ NOTICE                                       ← 开源引用声明
  ├─ docs/
  │    ├─ handoff.md                              ← 本文档（会话交接）
  │    ├─ todo/future-ideas.md                    ← 后续想法（Liquid Glass 可调、命名重斟酌）
  │    └─ superpowers/
  │         ├─ specs/2026-05-19-yeying-ime-design.md    ← 设计文档
  │         └─ plans/2026-05-19-yeying-ime.md           ← 实现计划
  └─ .git/                                        ← Git 仓库（已初始化，未提交）
```

## 8. 下一步行动

1. 在 `D:/Dev/project/ime/yeying/` 目录下开启新的 Claude Code 会话
2. 读取 `docs/handoff.md`（本文档）了解项目全貌
3. 读取 `docs/superpowers/plans/2026-05-19-yeying-ime.md` 获取实现计划详情
4. 从 Phase 1 Task 1 开始执行：创建 Android 项目脚手架
5. 建议使用 `superpowers:executing-plans` skill 逐步执行计划
