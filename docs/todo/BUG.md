# BUG 列表

更新日期：2026-05-20

---

## BUG-001：手写键盘右侧标点高度不统一

右侧 5 个按钮的 weight 不一致（，。=1.0，？！=0.85，⌫=1.1）。改为全部 weight=1.0 等分高度，且宽度与底部确认按钮保持一致。涉及文件：`HandwritingBoard.kt:146-182`

## BUG-002：T9 键盘空格按钮按下无效

空格事件使用了 `KeyPress(32)` 发给 Rime 引擎但 T9 schema 不处理空格。应改为：无编码时 `DirectCommit(" ")`，有编码时提交 composition。涉及文件：`T9Keyboard.kt:148-154`、`KeyboardViewModel.kt`

## BUG-003：T9 键盘删除按钮改为仅图标

右侧删除按钮当前显示图标 + "删除"文字，去掉文字，仅保留 Backspace 图标居中。涉及文件：`T9Layout.kt:24`、`T9Keyboard.kt:104-121`

## BUG-004：123 数字键盘按钮大小与 T9 不一致

NumberKeyboard 中间列用了 `weight(3f)` 而 T9 是 `weight(4f)`，导致按钮宽度偏小。改为 `weight(4f)` 对齐 T9。涉及文件：`NumberKeyboard.kt:54`、`:118`

## BUG-005：T9 键盘按键内字母数字间距过大

8 个按键中字母（如 ABC）和数字（如 2）垂直间距偏大，向中心收拢间距。涉及文件：`KeyButton.kt:97-101`

## BUG-006：降低按键和背景的模糊度

窗口背景模糊半径从 60 降低到 30-40（`AgileShadowImeService.kt:78`），同时降低 LiquidGlass 各层 alpha 参数，提升按键清晰度。涉及文件：`LiquidGlass.kt:14-22`
