# SPEC.md - Specification for Shibari App

**Version:** 1.1.0 (Added Onboarding & Quest Selection)
**Project:** Shibari (Betsudotai)

---

## 1. App Overview
**Concept:**
友達グループ「別働隊」専用の相互監視・承認アプリ。
InstagramやBeRealのようなビジュアル重視のUIで、日々の「縛り（Quest）」の達成や遵守を報告し合う。

**Core Loop:**
1.  **Onboarding:** アカウント作成時に、自分が参加する「縛り」を選択する。
2.  **Do:** ユーザーが縛りを実行、または禁止事項を守る。
3.  **Post:** 写真・動画で証拠を投稿する。
4.  **Verify:** 他のメンバーがタイムラインを見て承認（Approve）または否認（Reject）する。
5.  **Result:** 承認数が規定に達すると「達成」として記録される。

---

## 2. User Interface (UX)

### A. Onboarding Flow (First Launch)
アプリ未ログイン時に表示されるフロー。

1.  **Welcome / Auth Screen:**
    * 「ログイン」または「新規登録」を選択。
    * Googleログイン、またはメールアドレス/パスワード入力。
2.  **Profile Setup Screen (New User Only):**
    * 表示名（ニックネーム）の入力。
    * アイコン画像のアップロード。
3.  **Quest Selection Screen (New User Only):**
    * マスタデータにある「縛り一覧」がチェックボックス付きリストで表示される。
    * 自分が参加する（縛られる）項目を選択して「開始」ボタンを押す。
    * *例: 「丼禁止」はやるけど、「15県回る」はやらない、などをここで決定する。*

### B. Global Navigation (Bottom Tab)
ログイン後は以下の3画面構成となる。

1.  **Timeline (Home):** メインフィード。全員の報告が流れる。
2.  **Quests:** 自分の「今日のタスク」と「通算成績」を確認・投稿する画面。
3.  **Profile:** 自分の過去ログ、設定。参加クエストの変更もここから可能。

### C. Screen Details

#### 1. Timeline Screen (Feed)
* **Style:** BeReal / Instagramライクな縦スクロールUI。
* **Sort Order:** 「承認待ち (Pending)」の投稿を最優先で上に表示。
* **Card UI:** 写真・動画を大きく表示し、スタンプ感覚で承認・否認を行う。

#### 2. Quest Screen (Dashboard)
* **Filtering:** Onboardingで**「自分が選択したクエスト」のみ**が表示される。
* **Daily Section:** 「毎日おはよう動画」などのルーティン系。未達成ならタップして撮影へ。
* **Season Section:** 「15県回る」などの積み上げ系進捗バー。

#### 3. Post Screen
* **Input:** Media (Camera / Library), Comment.
* **Processing:** 動画はクライアント側で圧縮してからアップロードする。

---

## 3. Data Schema (Firestore)

### A. Strategy
**Read-Optimized / Denormalization (非正規化)**

### B. Collections

#### \`users\`
ユーザー情報に「参加しているクエストIDのリスト」を持たせる。
\`\`\`json
{
"uid": "user_abc",
"displayName": "タロウ",
"photoUrl": "https://...",
"fcmToken": "token_...",

// 参加中の縛りIDリスト (ここにあるものだけQuest画面に出る)
"participatingQuestIds": [
"daily_morning_video",
"ban_donburi",
"challenge_fuji"
]
}
\`\`\`

#### \`quests\` (Master Data)
\`\`\`json
{
"id": "daily_morning_video",
"title": "毎日おはよう動画",
"type": "routine", // prohibition, routine, achievement, challenge
"description": "朝起きてから30分以内に動画をアップ",
"threshold": 1
}
\`\`\`

#### \`timelines\` (Transactional Data)
\`\`\`json
{
"id": "post_uuid_v4",
"userId": "user_abc",
"questId": "daily_morning_video",

// Snapshots (Read用)
"author": { "displayName": "...", "photoUrl": "..." },
"quest": { "title": "...", "type": "..." },

"mediaUrl": "gs://...",
"mediaType": "video",
"comment": "眠い...",

"status": "pending",
"approvalCount": 0,
"votes": { "user_xyz": "approve" },

"createdAt": "timestamp"
}
\`\`\`

---

## 4. Business Logic

### A. Quest Selection Rule
* ユーザーはいつでも参加クエストを追加・削除できる（Profile画面）。
* ただし、削除前に投稿したデータは消えない。
* Timelineには「自分が参加していないクエスト」の投稿も流れてくる（他人の応援はするため）。

### B. Verification Flow
1.  **Initial State:** \`status: pending\`
2.  **Determination:**
    * **Approved:** \`approvalCount >= 2\`
    * **Rejected:** \`rejectionCount >= 2\`

---

## 5. Non-Functional Requirements
* **Performance:** 画像・動画の遅延読み込み (Coil)。
* **Media:** 動画圧縮必須。
* **Offline:** 閲覧のみ可。