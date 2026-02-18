# AGENT.md

## 1. Project Overview
**Name:** Shibari (for Betsudotai)
**Description:** 友達グループ「別働隊」専用の縛り共有・習慣化アプリ。
**Core Value:** 監視と承認による習慣化と、部隊としての連帯感強化。

## 2. Tech Stack & Libraries
* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Material3)
* **Architecture:** MVVM + Clean Architecture (Layer-first)
* **DI:** Hilt (Dagger)
* **Async:** Kotlin Coroutines + Flow / StateFlow
* **Network (Main):** Firebase SDK (Auth, Firestore, Storage, Functions, Messaging)
* **Network (Sub):** Retrofit + OkHttp (for external APIs if needed)
* **Local DB:** Room (if needed), DataStore (Preferences)
* **Image Loading:** Coil
* **JSON Parsing:** Kotlin Serialization

## 3. Architecture & Directory Structure
**Layer-first Strategy** を採用。依存方向は \`Presentation -> Application -> Domain <- Data\` とする。

### Package Structure: \`com.betsudotai.shibari\`

\`\`\`text
├── domain (Pure Kotlin / Enterprise Rules)
│   ├── model (Data Class / Entity)
│   ├── repository (Interface definition)
│   └── usecase (Interface definition)
│
├── application (Business Logic Implementation)
│   └── usecase (Implementation of domain interfaces)
│
├── data (Interface Adapters / Frameworks)
│   ├── datasource
│   │   ├── remote (Firestore, Firebase Storage)
│   │   └── local (Room, DataStore)
│   ├── repository (Implementation of domain repositories)
│   └── dto (Data Transfer Objects for API/Firestore)
│
├── presentation (UI & State)
│   ├── ui
│   │   ├── screens (Composable Screens)
│   │   ├── components (Reusable Composables)
│   │   └── theme
│   └── viewmodel (Hilt ViewModel)
│
├── core (Shared Utilities)
│   ├── di (Hilt Modules)
│   ├── util (Extensions, Constants)
│   └── network (API Client wrappers)
\`\`\`

### Key Architectural Rules
1.  **Domain Layer is Pure:** AndroidフレームワークやFirebase SDKへの依存を持たないこと。
2.  **UseCase Separation:**
    * \`domain/usecase\`: インターフェース（契約）を定義。
    * \`application/usecase\`: 実装（ロジック）を記述。
3.  **Repository Pattern:** Data層の実装詳細は隠蔽し、Domain層のInterfaceを通じてアクセスする。
4.  **ViewModel:** UIロジックのみを担当。ビジネスロジックはUseCaseに委譲する。

## 4. Database Strategy (Firestore)
**NoSQL / Denormalization (非正規化)** を基本戦略とする。
Read性能を最大化するため、Joinが必要なデータは書き込み時にスナップショットとしてコピーする。

### Collections Schema

#### \`users\`
\`\`\`json
{
  "uid": "string",
  "displayName": "string",
  "photoUrl": "string",
  "fcmToken": "string"
}
\`\`\`

#### \`quests\` (Master Data)
IDは可読性のあるString IDを使用（例: \`daily_walk_1500\`）。
\`\`\`json
{
  "id": "string",
  "title": "string",
  "type": "prohibition | routine | achievement | challenge",
  "description": "string",
  "threshold": number (optional)
}
\`\`\`

#### \`timelines\` (Main Feed)
**[重要]** \`author\` と \`quest\` 情報は参照（Ref）ではなく、**Snapshot（コピー）**を持つ。
\`\`\`json
{
  "id": "uuid",
  "userId": "string", // Reference Key
  "questId": "string", // Reference Key
  
  // Denormalized Snapshots
  "author": {
    "displayName": "string",
    "photoUrl": "string"
  },
  "quest": {
    "title": "string",
    "type": "string"
  },

  "mediaUrl": "string",
  "mediaType": "image | video",
  "comment": "string",
  "status": "pending | approved | rejected | disputed",
  "approvalCount": number,
  "votes": {
    "user_uid_A": "approve",
    "user_uid_B": "reject"
  },
  "createdAt": "timestamp"
}
\`\`\`

## 5. Implementation Guidelines

### Coding Style
* **Jetpack Compose:**
    * 全てのScreenは \`ViewModel\` から \`State\` を受け取り、\`Event\` をコールバックとして返す（Unidirectional Data Flow）。
    * Previewを作成し、UIコンポーネントの独立性を保つ。
* **Coroutines:**
    * \`viewModelScope\` を使用する。
    * Flowの収集は \`collectAsStateWithLifecycle()\` を使用する。
* **DI (Hilt):**
    * \`@HiltViewModel\`, \`@AndroidEntryPoint\`, \`@Inject\` を適切に使用する。
    * InterfaceとImplementationの紐付けは \`di\` パッケージ内のModuleで行う（\`@Binds\` 推奨）。

### Error Handling
* Domain/Data層での例外は \`Result<T>\` ラッパークラス、または \`kotlin.runCatching\` を用いてViewModelまで伝播させる。
* UI層で \`Result.onFailure\` をハンドリングし、Snackbar等で表示する。

## 6. Development Roadmap (Current Phase)
**Phase 1: Foundation & Auth**
1.  [x] Project Setup & Directory Structure
2.  [ ] Define Domain Models (\`User\`, \`Quest\`, \`Timeline\`)
3.  [ ] Setup Firebase (Auth, Firestore) in Data Layer
4.  [ ] Implement Auth Repository & UseCase
5.  [ ] Create Login Screen
