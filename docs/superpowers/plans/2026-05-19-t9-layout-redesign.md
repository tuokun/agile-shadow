# T9 九键布局重构 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复 T9 九键布局 bug（多出的 `'` 键），重构为三列布局（左标点 + 中九键 + 右功能键 + 底栏 + 底部留白）

**Architecture:** 将 T9Layout 数据从 4 行平铺改为语义化分组（左列标点、中列九键、右列功能、底栏工具），T9Keyboard Composable 从逐行渲染改为三列 Row 嵌套结构。新增 `中/英` 键切换逻辑在 ViewModel 中处理。

**Tech Stack:** Kotlin, Jetpack Compose, Android IME

---

### Task 1: 更新 T9Layout.kt 布局数据

**Files:**
- Modify: `app/src/main/java/dev/yeying/ime/ui/keyboard/T9Layout.kt`

将当前的 4 行平铺布局重写为语义化分组，移除 `'` 键，新增 `；` 键码常量。

- [ ] **Step 1: 重写 T9Layout.kt**

将文件内容替换为：

```kotlin
package dev.yeying.ime.ui.keyboard

object T9Layout {
    data class T9Key(
        val label: String,
        val subLabel: String = "",
        val code: Int,
    )

    val leftPunctuation = listOf(
        T9Key("，", code = KEYCODE_COMMA),
        T9Key("。", code = KEYCODE_PERIOD),
        T9Key("？", code = KEYCODE_QUESTION),
        T9Key("！", code = KEYCODE_EXCLAMATION),
    )

    val center9Keys = listOf(
        listOf(T9Key("1", code = KEYCODE_T9_1)),
        listOf(T9Key("ABC", "2", KEYCODE_T9_2), T9Key("DEF", "3", KEYCODE_T9_3)),
        listOf(T9Key("GHI", "4", KEYCODE_T9_4), T9Key("JKL", "5", KEYCODE_T9_5), T9Key("MNO", "6", KEYCODE_T9_6)),
        listOf(T9Key("PQRS", "7", KEYCODE_T9_7), T9Key("TUV", "8", KEYCODE_T9_8), T9Key("WXYZ", "9", KEYCODE_T9_9)),
    )

    val rightFunctions = listOf(
        T9Key("⌫", "删除", KEYCODE_DELETE),
        T9Key("重输", code = KEYCODE_CLEAR),
        T9Key("@", code = KEYCODE_AT),
    )

    val bottomToolbar = listOf(
        T9Key("符号", code = KEYCODE_SYMBOL),
        T9Key("123", code = KEYCODE_SYMBOL),
        T9Key("空格", code = ' '.code),
        T9Key("中/英", code = KEYCODE_SWITCH_LANG),
        T9Key("确认", code = KEYCODE_ENTER),
    )
}

const val KEYCODE_T9_1 = '1'.code
const val KEYCODE_T9_2 = 'A'.code
const val KEYCODE_T9_3 = 'D'.code
const val KEYCODE_T9_4 = 'G'.code
const val KEYCODE_T9_5 = 'J'.code
const val KEYCODE_T9_6 = 'M'.code
const val KEYCODE_T9_7 = 'P'.code
const val KEYCODE_T9_8 = 'T'.code
const val KEYCODE_T9_9 = 'W'.code
const val KEYCODE_CLEAR = -10
const val KEYCODE_COMMA = ','.code
const val KEYCODE_PERIOD = '.'.code
const val KEYCODE_QUESTION = '?'.code
const val KEYCODE_EXCLAMATION = '!'.code
const val KEYCODE_AT = '@'.code
const val KEYCODE_ENTER = '\n'.code
const val KEYCODE_SWITCH_LANG = -11
```

- [ ] **Step 2: 验证构建**

Run: `./gradlew assembleDebug 2>&1 | tail -5`
Expected: BUILD FAILING — T9Keyboard.kt 引用了已删除的 `T9Layout.rows`，这是预期的，在 Task 2 修复。

---

### Task 2: 重写 T9Keyboard.kt 三列布局

**Files:**
- Modify: `app/src/main/java/dev/yeying/ime/ui/keyboard/T9Keyboard.kt`

将逐行渲染改为三列布局：左列标点 + 中列九键网格 + 右列功能键，底部工具栏 + 留白。

- [ ] **Step 1: 重写 T9Keyboard.kt**

将文件内容替换为：

```kotlin
package dev.yeying.ime.ui.keyboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun T9Keyboard(
    viewModel: KeyboardViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // 主区域：三列（左标点 + 中九键 + 右功能）
        Row(
            modifier = Modifier.fillMaxWidth().height(168.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // 左列：标点符号 4 键
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                T9Layout.leftPunctuation.forEach { key ->
                    GlassKeyButton(
                        label = key.label,
                        onClick = { viewModel.onAction(KeyboardAction.KeyPress(key.code)) },
                        modifier = Modifier.weight(1f),
                        height = 40.dp,
                    )
                }
            }

            // 中列：九键 3x3 网格
            Column(
                modifier = Modifier.weight(4f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Row 1: 仅 "1"
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    T9Layout.center9Keys[0].forEach { key ->
                        GlassKeyButton(
                            label = key.label,
                            subLabel = key.subLabel.ifEmpty { null },
                            onClick = { viewModel.onAction(KeyboardAction.KeyPress(key.code)) },
                            modifier = Modifier.weight(1f),
                            height = 52.dp,
                        )
                    }
                }
                // Row 2: ABC DEF
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    T9Layout.center9Keys[1].forEach { key ->
                        GlassKeyButton(
                            label = key.label,
                            subLabel = key.subLabel.ifEmpty { null },
                            onClick = { viewModel.onAction(KeyboardAction.KeyPress(key.code)) },
                            modifier = Modifier.weight(1f),
                            height = 52.dp,
                        )
                    }
                }
                // Row 3: GHI JKL MNO
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    T9Layout.center9Keys[2].forEach { key ->
                        GlassKeyButton(
                            label = key.label,
                            subLabel = key.subLabel.ifEmpty { null },
                            onClick = { viewModel.onAction(KeyboardAction.KeyPress(key.code)) },
                            modifier = Modifier.weight(1f),
                            height = 52.dp,
                        )
                    }
                }
                // Row 4: PQRS TUV WXYZ
                Row(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    T9Layout.center9Keys[3].forEach { key ->
                        GlassKeyButton(
                            label = key.label,
                            subLabel = key.subLabel.ifEmpty { null },
                            onClick = { viewModel.onAction(KeyboardAction.KeyPress(key.code)) },
                            modifier = Modifier.weight(1f),
                            height = 52.dp,
                        )
                    }
                }
            }

            // 右列：功能键 3 键
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                T9Layout.rightFunctions.forEach { key ->
                    GlassKeyButton(
                        label = key.label,
                        subLabel = key.subLabel.ifEmpty { null },
                        onClick = {
                            when (key.code) {
                                KEYCODE_CLEAR -> viewModel.onAction(KeyboardAction.ClearComposition)
                                else -> viewModel.onAction(KeyboardAction.KeyPress(key.code))
                            }
                        },
                        modifier = Modifier.weight(1f),
                        height = 52.dp,
                    )
                }
            }
        }

        // 底部工具栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            T9Layout.bottomToolbar.forEach { key ->
                GlassKeyButton(
                    label = key.label,
                    onClick = {
                        when (key.code) {
                            KEYCODE_SWITCH_LANG -> viewModel.onAction(
                                KeyboardAction.SwitchKeyboard(
                                    if (state.activeKeyboard == KeyboardType.T9) KeyboardType.QWERTY else KeyboardType.T9
                                )
                            )
                            else -> viewModel.onAction(KeyboardAction.KeyPress(key.code))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    height = 44.dp,
                )
            }
        }

        // 底部留白（适配曲面屏）
        Spacer(modifier = Modifier.height(28.dp))
    }
}
```

- [ ] **Step 2: 验证构建**

Run: `./gradlew assembleDebug 2>&1 | tail -5`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 提交**

```bash
git add app/src/main/java/dev/yeying/ime/ui/keyboard/T9Layout.kt app/src/main/java/dev/yeying/ime/ui/keyboard/T9Keyboard.kt
git commit -m "refactor: redesign T9 layout to 3-column structure with aligned sidebars"
```

---

### Task 3: 更新 KeyboardViewModel 处理新键码

**Files:**
- Modify: `app/src/main/java/dev/yeying/ime/ui/keyboard/KeyboardViewModel.kt`

ViewModel 需要处理新增的 `KEYCODE_SYMBOL`（底部工具栏的符号/123键）和确认 `KEYCODE_ENTER` 逻辑已在 `handleKeyPress` 的 else 分支处理。

- [ ] **Step 1: 检查 ViewModel 是否需要改动**

查看当前 `handleKeyPress`，`KEYCODE_SYMBOL` 已有处理（切换到 SYMBOL 键盘）。`KEYCODE_AT`、`KEYCODE_QUESTION`、`KEYCODE_EXCLAMATION` 都是字符键码，会走 else 分支由 RimeEngine 处理。`KEYCODE_SWITCH_LANG` 在 T9Keyboard 中直接通过 `SwitchKeyboard` action 处理，不走 handleKeyPress。

结论：**ViewModel 无需改动**，现有逻辑已覆盖所有新键码。

- [ ] **Step 2: 确认无遗漏**

验证：
- `，` → KEYCODE_COMMA = 44 → else 分支 → RimeEngine 处理 ✓
- `。` → KEYCODE_PERIOD = 46 → else 分支 → RimeEngine 处理 ✓
- `？` → KEYCODE_QUESTION = 63 → else 分支 → RimeEngine 处理 ✓
- `！` → KEYCODE_EXCLAMATION = 33 → else 分支 → RimeEngine 处理 ✓
- `@` → KEYCODE_AT = 64 → else 分支 → RimeEngine 处理 ✓
- `⌫` → KEYCODE_DELETE → 已有处理 ✓
- `重输` → KEYCODE_CLEAR → T9Keyboard 中直接走 ClearComposition action ✓
- `符号/123` → KEYCODE_SYMBOL → 已有处理 ✓
- `空格` → ' '.code → else 分支 ✓
- `中/英` → KEYCODE_SWITCH_LANG → T9Keyboard 中直接走 SwitchKeyboard action ✓
- `确认` → KEYCODE_ENTER → else 分支 ✓

所有键码均已覆盖，无需额外改动。

---

### Task 4: 构建验证与提交

**Files:** 无新增

最终验证构建并确保一切正常。

- [ ] **Step 1: 完整构建验证**

Run: `./gradlew assembleDebug 2>&1 | tail -10`
Expected: BUILD SUCCESSFUL

- [ ] **Step 2: 确认所有改动文件**

Run: `git diff --stat HEAD`
Expected: T9Layout.kt, T9Keyboard.kt 有改动
