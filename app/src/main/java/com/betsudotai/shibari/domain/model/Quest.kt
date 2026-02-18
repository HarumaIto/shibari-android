package com.betsudotai.shibari.domain.model

data class Quest(
    val id: String,
    val title: String,
    val type: String, // prohibition | routine | achievement | challenge
    val description: String,
    val threshold: Int? // Optional
)