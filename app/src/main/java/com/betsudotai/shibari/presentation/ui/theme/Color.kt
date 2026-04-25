package com.betsudotai.shibari.presentation.ui.theme

import androidx.compose.ui.graphics.Color

// プライマリー（メインカラー）：厳しさ・タスクフォース感のあるクリムゾンレッド
val TacticalRedLight = Color(0xFFD32F2F)
val TacticalRedDark = Color(0xFFEF5350)

// セカンダリー：実績やバッジに使うゴールド/アンバー（縛りアクセント）
val AchievementGold = Color(0xFFE8A44A)

// ターシャリー（第3の色）：成功・承認・クリアを表すネオングリーン
val SuccessNeonGreen = Color(0xFF00E676)

// --- タイムライン用デザイントークン ---
// ステータスバッジ
val StatusPendingBg     = Color(0xFF2A2218)
val StatusPendingFg     = Color(0xFFC8942A)
val StatusApprovedBg    = Color(0xFF1A3020)
val StatusApprovedFg    = Color(0xFF3DD68C)
val StatusRejectedBg    = Color(0xFF2D1A1A)
val StatusRejectedFg    = Color(0xFFF06060)

// 投票・AI判定・シート
val VoteApproveColor    = Color(0xFF3DD68C)
val VoteRejectColor     = Color(0xFFF06060)
val TimelineSheetBg     = Color(0xFF1A2840)
val TimelineSheetBtnBg  = Color(0xFF1E2E40)
val TimelineDivider     = Color(0xFF2A3A4A)

// --- 背景・サーフェス（表面）の色 ---

// ダークモード用：ただの黒ではなく、モダンでサイバーなスレート（青みグレー）
val SlateBackgroundDark = Color(0xFF0F172A) // 背景
val SlateSurfaceDark = Color(0xFF1E293B)    // カードやトップバー
val SlateSurfaceVariantDark = Color(0xFF334155) // 押された時や区切り線

// ライトモード用：清潔感がありつつ、赤が映えるクールな白〜薄グレー
val SlateBackgroundLight = Color(0xFFF8FAFC)
val SlateSurfaceLight = Color(0xFFFFFFFF)
val SlateSurfaceVariantLight = Color(0xFFE2E8F0)