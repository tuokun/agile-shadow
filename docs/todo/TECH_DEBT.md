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

## 2. 候选词展开视图"全/字"切换按钮

**文件**: `app/src/main/java/io/github/cgfhsc/agileshadow/ime/ui/candidate/ToolbarCandidateBar.kt`（`ExpandedCandidateView`）

在候选词展开视图的右侧操作栏增加"全/字"切换按钮：
- "全"模式：显示当前所有候选词（含词组），即现有行为
- "字"模式：过滤 `state.candidates` 只显示 `text.length == 1` 的单字候选词

**方案**：
- `KeyboardState` 增加 `singleCharMode: Boolean` 状态
- `ExpandedCandidateView` 根据状态过滤候选词列表
- 在右侧操作栏添加切换按钮

**搁置原因**：需要确认 Rime 引擎在单字模式下是否能返回足够的单字候选词，否则用户体验可能不佳。

