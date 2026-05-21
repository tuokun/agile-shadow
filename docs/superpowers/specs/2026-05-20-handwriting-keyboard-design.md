---
title: 手写键盘布局优化
date: 2026-05-20
status: approved
---

## 背景

当前手写键盘 (`HandwritingBoard.kt`) 存在两个问题：
1. 手写候选词显示在画布下方，而非顶部统一的 `ToolbarCandidateBar`
2. 缺少工具按钮（删除、符号、空格、确认等）

## 设计方案

### 候选词 → 顶部 ToolbarCandidateBar

手写识别结果不再在画布下方显示，而是通过回调注入 `KeyboardViewModel`，由 `ToolbarCandidateBar` 统一展示。

实现：`HandwritingBoard` 暴露候选词列表回调，`YeyingKeyboard` 监听后更新 ViewModel 的 candidates 状态。

### 右侧面板：标点 + 删除

画布右侧垂直排列 5 个按钮，高度随画布填满：

| 按钮 | 高度 | 优先级 |
|------|------|--------|
| ， | 38dp | 必须 |
| 。 | 38dp | 必须 |
| ？ | 30dp | 可选 |
| ！ | 30dp | 可选 |
| ⌫ | 40dp | 必须 |

间距 3dp，总计 176+12=188dp。

点击标点直接提交字符，点击删除调用 `onDeleteChar`。

### 底部工具栏：完全对齐 T9

结构与 T9 键盘底部栏一致：

```
符号(1) | [123(1), 空格(1), 中/英(1)] | 确认(1)
```

weight 比例：左 1 + 中 4 + 右 1，与 T9 完全相同。

### 画布高度

移除内部候选行后，画布从 180dp 增至 188dp。右侧面板与画布等高。

## 涉及文件

- `HandwritingBoard.kt` — 主要改动：布局重构、候选词回调、右侧面板、底部工具栏
- `YeyingKeyboard.kt` — 传递 `viewModel` 给 `HandwritingBoard`，处理候选词回调
- `KeyboardViewModel.kt` — 新增方法接收手写候选词

## 不涉及

- `HandwritingEngine.kt` — 识别逻辑不变
- `ToolbarCandidateBar.kt` — 已有逻辑不变
- 其他键盘布局
