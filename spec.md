# PRD: Reminder Web App (Apple Reminders 클론)

## 1. 개요

Apple의 iOS/macOS 기본 앱인 **Reminders**의 핵심 기능과 **UI/UX를 최대한 충실히 재현**하여 웹 환경에서 동일한 경험을 제공하는 것을 목표로 한다.
디자인 톤, 인터랙션, 마이크로 애니메이션, 키보드 사용성까지 Apple Reminders와 구분이 안 갈 정도로 가깝게 만든다.

- **Backend**: Spring Boot 4.0.3 + Kotlin 2.3 + JPA + H2 (in-memory) + REST API
- **Frontend**: Next.js (latest, App Router) + TypeScript + Tailwind CSS
- **디자인 기준**: macOS Sonoma/Sequoia의 Reminders 앱 (Apple HIG, SF Symbols, SF Pro 톤)
- **인증**: 1차 범위에서는 단일 사용자(로컬용)로 시작 → 향후 확장 고려

---

## 2. 목표 / 비목표

### Goals
- Apple Reminders의 **UI/UX를 최대한 충실히 재현**한다 (high-fidelity clone).
  - 레이아웃, 색감, 타이포, 간격, 모서리 라운드, 그림자, 호버/포커스 상태까지 동일한 톤.
  - 인터랙션: 체크 시 fade-out 후 완료 섹션으로 이동, 인라인 편집, 드래그 정렬, 슬라이드 삭제 등.
  - 키보드 사용성: macOS 앱의 단축키와 동일한 매핑 (`⌘N`, `⌘⇧N`, `⌫`, `Tab`/`Shift+Tab` indent 등).
- 기능적으로 Apple Reminders의 핵심 흐름을 웹에서 동일하게 재현한다 (리스트, 할 일 CRUD, 마감일/시간/우선순위/깃발, 하위 작업, 스마트 리스트, 검색).
- Backend는 RESTful API로 **프론트와 분리 가능한** 구조.
- Frontend는 Apple Reminders의 macOS 앱 레이아웃을 그대로 따른다 (좌측 사이드바 + 우측 컨텐츠 영역, 라이트/다크 모드).

### Non-Goals (1차 범위 외)
- 다중 사용자 인증/공유, OAuth, iCloud 동기화
- 위치 기반 알림, 푸시 알림, 음성 입력(Siri)
- 모바일 네이티브 앱
- 실시간 협업(WebSocket)
- 첨부파일/이미지 업로드 (단, **2차에서 우선 고려** — Apple은 지원)

---

## 3. 사용자 시나리오

1. 사용자는 사이드바에서 "쇼핑", "업무" 같은 **리스트**를 만든다.
2. 리스트를 선택하면 우측에 해당 리스트의 **할 일들**이 표시된다.
3. 할 일에 제목/메모/마감일/우선순위를 입력하고, 완료되면 체크한다.
4. 할 일은 **하위 작업**(subtask)으로 세분화할 수 있다.
5. 검색바에서 키워드로 모든 리스트의 할 일을 검색한다.
6. "오늘", "예정", "전체", "완료됨" 같은 **스마트 리스트(Smart List)** 로 빠르게 필터링한다.

---

## 4. 기능 요구사항

### 4.1 리스트(List) 관리
- 리스트 생성/조회/이름 수정/삭제
- 리스트별 색상(컬러 코드) 및 아이콘(이모지/심볼) 지정
- 리스트 정렬 순서 변경 (drag-and-drop은 2차)
- 리스트 삭제 시 포함된 할 일은 함께 삭제 (cascade)

### 4.2 할 일(Reminder) 관리
- 필드:
  - `title` (필수)
  - `notes` (메모, 선택)
  - `dueAt` (마감 일시, 선택)
  - `priority` (NONE / LOW / MEDIUM / HIGH)
  - `completed` (boolean)
  - `completedAt` (완료 시각)
  - `flagged` (깃발 표시, boolean)
  - `listId` (소속 리스트, 필수)
  - `parentId` (상위 할 일, 선택 — 하위 작업 표현)
  - `sortOrder` (리스트 내 순서)
- CRUD + 완료 토글 + 깃발 토글
- 하위 작업: 1단계 깊이까지 지원 (Apple Reminders 동일)

### 4.3 스마트 리스트 (View)
백엔드의 별도 엔티티 없이 **쿼리 기반**으로 제공:
- **오늘(Today)**: `dueAt`이 오늘인 미완료 할 일
- **예정(Scheduled)**: `dueAt`이 미래인 미완료 할 일 (날짜별 그룹)
- **전체(All)**: 모든 미완료 할 일
- **깃발 표시(Flagged)**: `flagged = true`인 할 일
- **완료됨(Completed)**: `completed = true`인 할 일

### 4.4 검색
- 제목/메모에 대한 부분 일치 검색 (대소문자 무시)
- 리스트 전체 범위에서 검색

---

## 5. 데이터 모델 (JPA)

### `ReminderList`
| 필드 | 타입 | 비고 |
|---|---|---|
| id | Long (PK) | auto |
| name | String | not null |
| color | String | hex, e.g. `#FF9500` |
| icon | String | 이모지 또는 심볼 식별자 |
| sortOrder | Int | |
| createdAt | Instant | |
| updatedAt | Instant | |

### `Reminder`
| 필드 | 타입 | 비고 |
|---|---|---|
| id | Long (PK) | auto |
| list | ManyToOne → ReminderList | not null, FK |
| parent | ManyToOne → Reminder | nullable (subtask) |
| title | String | not null |
| notes | String? | TEXT |
| dueAt | Instant? | |
| priority | Enum (NONE/LOW/MEDIUM/HIGH) | default NONE |
| completed | Boolean | default false |
| completedAt | Instant? | |
| flagged | Boolean | default false |
| sortOrder | Int | |
| createdAt | Instant | |
| updatedAt | Instant | |

> 인덱스: `(list_id, completed)`, `(due_at)`, `(parent_id)`

---

## 6. REST API 설계 (`/api/v1`)

### Lists
| Method | Path | 설명 |
|---|---|---|
| GET | `/lists` | 모든 리스트 + 미완료 카운트 |
| POST | `/lists` | 리스트 생성 |
| GET | `/lists/{id}` | 단건 조회 |
| PATCH | `/lists/{id}` | 이름/색상/아이콘/순서 수정 |
| DELETE | `/lists/{id}` | 리스트 삭제 (cascade) |

### Reminders
| Method | Path | 설명 |
|---|---|---|
| GET | `/lists/{listId}/reminders` | 특정 리스트의 할 일 (쿼리: `completed`, `parentId`) |
| POST | `/lists/{listId}/reminders` | 할 일 생성 |
| GET | `/reminders/{id}` | 단건 조회 |
| PATCH | `/reminders/{id}` | 부분 수정 (title/notes/dueAt/priority/flagged/sortOrder/parentId) |
| POST | `/reminders/{id}/toggle` | 완료 토글 |
| DELETE | `/reminders/{id}` | 삭제 |

### Smart Views
| Method | Path | 설명 |
|---|---|---|
| GET | `/views/today` | 오늘 마감 미완료 |
| GET | `/views/scheduled` | 미래 마감 미완료 |
| GET | `/views/all` | 모든 미완료 |
| GET | `/views/flagged` | 깃발 표시 |
| GET | `/views/completed` | 완료됨 |

### Search
| Method | Path | 설명 |
|---|---|---|
| GET | `/search?q={keyword}` | 제목/메모 검색 |

### 공통 규약
- 응답 포맷: JSON, 시각은 ISO-8601 UTC
- 에러: `{ "code": "...", "message": "...", "fieldErrors": [...] }`
- Validation: `spring-boot-starter-validation` 사용 (`@NotBlank`, `@Size` 등)

---

## 7. 프론트엔드 (Next.js)

### 기술 스택
- Next.js 15+ (App Router, RSC)
- TypeScript (strict)
- Tailwind CSS + shadcn/ui (또는 Radix UI 기반 컴포넌트)
- 데이터 페칭: **TanStack Query** (낙관적 업데이트 위해)
- 폼: react-hook-form + zod
- 아이콘: lucide-react

### 화면 구성 (macOS Reminders 그대로)
- **레이아웃**: 2-pane, 좌측 사이드바는 **resizable**, 상단 윈도우 컨트롤 영역엔 정렬·뷰 토글 버튼
  - 좌측 사이드바
    - 상단 검색바 (`⌘F`)
    - 스마트 리스트 5종 그리드 카드: **오늘 / 예정 / 전체 / 깃발 표시 / 완료됨** (각 카드는 둥근 모서리, 좌상단 큰 카운트 숫자, 우하단 SF Symbol 톤 아이콘, 리스트 색)
    - "내 목록" 헤더 + 사용자 리스트 (이모지/심볼 + 이름 + 미완료 카운트)
    - 하단 "리스트 추가" 버튼
  - 우측 컨텐츠
    - 상단: 리스트명(큰 타이틀, 리스트 색상으로 컬러링) + 우측 정렬·옵션 메뉴
    - 본문: 할 일 행(체크 원 + 제목 + 메타) — 미완료/완료 섹션 분리
    - 빈 상태(empty state)도 Apple과 동일한 톤 (옅은 회색 텍스트 + 아이콘)
- **라우트**:
  - `/` → 기본 뷰 (오늘 또는 마지막 선택 리스트)
  - `/lists/[id]` → 특정 리스트
  - `/views/[type]` → 스마트 뷰 (today, scheduled, all, flagged, completed)
  - `/search?q=...`

### 주요 컴포넌트
- `Sidebar`: 스마트 리스트 + 사용자 리스트
- `ReminderRow`: 체크박스 + 제목 + 메타 (마감/깃발/우선순위)
- `ReminderDetailPanel`: 우측 슬라이드 패널 (상세 편집)
- `NewListDialog`, `NewReminderInput`
- `SearchBar`

### UX 디테일 (Apple Reminders 충실 재현)

#### 인터랙션 / 마이크로 애니메이션
- **체크 토글**: 원형 체크박스 클릭 → 체크 마크 spring 애니메이션 → 약 0.8~1.0초 뒤 행이 fade + slide로 사라지며 "완료됨" 섹션으로 이동.
  - 체크 직후 다시 누르면 취소 (실수 방지) — Apple과 동일.
- **인라인 편집**: 제목/메모 영역 클릭 시 즉시 텍스트 편집 모드. `Enter`로 같은 들여쓰기에 새 할 일 추가, `Tab`/`Shift+Tab`로 하위/상위 변경.
- **상세 정보(i)**: 행 hover 시 우측에 ⓘ 아이콘 노출 → 클릭하면 **인라인 익스팬더**(상세 폼: 메모, 날짜, 시간, 우선순위, 깃발, URL, 하위 작업)가 행 아래에서 펼쳐짐. (Apple은 모달이 아니라 인라인 확장)
- **드래그 정렬**: 행/리스트를 long-press 후 드래그로 순서 변경. 같은 리스트 안의 할 일, 또는 다른 리스트로 이동 모두 지원.
- **스와이프 액션** (트랙패드/마우스 가로 스와이프, 또는 우클릭 메뉴): 깃발, 정보, 삭제.
- **삭제 확인**: 리스트 삭제 시 Apple 스타일 confirm dialog ("이 목록을 삭제하시겠습니까? 모든 미리 알림이 삭제됩니다.")
- **호버 상태**: 행 hover 시 옅은 배경(라이트 모드 ~3% 회색), 선택된 행은 리스트 색상의 옅은 틴트.
- **사이드바 너비 조절**: 사이드바 우측 경계 드래그로 resize.

#### 키보드 단축키 (macOS 동일)
| 단축키 | 동작 |
|---|---|
| `⌘N` | 새 미리 알림 |
| `⌘⇧N` | 새 목록 |
| `Enter` | 같은 들여쓰기로 새 할 일 |
| `Tab` / `Shift+Tab` | 하위 작업으로 들여쓰기 / 내어쓰기 |
| `⌫` (빈 행) | 행 삭제 |
| `⌘F` | 검색 포커스 |
| `⌘1`~`⌘5` | 스마트 리스트 전환 (오늘/예정/전체/깃발/완료) |
| `Esc` | 편집 취소 |
| `⌘.` | 선택한 할 일 완료 토글 |

#### 비주얼 시스템
- **타이포그래피**: SF Pro (시스템 스택 fallback: `-apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif`).
  - 큰 타이틀(리스트명): 28~32px / Semibold.
  - 본문 행: 14px Regular, 메타(날짜/우선순위) 12~13px / Secondary color.
- **색상 팔레트** (Apple 시스템 컬러 매칭):
  - Red `#FF3B30`, Orange `#FF9500`, Yellow `#FFCC00`, Green `#34C759`, Mint `#00C7BE`, Teal `#30B0C7`, Cyan `#32ADE6`, Blue `#0A84FF`(기본), Indigo `#5E5CE6`, Purple `#AF52DE`, Pink `#FF2D55`, Brown `#A2845E`, Gray `#8E8E93`.
  - 라이트 BG `#FFFFFF`/`#F2F2F7`, 다크 BG `#1C1C1E`/`#2C2C2E`.
  - 사이드바는 macOS의 vibrancy material 느낌(반투명 + blur)을 CSS `backdrop-filter`로 근사.
- **체크박스**: 24px 원형 outline. 체크 시 리스트 색상으로 채워지고 SF Symbol 톤의 체크마크.
- **우선순위 뱃지**: 제목 앞에 `!`(LOW), `!!`(MEDIUM), `!!!`(HIGH) 빨간색 텍스트로 prefix (Apple과 동일).
- **깃발 아이콘**: 행 우측에 주황색 깃발 (`flagged=true`).
- **마감일 표시**: 제목 아래 작은 회색 텍스트로 "오늘", "내일, 오후 3:00", "2026년 5월 1일 금요일" 형태 (`Intl.DateTimeFormat` 한국어 로케일).
- **모서리 라운드**: 카드 10px, 행 6px, 사이드바 패널 10px.
- **다크 모드**: 시스템 prefers-color-scheme 기본 + 토글 제공.

#### 빈 상태 / 로딩
- 빈 리스트: 큰 회색 아이콘 + "미리 알림 없음" 텍스트.
- 로딩: skeleton 행 (Apple은 거의 안 보이지만, 웹 환경에선 필요).

---

## 8. 비기능 요구사항

- **성능**: 리스트당 할 일 1,000건까지 부드럽게 렌더 (가상 스크롤은 2차)
- **개발 환경**:
  - Backend: `./gradlew bootRun` → `http://localhost:8080`
  - Frontend: `npm run dev` → `http://localhost:3000`
  - CORS: 개발 환경에서 `http://localhost:3000` 허용
- **데이터 영속성**: 1차는 H2 in-memory (`create-drop`) → 추후 파일 모드 또는 Postgres 전환 고려
- **로깅**: kotlin-logging 사용, API 요청 로깅 필터
- **테스트**:
  - Backend: 서비스 단위 테스트(MockK), 컨트롤러 슬라이스 테스트
  - Frontend: 핵심 컴포넌트 단위 테스트 (선택)

---

## 9. 패키지 구조 (Backend 제안)

```
com.bong.reminder
├── ReminderApplication.kt
├── config/         (CORS, Jackson, etc.)
├── common/         (ErrorResponse, GlobalExceptionHandler)
├── list/
│   ├── ReminderList.kt
│   ├── ReminderListRepository.kt
│   ├── ReminderListService.kt
│   ├── ReminderListController.kt
│   └── dto/
└── reminder/
    ├── Reminder.kt
    ├── ReminderRepository.kt
    ├── ReminderService.kt
    ├── ReminderController.kt
    ├── ReminderViewController.kt   (smart views, search)
    └── dto/
```

---

## 10. 마일스톤

| 단계 | 범위 | 산출물 |
|---|---|---|
| **M1. 백엔드 도메인** | List/Reminder 엔티티, Repository, 기본 CRUD API | 빌드 성공 + 단위 테스트 |
| **M2. 스마트 뷰/검색** | views/* 엔드포인트, 검색 API, 하위작업 | API 동작 확인 |
| **M3. 프론트 골격** | Next.js 프로젝트, 사이드바/리스트 뷰 골격, API 클라이언트 | 리스트 CRUD 화면 |
| **M4. 할 일 UX** | 인라인 편집, 완료 토글 애니메이션, 상세 패널 | 핵심 흐름 완성 |
| **M5. 다듬기** | 키보드 단축키, 빈 상태/로딩, 다크 모드 | 데모 가능 수준 |

---

## 11. UI 충실도 검증 기준 (Acceptance)

각 마일스톤에서 **육안 비교**로 합격 여부를 판단한다. 비교 대상은 macOS Sequoia의 Reminders 앱 스크린샷.
- 사이드바 스마트 리스트 카드의 색/크기/간격이 거의 동일한가?
- 체크 토글 애니메이션 타이밍과 곡선이 자연스러운가? (spring, ~0.3s)
- 인라인 익스팬더가 행 아래에서 부드럽게 펼쳐지는가?
- 라이트/다크 모드 전환 시 색이 모두 의도대로 매핑되는가?
- 키보드만으로 리스트 추가 → 할 일 추가 → 하위 작업 → 완료까지 가능한가?
- 한국어 날짜 포맷(`내일, 오후 3:00`)이 제대로 표시되는가?

> 픽셀 단위 완벽 일치는 목표가 아니지만, **3m 떨어져 보면 구분이 안 갈 정도**가 합격선.

---

## 12. 열린 질문 (리뷰 요청 포인트)

1. **인증**: 1차에서 정말 단일 사용자로 갈지, 처음부터 간단한 사용자 분리(JWT)를 둘지?
2. **반복(Repeat)** 일정 (매일/매주 등) 1차 포함 여부 — Apple은 지원하지만 구현 비용 큼.
3. **태그(#tag)** 기능 1차 포함 여부 — Apple은 iOS 16+에서 지원.
4. **섹션(Sections)**: Apple은 한 리스트 내에서 섹션으로 그룹화 가능. 1차 포함?
5. **데이터 영속성**: 데모 종료 후에도 데이터가 남아야 하는지? (H2 file 모드로 전환 필요 여부)
6. **프론트 컴포넌트 라이브러리**: shadcn/ui로 진행해도 괜찮을지, 아니면 순수 Tailwind?
7. **폰트 라이선스**: SF Pro는 Apple 폰트 — 웹 환경에서 시스템 스택으로 fallback 처리할지, 유사 오픈소스 폰트(Inter)를 쓸지.
8. **배포**: 어디까지 고려? (단일 머신 도커 컴포즈 vs 배포 X)
