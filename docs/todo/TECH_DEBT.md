# 技术债务

## 1. Emoji 显示风格替换（搁置）

**文件**: `app/src/main/java/io/github/cgfhsc/agileshadow/ime/ui/symbol/EmojiPanel.kt`

当前 emoji 使用 Android 系统原生 Noto Color Emoji 渲染。用户希望替换为 Apple Color Emoji 风格（iOS/Mac 默认样式），与主流输入法（搜狗、百度、QQ）一致。

**方案**：打包自定义 emoji TTF 字体到 `assets/fonts/`，Compose `Text` 通过 `FontFamily` 加载渲染。键盘面板内自行渲染，发送给目标应用的仍是标准 Unicode 编码。

**搁置原因**：Apple Color Emoji 字体受版权保护，直接使用存在法律风险。需确认合规的字体来源后再推进。

**可选替代方案**：
- [Twemoji](https://github.com/twitter/twemoji)（Twitter 开源，风格不同）
- 自行设计/委托设计 emoji 图标集
- 等待社区出现合规的类 Apple 风格字体

## 2. T9PopupKeyButton 视觉逻辑重复

**文件**: `app/src/main/java/io/github/cgfhsc/agileshadow/ime/ui/keyboard/KeyButton.kt`

`T9PopupKeyButton` 与 `GlassKeyButton` 重复了按键背景色计算、暗色主题颜色解析、边框颜色逻辑、按下状态视觉效果。可提取共享的颜色解析函数或创建基础按键 Composable。

## 3. 拖拽时重组频率

**文件**: `app/src/main/java/io/github/cgfhsc/agileshadow/ime/ui/keyboard/KeyButton.kt`

`T9PopupKeyButton` 中 `highlightedIndex` 在每次指针移动事件时更新，触发整个弹出面板的重组。可考虑用 `Modifier.graphicsLayer` 或降低更新频率来优化。

## 4. T9Mapper.getT9Composition 字符串分配

**文件**: `app/src/main/java/io/github/cgfhsc/agileshadow/ime/engine/T9Mapper.kt`

该方法在每次 T9 按键时调用，内部创建了多个 StringBuilder 和中间 List（两次 split + filter + buildString）。在高频输入场景下可优化为单次遍历构建结果字符串。
