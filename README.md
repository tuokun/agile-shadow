# 曳影 (Yeying)

> 《拾遗记》载，颛顼有神剑曰曳影，在匣中如电拖影，时刻待出。

- 九键拼音 (T9)、全键拼音 (QWERTY)、英文输入、手写输入（计划中）
- 符号/Emoji 面板、剪贴板管理
- 内置 [雾凇拼音](https://github.com/iDvel/rime-ice) 词库，用户可自行替换

## 架构

```
┌─────────────────────────────────────┐
│  Compose UI 层                      │  键盘、候选栏、符号面板、设置
├─────────────────────────────────────┤
│  桥接层                             │  InputMethodService ↔ Compose
├─────────────────────────────────────┤
│  引擎层                             │  Rime JNI 封装、T9 映射、数据结构
├─────────────────────────────────────┤
│  数据层                             │  rime-ice 词库、用户词库、偏好、剪贴板
└─────────────────────────────────────┘
```

## 技术栈

- Kotlin / Jetpack Compose
- Rime 输入法引擎 (JNI)
- Android minSdk 31+ (arm64-v8a)

## 开源引用

| 项目 | 用途 |
|------|------|
| [Rime](https://github.com/rime/home) | 输入法引擎核心框架 |
| [雾凇拼音](https://github.com/iDvel/rime-ice) | 内置默认词库与拼音方案 |
| [语燕输入法 ](https://github.com/gurecn/yuyansdk) | Rime JNI 桥接层与原生库 |

详见 `NOTICE` 文件。

## License

[GPL-3.0](LICENSE) © 2026 cgfhsc
