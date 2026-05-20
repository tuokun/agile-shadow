package dev.yeying.ime.engine

sealed class InputRecord {
    data class T9Key(val keyChar: Char, var consumed: Boolean = false) : InputRecord()

    data class PinyinKey(val pinyin: String, val posInInput: Int = 0) : InputRecord() {
        val pinyinLength: Int = pinyin.length
        val inputKeyLength: Int = pinyinLength + 1

        fun t9Keys(): String = T9Mapper.pinyinToT9(pinyin)
        fun restoreToT9Keys(): List<T9Key> = t9Keys().map { T9Key(it) }
        fun rimeKey(): String = "${pinyin}'"
        fun withPosInInput(pos: Int) = PinyinKey(pinyin, pos)
    }

    object SelectPinyinAction : InputRecord()
}

class InputKeyTracker {
    private val records = mutableListOf<InputRecord>()

    private val numToT9 = mapOf(
        '2' to 'A', '3' to 'D', '4' to 'G', '5' to 'J',
        '6' to 'M', '7' to 'P', '8' to 'T', '9' to 'W',
    )

    fun pushT9Key(keycode: Int): Char? {
        val t9Letter = numToT9[keycode.toChar()] ?: return null
        if (records.lastOrNull() == InputRecord.SelectPinyinAction) {
            records.removeLastOrNull()
        }
        records.add(InputRecord.T9Key(t9Letter))
        return t9Letter
    }

    fun pushPinyinSelectAction(pinyin: String): InputRecord.PinyinKey? {
        val t9Sequence = T9Mapper.pinyinToT9(pinyin)
        val t9KeysNeeded = t9Sequence.map { InputRecord.T9Key(it) }

        val startIndex = (0..(records.size - t9KeysNeeded.size)).indexOfFirst { i ->
            t9KeysNeeded.indices.all { j ->
                val record = records[i + j]
                record is InputRecord.T9Key && !record.consumed && record.keyChar == t9KeysNeeded[j].keyChar
            }
        }
        if (startIndex < 0) return null

        repeat(t9KeysNeeded.size) { records.removeAt(startIndex) }

        val posInInput = records.subList(0, startIndex).sumOf { record ->
            when (record) {
                is InputRecord.T9Key -> 1
                is InputRecord.PinyinKey -> record.inputKeyLength
                else -> 0
            }
        }

        val pinyinKey = InputRecord.PinyinKey(pinyin, posInInput)
        records.add(startIndex, pinyinKey)
        records.add(InputRecord.SelectPinyinAction)
        return pinyinKey
    }

    fun pop(): InputRecord? {
        if (records.isEmpty()) return null
        val last = records.removeLastOrNull()
        if (last == InputRecord.SelectPinyinAction && records.isNotEmpty()) {
            return records.removeLastOrNull()
        }
        return last
    }

    fun restorePinyinToT9Key(pinyinKey: InputRecord.PinyinKey? = null): InputRecord.PinyinKey? {
        if (pinyinKey != null) records.add(pinyinKey)
        val index = records.indexOfLast { it is InputRecord.PinyinKey }
        if (index < 0) return null
        val found = records[index] as InputRecord.PinyinKey
        records.removeAt(index)
        found.restoreToT9Keys().reversed().forEach { records.add(index, it) }
        return found
    }

    fun updateConsumedFlags(compositionText: String) {
        val unresolvedCount = compositionText.count { it in 'A'..'Z' || it in '2'..'9' }
        var remaining = unresolvedCount
        for (i in records.indices.reversed()) {
            val record = records[i]
            if (record is InputRecord.T9Key) {
                record.consumed = remaining <= 0
                remaining--
            }
        }
    }

    fun getUnresolvedT9Sequence(): String {
        val seq = StringBuilder()
        for (record in records) {
            if (record is InputRecord.T9Key && !record.consumed) seq.append(record.keyChar)
        }
        return seq.toString()
    }

    fun clear() = records.clear()
}
