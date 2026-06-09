# 別働隊 -Shibari- (Android)

このプロジェクトは、クローズドSNS型の目標達成・習慣化支援アプリ「別働隊 -Shibari-」の Android アプリ用ソースコードです。
Jetpack Compose と Firebase を基盤とし、グループ内でのクエスト達成と相互レビューを実現します。

---

## 1. プロジェクトの概要と目的
「一人では続かない目標も、信頼できる仲間との『縛り』があれば達成できる」というコンセプトのもと開発されています。
証拠となる写真や動画を投稿し、メンバー同士が「承認/否認」を投票するシステムを備えた、規律重視の目標達成支援プラットフォームです。

## 2. 技術スタック
- **Language:** Kotlin 2.3.10
- **UI Framework:** Jetpack Compose (Material3)
- **State Management:** StateFlow / SharedFlow + Kotlin Coroutines (async/await)
- **DI:** Hilt / Dagger + KSP
- **Navigation:** Navigation Compose
- **Backend Integration:**
  - Firebase Auth (Google & Email/Password)
  - Cloud Firestore (Database)
  - Firebase Storage (Media)
  - Firebase Messaging (Push Notifications / FCM)
  - Firebase Crashlytics (Error Tracking)
  - Firebase Analytics
- **Media:** Coil (画像ロード), Media3 ExoPlayer (動画再生)
- **Local Storage:** DataStore Preferences, Room (SQLite)
- **Serialization:** kotlinx-serialization-json
- **Minimum SDK:** 26 (Android 8.0) / Target SDK: 36 (Android 15)

## 3. アーキテクチャと実装ルール
保守性とテスタビリティを確保するため、**Clean Architecture** および **MVVM** パターンを採用しています。
依存の方向は `Presentation → Application → Domain ← Data` となります。

### レイヤー構成

1. **Domain層 (`domain/`):**
   - 純粋な Kotlin コード。Android / Firebase への依存を持ちません。
   - **Model (`domain/model/`):** ビジネスロジックや UI で使用する純粋なデータ型 (例: `User`, `Quest`, `TimelinePost`)。
   - **Repository インタフェース (`domain/repository/`):** データアクセスの契約定義 (例: `AuthRepository`, `TimelineRepository`)。
   - **Value Objects (`domain/value/`):** 列挙型や値オブジェクト (例: `QuestType`, `PostStatus`, `VoteType`)。

2. **Application層 (`application/`):**
   - ビジネスロジックの具体的な実装 (Use Case)。
   - **UseCase実装 (`application/usecase/`):** Domain で定義したインタフェースの具象クラス (例: `GetMyQuestsUseCaseImpl`)。

3. **Data層 (`data/`):**
   - **Repository実装 (`data/repository/`):** Domain のインタフェースを Firebase / ローカルストレージに適合させる実装クラス。
   - **DataSource (`data/datasource/`):** Firebase の具体的な読み書き処理 (`remote/`) とローカルキャッシュ処理 (`local/`)。
   - **DTO (`data/dto/`):** Firestore への保存形式に最適化されたデータ型。`toDomain()` / `fromDomain()` で Domain Model と相互変換を行います。

4. **Presentation層 (`presentation/`):**
   - **ViewModel (`presentation/viewmodel/`):** `@HiltViewModel` を使用。状態は `StateFlow<UiState>`、ナビゲーション等の一時イベントは `SharedFlow<Event>` で管理します。
   - **UI Screens (`presentation/ui/screens/`):** Composable によるスクリーン実装。
   - **Components (`presentation/ui/components/`):** 再利用可能な Composable コンポーネント。

5. **Core層 (`core/`):**
   - **DI Modules (`core/di/`):** Hilt モジュール定義 (FirebaseModule, RepositoryModule 等)。
   - **Utilities (`core/util/`):** メディア処理等の共通ユーティリティ。

---

## 4. 現在実装済みの主要機能
- **認証:** Google ログイン (AndroidX Credentials API) およびメールアドレス認証。
- **オンボーディング:** プロフィール設定、グループ選択、クエスト選択の初期フロー。認証状態に基づく画面遷移の自動制御。
- **タイムライン:**
  - グループメンバーの投稿フィード (承認待ち投稿を優先表示)。
  - 今日 / 今週 / 今月 のフィルタタブ。
  - 写真・動画のアップロード (クライアント側動画圧縮、50MB制限)。
  - ステータスバッジ (審査中、承認済み、否認済み) の表示。
- **ピアレビュー:** メンバーの投稿に対する「承認」「否認」の投票ロジック。
- **クエスト管理:** 参加中のクエストを頻度別 (ALWAYS / DAILY / WEEKLY / MONTHLY / YEARLY) に表示。証拠投稿へのナビゲーション。
- **コメント:** 投稿へのコメント閲覧・追加。
- **通知:** FCM によるプッシュ通知。アプリ内通知一覧画面。
- **グループ管理:** グループメンバー表示、グループクエスト一覧。クエスト作成・編集フォーム。
- **安全性:** ユーザーのブロック・通報機能 (UGC 対策)。AI モデレーション判定の表示。
- **アカウント管理:** プロフィール編集、ログアウト、退会 (過去投稿を匿名化して保持)。

## 5. ディレクトリ構造
```text
shibari-android/
├── app/src/main/java/com/betsudotai/shibari/
│   ├── MainActivity.kt               # エントリーポイント (Hilt アノテーション)
│   ├── MainViewModel.kt              # 認証状態 + FCM トークン管理
│   ├── ShibariApplication.kt         # Application クラス
│   │
│   ├── domain/                       # ビジネスルール層 (Android 依存なし)
│   │   ├── model/                    # ドメインモデル (User, Quest, TimelinePost 等)
│   │   │   └── timeline/             # スナップショット (AuthorSnapshot, AiJudgment 等)
│   │   ├── repository/               # Repository インタフェース定義
│   │   ├── usecase/                  # UseCase インタフェース定義
│   │   └── value/                    # 列挙型・値オブジェクト (QuestType, PostStatus 等)
│   │
│   ├── application/                  # ビジネスロジック実装層
│   │   └── usecase/                  # UseCase 実装 (GetMyQuestsUseCaseImpl 等)
│   │
│   ├── data/                         # データアクセス層
│   │   ├── repository/               # Repository 実装 (AuthRepositoryImpl 等)
│   │   ├── datasource/
│   │   │   ├── remote/               # Firebase 実装 (TimelineRemoteDataSourceImpl 等)
│   │   │   └── local/                # Room / DataStore 実装
│   │   ├── dto/                      # Firestore マッピング用 DTO
│   │   │   └── (UserDto, QuestDto, TimelinePostDto, CommentDto 等)
│   │   └── service/
│   │       └── MyFirebaseMessagingService.kt
│   │
│   ├── presentation/                 # UI 層
│   │   ├── ui/
│   │   │   ├── screens/              # Composable スクリーン (15画面)
│   │   │   │   ├── AuthScreen.kt
│   │   │   │   ├── ProfileSetupScreen.kt
│   │   │   │   ├── ProfileEditScreen.kt
│   │   │   │   ├── QuestSelectionScreen.kt
│   │   │   │   ├── GroupSelectionScreen.kt
│   │   │   │   ├── MainScreen.kt     # ボトムタブナビゲーション
│   │   │   │   ├── TimelineScreen.kt
│   │   │   │   ├── QuestsScreen.kt
│   │   │   │   ├── ProfileScreen.kt
│   │   │   │   ├── PostScreen.kt
│   │   │   │   ├── CommentScreen.kt
│   │   │   │   ├── GroupScreen.kt
│   │   │   │   ├── GroupQuestListScreen.kt
│   │   │   │   ├── QuestFormScreen.kt
│   │   │   │   └── NotificationScreen.kt
│   │   │   ├── components/           # 再利用可能 Composable
│   │   │   │   ├── TimelinePostItem.kt
│   │   │   │   ├── VideoPlayer.kt
│   │   │   │   ├── AiJudgmentDisplay.kt
│   │   │   │   └── StatusBadge.kt
│   │   │   ├── navigation/
│   │   │   │   ├── Screen.kt         # ルート定義 (sealed class)
│   │   │   │   └── AppNavigation.kt  # ナビゲーショングラフ
│   │   │   └── theme/
│   │   │       ├── Color.kt
│   │   │       ├── Theme.kt          # ダークモード強制テーマ
│   │   │       └── Type.kt
│   │   └── viewmodel/                # 機能別 ViewModel
│   │       ├── auth/                 (AuthViewModel, AuthEvent)
│   │       ├── timeline/             (TimelineViewModel, TimelineUiState)
│   │       ├── quest/                (QuestsViewModel, QuestUiState)
│   │       ├── post/                 (PostViewModel, PostEvent)
│   │       ├── profile/              (ProfileViewModel, ProfileUiState, ProfileEvent)
│   │       └── ... (他機能の ViewModel)
│   │
│   └── core/                         # 共通ユーティリティ
│       ├── di/                       # Hilt モジュール
│       │   ├── FirebaseModule.kt
│       │   ├── DataSourceModule.kt
│       │   ├── RepositoryModule.kt
│       │   └── UseCaseModule.kt
│       ├── util/
│       │   └── FileUtil.kt           # メディア圧縮・一時ファイル処理
│       └── network/                  # API クライアント (将来拡張用)
│
├── docs/
│   ├── SPEC_V1.md                    # 機能仕様書 v1.1.0
│   └── post-screen-improvement-plan.md
├── AGENTS.md                         # アーキテクチャ・実装ガイドライン
├── build.gradle.kts                  # ルート Gradle 設定
├── app/build.gradle.kts              # アプリレベル Gradle 設定
└── settings.gradle.kts               # モジュール定義
```

## 6. 環境構築の手順
1. Android Studio で `shibari-android/` を開きます。
2. Firebase コンソールから Android アプリを追加し、`google-services.json` をダウンロードして `app/` ディレクトリ直下に配置します。
3. `local.properties` に `sdk.dir` が正しく設定されていることを確認します。
4. Gradle Sync を実行後、`gradlew assembleDebug` でビルドできます。

## 7. 今後の課題・未実装のタスク
- **カメラ撮影機能:** 投稿画面でギャラリー選択に加え、カメラによる写真・動画撮影を追加。`FileProvider` の設定と `ActivityResultContracts` (TakePicture / CaptureVideo) の実装が必要。詳細は `docs/post-screen-improvement-plan.md` を参照。
- **メディア選択 UI の改善:** 投稿画面のメディアプレビューに「×」クリアボタンを追加し、UX を向上させる。
- **ページネーション:** タイムライン投稿の取得をカーソルベースに移行し、高負荷時のパフォーマンスを改善。
- **オフライン対応:** Firestore のキャッシュ同期設定の最適化と Room の活用。
- **一時ファイルの削除:** 投稿完了後、`cacheDir` に保存された一時メディアファイルを自動削除する処理の追加。
