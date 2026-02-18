package com.betsudotai.shibari.domain.value

enum class PostStatus {
    PENDING,  // 審査中
    APPROVED, // 承認済
    REJECTED, // 却下
    DISPUTED  // 異議あり
}