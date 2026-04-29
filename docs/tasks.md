# 작업 목록 (Tasks)

`plan.md`의 각 Phase를 실제 작업 단위로 분해한 체크리스트.
완료한 항목은 `[x]`로 표시.

---

## Phase 0 — 셋업

### Backend
- [x] `application.yml` 정리: `spring.jackson.time-zone=UTC`, 프로파일 분리(`dev`)
- [x] `config/WebConfig.kt` 생성 — `WebMvcConfigurer.addCorsMappings`로 `http://localhost:3000` 허용
- [x] 패키지 구조 생성: `config/`, `common/`, `list/`, `reminder/`
- [x] `common/ErrorResponse.kt` DTO 정의 (`code`, `message`, `fieldErrors`)
- [x] `common/GlobalExceptionHandler.kt` (`@RestControllerAdvice`): `MethodArgumentNotValidException`, `IllegalArgumentException`, `ReminderListNotFoundException` 매핑
- [x] `common/HealthController.kt`: `GET /api/v1/health` → `{"status":"UP"}`
- [ ] `./gradlew bootRun` 동작 확인
- [ ] `curl http://localhost:8080/api/v1/health` 200 확인

### Frontend
- [x] `frontend/` 디렉토리에서 `npx create-next-app@latest .` 실행 (TS, Tailwind, App Router, src/, alias `@/`)
- [x] `tailwind.config.ts`에 디자인 토큰 placeholder 추가 (Phase 5에서 본격 정의)
- [x] `globals.css`에 라이트/다크 CSS 변수 스캐폴드
- [x] `src/lib/api.ts`: fetch wrapper, baseURL = `http://localhost:8080/api/v1`
- [x] `src/app/providers.tsx`: TanStack Query `QueryClientProvider` 등록
- [x] `src/app/layout.tsx`에 Provider 적용
- [x] 루트 페이지에서 `/health` 호출하여 "Backend OK" 표시
- [ ] `npm run dev` 동작 확인

### Exit Criteria 검증
- [ ] backend `bootRun` + frontend `npm run dev` 동시 기동 시 health OK 노출

---

## Phase 1 — 리스트 CRUD (Walking Skeleton)

### Backend — 도메인
- [x] `common/BaseEntity.kt` (`@MappedSuperclass` + `@EntityListeners(AuditingEntityListener)`) — `id`(`IDENTITY`) / `createdAt` / `updatedAt` 공통 제공
- [x] `config/JpaConfig.kt` — `@EnableJpaAuditing`
- [x] `list/ReminderList.kt` 엔티티 (name, color, sortOrder) — `BaseEntity` 상속
- [x] `list/ReminderListRepository.kt` (`JpaRepository`, `findAllByOrderBySortOrderAsc()`)

### Backend — DTO/Service/Controller
- [x] `list/dto/ReminderListResponse.kt`
- [x] `list/dto/ReminderListCreateRequest.kt` (`@NotBlank name`, `@Size color`)
- [x] `list/dto/ReminderListUpdateRequest.kt` (모두 nullable)
- [x] `list/ReminderListService.kt`: `findAll`, `create`, `update`, `delete`
- [x] `list/adapter/in/web/ReminderListController.kt` (헥사고날 위치):
  - [x] `GET /api/v1/lists`
  - [x] `POST /api/v1/lists` (201 + Location)
  - [x] `PATCH /api/v1/lists/{id}`
  - [x] `DELETE /api/v1/lists/{id}` (204)
- [x] `docs/openapi.yml` — OpenAPI 3.1 스펙 표준 형식

### Backend — 테스트
- [x] `ReminderListServiceTest` (MockK 기반)
- [x] `ReminderListControllerTest` (`@WebMvcTest` + MockMvc + `@MockkBean`)

### Frontend
- [ ] `src/components/Sidebar.tsx` 골격 (smart list placeholder + 사용자 리스트 영역)
- [ ] `src/lib/queries/lists.ts`: `useLists`, `useCreateList`, `useUpdateList`, `useDeleteList`
- [ ] `src/components/NewListDialog.tsx` (이름/색상 선택)
- [ ] 사이드바 리스트 항목 우클릭 메뉴: 이름 변경 / 삭제 (Radix ContextMenu)
- [ ] 라우트 `/lists/[id]` 스텁 (제목만 표시)

### Exit Criteria 검증
- [ ] 사이드바에서 리스트 생성/이름 변경/삭제 동작
- [ ] 새로고침 시 백엔드 데이터 그대로 표시 (in-memory 재시작 시 초기화는 정상)

---

## Phase 2 — Reminder CRUD + 완료 토글

### Backend — 도메인
- [x] `reminder/Priority.kt` enum (`NONE`, `LOW`, `MEDIUM`, `HIGH`)
- [x] `reminder/Reminder.kt` 엔티티 (list FK, parent FK, title, notes, dueAt, priority, completed, completedAt, flagged, sortOrder) — `BaseEntity` 상속
- [x] `reminder/ReminderRepository.kt`
  - [x] `findByListIdAndCompletedOrderBySortOrderAsc(listId, completed)`
  - [x] `findByParentIdOrderBySortOrderAsc(parentId)`
  - [x] `deleteByListId(listId)` 또는 cascade

### Backend — DTO/Service/Controller
- [x] `reminder/dto/ReminderResponse.kt`
- [x] `reminder/dto/ReminderCreateRequest.kt` (title 필수)
- [x] `reminder/dto/ReminderUpdateRequest.kt` (모두 nullable, 부분 업데이트)
- [x] `reminder/ReminderService.kt`:
  - [x] `findByList(listId, completed)`
  - [x] `create(listId, request)`
  - [x] `partialUpdate(id, request)`
  - [x] `toggleCompleted(id)` — `completed` flip + `completedAt` 갱신
  - [x] `delete(id)`
- [x] `reminder/ReminderController.kt`:
  - [x] `GET /api/v1/lists/{listId}/reminders?completed=false`
  - [x] `POST /api/v1/lists/{listId}/reminders`
  - [x] `PATCH /api/v1/reminders/{id}`
  - [x] `POST /api/v1/reminders/{id}/toggle`
  - [x] `DELETE /api/v1/reminders/{id}`
- [x] 리스트 삭제 시 reminders cascade 동작 확인

### Backend — 테스트
- [x] `ReminderServiceTest`: 토글 동작, 부분 업데이트 null 무시, cascade
- [x] `ReminderControllerTest`: validation 실패 400, 정상 200/201

### Frontend
- [ ] `src/lib/queries/reminders.ts`: `useReminders(listId, completed)`, `useCreateReminder`, `useUpdateReminder`, `useToggleReminder`, `useDeleteReminder`
- [ ] 라우트 `/lists/[id]/page.tsx`에서 reminders 표시
- [ ] `src/components/ReminderRow.tsx`: 체크박스 + 제목 + 클릭 시 인라인 편집(`contentEditable` 또는 input toggle)
- [ ] `src/components/NewReminderInput.tsx`: 상단 input, Enter 키로 추가
- [ ] 체크 토글에 낙관적 업데이트 적용
- [ ] 미완료/완료 섹션 분리 표시

### Exit Criteria 검증
- [ ] 리스트 클릭 → reminder 입력 → Enter 추가 → 체크 → 완료 섹션 이동까지 정상

---

## Phase 3 — 상세 정보 + 인라인 익스팬더

### Backend
- [x] `ReminderCreateRequest.title`: `@NotBlank @Size(max=500)`
- [x] `ReminderUpdateRequest.title`: `@Size(max=500)`
- [x] `notes`: `@Size(max=10_000)` (create/update 공통)
- [x] validation 실패 시 `GlobalExceptionHandler`가 fieldErrors 포함하는지 테스트

### Frontend
- [ ] `ReminderRow` hover 시 우측에 ⓘ 아이콘 노출
- [ ] `src/components/ReminderExpander.tsx` (framer-motion `motion.div` height auto)
- [ ] 익스팬더 폼 필드:
  - [ ] notes textarea
  - [ ] dueAt date+time picker (한국어 로케일)
  - [ ] priority segmented control (NONE/LOW/MEDIUM/HIGH)
  - [ ] flagged 토글
- [ ] react-hook-form + zod 스키마, 변경 시 debounce(500ms) 후 PATCH
- [ ] 행 메타: 마감일/우선순위 prefix(`!`/`!!`/`!!!`)/깃발 아이콘 표시
- [ ] 한국어 날짜 포맷 helper (`formatDueDate(dueAt): "내일, 오후 3:00"` 등)

### Exit Criteria 검증
- [ ] 모든 reminder 필드 UI에서 입력/수정 가능
- [ ] 한국어 날짜 포맷 정상 표시

---

## Phase 4 — Subtask + 검색 + 스마트 뷰

### Backend — Subtask
- [x] `ReminderService.create`/`update`에서 parent depth 검증 (parent의 parent != null이면 400)
- [x] subtask 응답 구조: 부모 reminder의 `children` 필드로 nested 또는 별도 endpoint (별도 엔드포인트 `GET /api/v1/reminders/{id}/children`)
- [x] `findByParentIdOrderBySortOrderAsc` 사용

### Backend — 검색
- [x] `reminder/ReminderSearchService.kt` (또는 service에 메서드 추가)
- [x] `GET /api/v1/search?q=...`: title/notes ILIKE (H2: `LOWER(title) LIKE LOWER(...)`)
- [x] 페이징 고려 (Pageable) 또는 limit 50 단순 처리

### Backend — 스마트 뷰
- [x] `reminder/ReminderViewController.kt`
  - [x] `GET /api/v1/views/today?tz=Asia/Seoul`
  - [x] `GET /api/v1/views/scheduled?tz=Asia/Seoul`
  - [x] `GET /api/v1/views/all`
  - [x] `GET /api/v1/views/flagged`
  - [x] `GET /api/v1/views/completed`
- [x] tz 파라미터로 LocalDate 경계 → Instant 변환 로직
- [x] 카운트 집계: `GET /api/v1/views/counts?tz=...` (사이드바 카드용)

### Backend — 테스트
- [x] today 경계값 테스트 (자정 직전 23:59, 직후 00:00)
- [x] subtask depth 거부 테스트
- [x] 검색 대소문자 무시 테스트

### Frontend — Subtask UI
- [ ] `ReminderRow`에 `Tab` → 들여쓰기 (parentId = 직전 형제 id), `Shift+Tab` → 내어쓰기
- [ ] 시각적 indent (좌측 padding)
- [ ] 부모 완료 시 자식 처리 정책 결정 (Apple은 자식만 영향) — 처음엔 독립 처리

### Frontend — 스마트 뷰
- [ ] 사이드바 상단 스마트 카드 5종 (1차는 단순 카드, Phase 5에서 그리드/디자인 강화)
- [ ] 라우트 `/views/[type]/page.tsx`
- [ ] scheduled 뷰: dueDate별 그룹핑 헤더

### Frontend — 검색
- [ ] 사이드바 상단 검색 input
- [ ] 라우트 `/search?q=...` 결과 페이지
- [ ] 입력 debounce 250ms

### Exit Criteria 검증
- [ ] 5개 스마트 뷰 + 검색 + subtask 동작
- [ ] 키보드만으로 새 할 일 → Tab → subtask → Enter → 같은 깊이 추가 가능

---

## Phase 5 — UI 충실도 1차

### 디자인 시스템
- [ ] `globals.css` CSS 변수에 Apple 시스템 컬러 13종 정의 (라이트/다크 각각)
- [ ] 타이포 스택: `-apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", sans-serif`
- [ ] 토큰: `--radius-row: 6px`, `--radius-card: 10px`, `--shadow-sm/md`, `--bg-sidebar`, `--bg-content`
- [ ] 다크 모드: `prefers-color-scheme` + 수동 토글 컴포넌트

### 사이드바 — Apple 그리드 카드
- [ ] `src/components/SmartListGrid.tsx`: 2x3 grid (오늘/예정/전체/깃발/완료, 6번째 칸은 placeholder 또는 생략)
- [ ] 각 카드: 좌상단 큰 카운트(28~32px), 우하단 SF 톤 아이콘(lucide-react), background는 카드별 컬러
- [ ] vibrancy: 사이드바에 `backdrop-filter: blur(40px)` + 반투명 배경
- [ ] 사이드바 너비 resize: 우측 경계 드래그 핸들
- [ ] "내 목록" 헤더 + 사용자 리스트 행 (리스트 색 점 + 이름 + 우측 카운트)
- [ ] 하단 "리스트 추가" 버튼 (Apple 위치)

### 컨텐츠 영역
- [ ] 상단 큰 타이틀 (28~32px Semibold, 리스트 색상으로 컬러링)
- [ ] `ReminderRow` 디자인 리뉴얼: 24px 원형 체크박스 (체크 시 리스트 색으로 채워짐)
- [ ] 우선순위 prefix `!`/`!!`/`!!!` 빨간색
- [ ] 깃발 아이콘 우측 (주황 `#FF9500`)
- [ ] 마감일 작은 회색 텍스트로 제목 아래

### 인터랙션 / 애니메이션
- [ ] 체크 토글 spring 애니메이션 (체크 마크 stroke 그리기)
- [ ] 체크 후 0.8s 뒤 fade+slide → 완료 섹션 (framer-motion `AnimatePresence`)
- [ ] 익스팬더 spring 펼침/접힘
- [ ] 행 hover 시 옅은 회색 배경, 선택 시 리스트 색 틴트
- [ ] 빈 상태 컴포넌트 (큰 회색 아이콘 + "미리 알림 없음")

### Exit Criteria 검증
- [ ] macOS Reminders 스크린샷과 사이드바/카드/행을 나란히 비교하여 톤 일치
- [ ] 체크 애니메이션 spring 곡선 자연스러움

---

## Phase 6 — 키보드 / 드래그 / 컨텍스트 메뉴

### 키보드 단축키
- [ ] `react-hotkeys-hook` 도입
- [ ] `src/hooks/useAppShortcuts.ts` 통합 훅
- [ ] `⌘N` 새 미리 알림 (현재 리스트에 입력 포커스)
- [ ] `⌘⇧N` 새 목록 다이얼로그
- [ ] `Enter` 같은 깊이 새 할 일 (편집 컨텍스트 내)
- [ ] `Tab` / `Shift+Tab` indent / outdent
- [ ] `⌫` 빈 행 삭제
- [ ] `⌘F` 검색 input 포커스
- [ ] `⌘1`~`⌘5` 스마트 뷰 전환
- [ ] `Esc` 편집 취소
- [ ] `⌘.` 선택한 reminder 완료 토글
- [ ] 입력/모달 컨텍스트 분기로 중복/오작동 방지

### 드래그 정렬
- [ ] `@dnd-kit/core` 도입
- [ ] reminder 행 reorder (같은 리스트 내)
- [ ] reminder 다른 리스트로 드래그 → PATCH `listId`
- [ ] 사이드바 사용자 리스트 reorder
- [ ] Backend batch reorder 엔드포인트:
  - [ ] `POST /api/v1/lists/reorder` body: `[{id, sortOrder}, ...]`
  - [ ] `POST /api/v1/lists/{listId}/reminders/reorder` 동일 패턴
- [ ] 낙관적 업데이트 + 롤백

### 컨텍스트 메뉴
- [ ] Radix `ContextMenu`로 행 우클릭 메뉴: 깃발 토글 / 정보 / 삭제
- [ ] 사이드바 리스트 우클릭 메뉴: 이름 변경 / 삭제

### Exit Criteria 검증
- [ ] 키보드만으로 핵심 흐름 가능
- [ ] 드래그로 리스트 간 reminder 이동 가능

---

## Phase 7 — 다듬기 / 영속성 / 배포 (선택)

### 영속성
- [ ] `application.yml` 옵션 추가: `jdbc:h2:file:./data/reminderdb` (또는 Postgres 프로파일)
- [ ] Flyway 의존성 추가, `db/migration/V1__init.sql` 작성
- [ ] seed 데이터 SQL 또는 `CommandLineRunner`

### 다듬기
- [ ] skeleton 로딩 컴포넌트
- [ ] 에러 토스트 (sonner)
- [ ] 접근성: 포커스 링, ARIA label, 키보드 네비게이션 점검
- [ ] Lighthouse 측정 + 개선
- [ ] Playwright E2E 시나리오 1~2개 (리스트 생성→reminder 추가→완료)

### 배포 (옵션)
- [ ] Backend `Dockerfile` (eclipse-temurin:25-jre + jar 복사)
- [ ] Frontend `Dockerfile` (Next.js standalone output)
- [ ] `docker-compose.yml` (backend + frontend + postgres)
- [ ] README에 실행 방법 정리

### Exit Criteria 검증
- [ ] 재시작 후 데이터 유지 (영속성 옵션 적용 시)
- [ ] 데모 가능한 완성도

---

## 결정 / 확인 필요 (Phase 0 시작 전)

- [ ] 인증 — 단일 사용자 vs JWT
- [ ] 반복(Repeat) 일정 / 태그 / 섹션 — 1차 포함 여부
- [ ] 데이터 영속성 시점 — Phase 0 vs Phase 7
- [ ] shadcn/ui 사용 여부
- [ ] SF Pro 폰트 처리 — 시스템 스택 vs Inter 등
- [ ] 배포 범위 — 로컬 only vs Docker compose
