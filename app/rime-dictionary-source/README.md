# Rime 词典源码

此目录保存用于生成预编译 Rime Runtime 的开发期词典源码，不会被 Android `assets` 打包。

- `cn_dicts/` 是主拼音词库的编译输入；当前 Release 使用受控的 `build/pinyin.table.bin`。
- 修改词典源码后，必须通过固定的词典生成流程更新 Runtime 二进制和 `assets/rime/runtime-manifest.txt`。
- 普通 Android 构建只消费已校验的 Runtime，不会从本目录自动编译词典。
