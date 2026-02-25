package com.betsudotai.shibari.domain.value

enum class QuestFrequency {
    ALWAYS,
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY;

    // 画面表示用のテキスト
    fun displayName(): String {
        return when (this)  {
            ALWAYS -> "常時"
            DAILY -> "日次"
            WEEKLY -> "週次"
            MONTHLY -> "月次"
            YEARLY -> "年次"
        }
    }


    // セクションを並べる順番
    fun sortOrder(): Int {
        return when (this) {
            ALWAYS -> 0
            DAILY -> 1
            WEEKLY -> 2
            MONTHLY -> 3
            YEARLY -> 4
        }
    }
}