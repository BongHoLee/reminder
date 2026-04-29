# 개발 계획 (Phased Plan)

`spec.md`의 요구사항을 **점진적 빌드업** 방식으로 구현한다. 각 Phase는 독립적으로 동작 가능한 산출물을 만들어내며, 다음 Phase의 기반이 된다.

---

## 원칙

- **Walking Skeleton 우선**: Phase 1에서 백엔드~프론트엔드까지 한 줄짜리 흐름을 먼저 잇는다.
- **Backend-first per feature**: 각 기능은 API → 프론트 순으로. API가 있어야 프론트가 mock 없이 진행됨.
- **UI 충실도는 후반에 강화**: Phase 5~6에서 Apple Reminders 디자인을 본격 반영. 그 전엔 기능 동작이 우선.
- **Phase 종료 조건**: 모든 Phase는 ① 빌드/테스트 통과 ② 수동 동작 확인 ③ 다음 Phase 시작 가능한 상태.

---

## 기술 스택 (확정)

### Backend
- **언어/런타임**: Kotlin 2.3 + JDK 25
- **프레임워크**: Spring Boot 4.0.3 (`spring-boot-starter-webmvc`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`)
- **DB**: H2 in-memory (`create-drop`) → Phase 7에서 file 모드 또는 Postgres 전환 검토
- **로깅**: `io.github.oshai:kotlin-logging-jvm`
- **테스트**: JUnit 5 + MockK + springmockk + `@DataJpaTest` / `@WebMvcTest`
- **빌드**: Gradle Kotlin DSL (이미 설정 완료)
- **JSON**: Jackson Kotlin module (Instant ISO-8601 직렬화)
- **CORS**: 개발 환경에서 `http://localhost:3000` 허용
- **API 버저닝**: `/api/v1/...`

### Frontend
- **프레임워크**: Next.js 15+ (App Router, RSC, Turbopack)
- **언어**: TypeScript (strict)
- **스타일링**: Tailwind CSS v4 + CSS variables for theming (light/dark)
- **컴포넌트**: shadcn/ui (Radix UI 기반) — 필요 부분만 cherry-pick
- **상태/데이터**: TanStack Query v5 (낙관적 업데이트), React Context는 최소
- **폼**: react-hook-form + zod
- **아이콘**: lucide-react (SF Symbols 톤에 가장 근접)
- **애니메이션**: framer-motion (체크 fade, 익스팬더 펼침)
- **DnD**: @dnd-kit/core (드래그 정렬)
- **HTTP 클라이언트**: ky 또는 fetch wrapper
- **단축키**: react-hotkeys-hook
- **날짜**: date-fns + Intl API (한국어 로케일)
- **테스트**: Vitest + React Testing Library (선택)

### 개발 환경
- Backend: `./gradlew bootRun` → `http://localhost:8080`
- Frontend: `npm run dev` → `http://localhost:3000`
- 동시 실행: 루트에 `concurrently` 또는 별 터미널 2개

---

## Phase 0 — 셋업 (0.5일)

이미 Spring Boot가 초기화되어 있으니, 프로젝트 골격만 마무리한다.

### Backend
- [ ] `application.yml`에 CORS 프로파일/시간대(`spring.jackson.time-zone=UTC`) 정리
- [ ] `WebConfig` (CORS configurer) 추가 — `http://localhost:3000` 허용
- [ ] 기본 패키지 구조 생성: `config/`, `common/`, `list/`, `reminder/`
- [ ] `GlobalExceptionHandler` + `ErrorResponse` DTO
- [ ] 헬스 체크: `GET /api/v1/health` → `{"status":"UP"}`

### Frontend
- [ ] `npx create-next-app@latest` (TS, Tailwind, App Router, src/, alias `@/`)
- [ ] 위치는 backend 루트 옆 `frontend/` 디렉토리
- [ ] Tailwind 설정 + CSS 변수 기반 테마 토큰 정의 (라이트/다크)
- [ ] `lib/api.ts` (fetch wrapper, baseURL = `http://localhost:8080/api/v1`)
- [ ] TanStack Query Provider 등록
- [ ] Health 호출하여 백엔드 연결 검증

### Exit Criteria
- Backend `bootRun` 가능, Frontend가 backend health에 접속해 OK 표시.

---

## Phase 1 — 리스트 CRUD (Walking Skeleton, 1~1.5일)

가장 단순한 기능부터: **리스트만** 만들고 보여준다. 할 일은 다음 Phase.

### Backend
- [ ] 엔티티 `ReminderList` (id, name, color, sortOrder, createdAt, updatedAt)
- [ ] `ReminderListRepository : JpaRepository<ReminderList, Long>` + `findAllByOrderBySortOrderAsc()`
- [ ] DTO: `ReminderListResponse`, `ReminderListCreateRequest`, `ReminderListUpdateRequest`
- [ ] `ReminderListService` (CRUD)
- [ ] `ReminderListController`:
  - `GET /api/v1/lists`
  - `POST /api/v1/lists`
  - `PATCH /api/v1/lists/{id}`
  - `DELETE /api/v1/lists/{id}`
- [ ] 단위 테스트: 서비스 + `@WebMvcTest` 컨트롤러 슬라이스

### Frontend
- [ ] 좌측 사이드바 골격 (스마트 리스트 자리는 placeholder)
- [ ] 사용자 리스트 영역에 백엔드 데이터 표시
- [ ] "리스트 추가" 다이얼로그 (이름/색 선택)
- [ ] 우측 클릭 → 이름 변경 / 삭제

### Exit Criteria
- 사이드바에서 리스트 생성/이름 변경/삭제가 동작.
- 새로고침해도 동일 (in-memory 라 재시작 시엔 사라짐 — 정상).

---

## Phase 2 — 미리 알림(Reminder) CRUD + 완료 토글 (1.5일)

### Backend
- [ ] 엔티티 `Reminder` (id, listId FK, parentId FK, title, notes, dueAt, priority, completed, completedAt, flagged, sortOrder, createdAt, updatedAt)
- [ ] enum `Priority(NONE, LOW, MEDIUM, HIGH)`
- [ ] `ReminderRepository`: `findByListIdAndCompleted`, `findByParentId`
- [ ] DTO + Service + Controller:
  - `GET /api/v1/lists/{listId}/reminders?completed=false`
  - `POST /api/v1/lists/{listId}/reminders` (title 필수)
  - `PATCH /api/v1/reminders/{id}` (부분 업데이트)
  - `POST /api/v1/reminders/{id}/toggle` (완료 토글)
  - `DELETE /api/v1/reminders/{id}`
- [ ] cascade: 리스트 삭제 시 해당 리스트의 reminders 삭제 (`@OnDelete(CASCADE)` 또는 서비스 레벨)
- [ ] 테스트: 토글, cascade 삭제 케이스 포함

### Frontend
- [ ] 우측 컨텐츠 영역: 선택된 리스트의 할 일 목록
- [ ] 라우트 `/lists/[id]`
- [ ] **`ReminderRow`**: 체크박스 + 제목, 클릭 시 인라인 편집
- [ ] **`NewReminderInput`**: 리스트 상단 input, Enter로 추가
- [ ] 체크 토글 — TanStack Query 낙관적 업데이트
- [ ] 미완료/완료 섹션 분리

### Exit Criteria
- 리스트 클릭 → 할 일 입력/체크/삭제까지 매끄럽게 동작.
- 완료 시 일단 즉시 완료 섹션으로 이동 (애니메이션은 Phase 5).

---

## Phase 3 — 상세 정보 (마감일·우선순위·메모·깃발) + 인라인 익스팬더 (1.5일)

### Backend
- 이미 Phase 2에서 필드는 모두 있음. 별도 작업 없음 (검증 메시지만 다듬기).
- [ ] Validation: title `@NotBlank @Size(max=500)`, notes `@Size(max=10_000)`

### Frontend
- [ ] 행 hover 시 우측에 ⓘ 아이콘 노출
- [ ] ⓘ 클릭 → **인라인 익스팬더** 열림 (행 아래에서 펼쳐짐, framer-motion)
- [ ] 익스팬더 내부 폼:
  - 메모(textarea)
  - 마감일/시간 (date+time picker, 한국어 로케일)
  - 우선순위 (드롭다운 또는 segmented control)
  - 깃발 토글
- [ ] react-hook-form + zod 사용, debounce 후 PATCH
- [ ] 행 메타 영역에 마감일/우선순위(`!!!`)/깃발 표시

### Exit Criteria
- 모든 reminder 필드를 UI에서 입력/수정 가능.
- 한국어 날짜 포맷 (`내일, 오후 3:00`) 정상 표시.

---

## Phase 4 — 하위 작업(Subtask) + 검색 + 스마트 뷰 (2일)

### Backend
- [ ] `Reminder.parentId` 활용 — 1단계 깊이만 허용 (서비스에서 검증: parent의 parent != null 거부)
- [ ] `ReminderSearchService`:
  - `GET /api/v1/search?q=...` (title/notes ilike)
- [ ] `ReminderViewController`:
  - `GET /api/v1/views/today` (오늘 0~23:59 마감, completed=false)
  - `GET /api/v1/views/scheduled` (미래 마감, completed=false, dueAt asc, 그룹용 dueDate 포함)
  - `GET /api/v1/views/all` (전체 미완료)
  - `GET /api/v1/views/flagged` (flagged=true)
  - `GET /api/v1/views/completed` (completed=true)
- [ ] 시간대: 서버는 UTC 저장, 쿼리는 클라이언트의 timezone 파라미터로 (`?tz=Asia/Seoul`)
- [ ] 테스트: 경계값(자정 직전/직후), parent 깊이 거부

### Frontend
- [ ] 사이드바 상단에 **스마트 리스트 카드 5종** (오늘/예정/전체/깃발/완료) — 각 카드 카운트 표시
- [ ] 라우트 `/views/[type]` 구현
- [ ] 검색바 (`⌘F`로 포커스, `/search?q=...`)
- [ ] 하위 작업 UI:
  - `Tab` / `Shift+Tab`로 들여쓰기 / 내어쓰기
  - 시각적 indent
- [ ] 스마트 뷰별 그룹핑 (예정 → 날짜별)

### Exit Criteria
- 5개 스마트 뷰 + 검색 + 하위 작업 동작.
- 키보드만으로 새 할 일 → Tab → 하위 작업 → Enter → 같은 깊이 추가가 자연스러움.

---

## Phase 5 — UI 충실도 1차 (Apple Reminders 디자인 매칭, 2~3일)

기능이 갖춰졌으니 이제 **외형/인터랙션을 Apple과 동일하게 끌어올린다**. 사용자가 가장 만족할 단계.

### 디자인 시스템 정립
- [ ] CSS 변수로 Apple 시스템 컬러 13종 정의 (라이트/다크)
- [ ] 타이포 스택: SF Pro fallback chain
- [ ] 간격/라운드/그림자 토큰 (`--radius-row: 6px`, `--radius-card: 10px`)

### 사이드바
- [ ] 스마트 리스트를 **2x3 그리드 카드**로 (Apple과 동일)
  - 각 카드: 좌상단 큰 카운트, 우하단 SF 톤 아이콘, 리스트 색 background
- [ ] vibrancy 효과 (반투명 + `backdrop-filter: blur(40px)`)
- [ ] 사이드바 너비 resize (드래그)
- [ ] 사용자 리스트 행: 리스트 색 점 + 이름 + 우측 카운트
- [ ] 하단 "리스트 추가" 버튼 (Apple 위치)

### 컨텐츠 영역
- [ ] 상단 큰 타이틀 (리스트 색상 컬러링)
- [ ] 행 디자인: 24px 원형 체크박스, 체크 시 리스트 색으로 채워짐
- [ ] 우선순위 prefix `!`, `!!`, `!!!` (빨강)
- [ ] 깃발 우측 표시 (주황)
- [ ] 마감일 작은 회색 텍스트로 제목 아래

### 인터랙션 / 애니메이션
- [ ] 체크 토글: spring 애니메이션 → 0.8s 후 fade+slide로 완료 섹션 이동 (framer-motion `AnimatePresence`)
- [ ] 익스팬더: spring 펼침/접힘
- [ ] 호버 상태: 옅은 회색 배경, 선택 시 리스트 색 틴트
- [ ] 빈 상태: 큰 회색 아이콘 + "미리 알림 없음"
- [ ] 다크 모드 (시스템 prefers-color-scheme + 토글)

### Exit Criteria
- macOS Reminders 스크린샷과 사이드바/행/카드를 나란히 놓고 봐도 톤 일치.
- 체크 애니메이션이 "툭" 하지 않고 부드러운 spring 곡선.

---

## Phase 6 — 키보드/드래그/스와이프 등 고급 인터랙션 (1.5일)

### 키보드 단축키 (macOS 동일)
- [ ] `⌘N` / `⌘⇧N` / `Enter` / `Tab` / `Shift+Tab` / `⌫`(빈 행) / `⌘F` / `⌘1~5` / `Esc` / `⌘.`
- [ ] react-hotkeys-hook + 모달/입력 컨텍스트 분기

### 드래그 정렬
- [ ] @dnd-kit으로 행/리스트 reorder
- [ ] 다른 리스트로 드래그 이동 (PATCH `listId`)
- [ ] 백엔드 batch sortOrder 업데이트 endpoint:
  - `POST /api/v1/lists/reorder` body: `[{id, sortOrder}, ...]`
  - `POST /api/v1/lists/{listId}/reminders/reorder` 동일 패턴

### 스와이프/우클릭 메뉴
- [ ] 우클릭 컨텍스트 메뉴 (Radix ContextMenu): 깃발, 정보, 삭제
- [ ] 트랙패드 가로 스와이프는 우선 제외 (브라우저 호환성 이유)

### Exit Criteria
- 마우스 없이 키보드만으로 모든 핵심 흐름 가능.
- 드래그로 리스트 간 이동 가능.

---

## Phase 7 — 다듬기 / 영속성 / 배포 (선택, 1~2일)

### 영속성
- [ ] H2 file 모드(`jdbc:h2:file:./data/reminderdb`) 또는 Postgres 전환
- [ ] Flyway 도입 (스키마 마이그레이션)
- [ ] seed 데이터 (개발 편의)

### 다듬기
- [ ] 빈 상태 / 로딩 skeleton 다듬기
- [ ] 에러 토스트 (sonner)
- [ ] i18n 준비 (한/영) — 1차는 한국어 고정도 OK
- [ ] Lighthouse / 접근성 점검 (포커스 링, ARIA)
- [ ] E2E (Playwright) 핵심 시나리오 1~2개

### 배포 (옵션)
- [ ] Backend Dockerfile (eclipse-temurin:25-jre)
- [ ] Frontend Dockerfile (Next standalone output)
- [ ] `docker-compose.yml` (backend + frontend + postgres)

### Exit Criteria
- 재시작 후 데이터 유지(영속성 선택 시).
- 데모 가능한 완성도.

---

## 의존성 / 리스크 / 결정 필요

### 결정 필요 (spec.md §12 열린 질문에서 가져옴)
1. **인증** — Phase 0 시작 전 결정 필요. 단일 사용자(본 plan 기준) vs JWT 도입.
2. **반복 일정 / 태그 / 섹션** — 1차 비포함이 기본. 포함 시 Phase 4~5 사이에 별도 Phase 추가.
3. **데이터 영속성** — Phase 7로 미루는 것이 기본. 데모 종료 후 데이터 유지가 필요하면 Phase 0에서 H2 file 모드로 시작.
4. **shadcn/ui 사용 여부** — 본 plan은 사용 가정.
5. **SF Pro 폰트** — 시스템 스택 fallback이 기본. Inter 등 대체는 Phase 5에서 결정.

### 리스크
- **시간대 처리**: 서버는 UTC, 클라는 Asia/Seoul. "오늘" 뷰의 경계 버그 자주 발생 → Phase 4에서 단위 테스트 강화.
- **체크 애니메이션 + 낙관적 업데이트**: 토글 직후 fade-out 중 다시 클릭하는 케이스 → Phase 5에서 상태머신으로 정리.
- **드래그 정렬 sortOrder 충돌**: 동시성 무시(단일 사용자) 가정. 다중 사용자 시 LexoRank 등 고려.
- **Spring Boot 4.0.x + Kotlin 2.3.x + JDK 25**는 매우 신버전 — 의존성 호환성 이슈 발생 시 Spring Boot 3.x로 다운그레이드 옵션.

---

## 일정 요약

| Phase | 내용 | 예상 |
|---|---|---|
| 0 | 셋업 | 0.5d |
| 1 | 리스트 CRUD | 1.5d |
| 2 | Reminder CRUD + 토글 | 1.5d |
| 3 | 상세 + 익스팬더 | 1.5d |
| 4 | Subtask + 검색 + 스마트 뷰 | 2d |
| 5 | UI 충실도 1차 | 3d |
| 6 | 키보드 + 드래그 | 1.5d |
| 7 | 다듬기/영속성/배포 | 1~2d |
| **합계** | | **~12.5~13.5d** |
