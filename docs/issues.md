# 코드 리뷰 이슈 목록

본 세션에서 진행한 전체 코드 리뷰의 결과 항목을 체크리스트 형태로 정리.
각 항목은 **파일 경로 + 라인** 과 **한 줄 fix 방향** 을 포함한다.

범례:
- 🔴 **Bugs / Risks** — 실 동작 버그·운영 영향
- 🟡 **Design / Maintainability** — 즉시 터지진 않지만 누적 부채
- 🟢 **Improvements** — 여유 될 때
- ✅ **유지** — 회귀시키지 말 것

---

## 🔴 Bugs / Risks (즉시 수정 권장)

- [x] **R1. 잘못된 timezone 파라미터 → 500**
  - 위치: `src/main/kotlin/com/bong/reminder/reminder/adapter/in/web/ReminderViewController.kt:19,23,39`
  - 증상: `?tz=Invalid/Zone` 시 `ZoneRulesException` 미매핑 → Spring 디폴트 500.
  - 고치기: `GlobalExceptionHandler` 에 `ZoneRulesException`/`DateTimeException` → 400 + `INVALID_TIMEZONE` 매핑 추가.

- [x] **R2. `ReminderExpander` 가 마운트 즉시 변경 없는 PATCH 1회 송신 + reminder prop 변경 시 로컬 state stale**
  - 위치: `frontend/src/components/ReminderExpander.tsx:24-40`
  - 증상: ⓘ 만 펼쳐도 500ms 후 PATCH, 부모가 `invalidateQueries` 후 새 데이터 받아도 로컬 state 가 옛 값으로 덮어씀.
  - 고치기: (a) `isDirty` 플래그로 첫 렌더 PATCH 차단, (b) `useEffect(... [reminder.id, reminder.updatedAt])` 로 reminder 동기화.

- [x] **R3. `scheduled` 뷰 그룹핑이 UTC 날짜 기준 → KST 자정 직후 항목이 어제 그룹에 잡힘**
  - 위치: `frontend/src/app/(app)/views/[type]/page.tsx:61`
  - 고치기: `Intl.DateTimeFormat('sv-SE', { timeZone: 'Asia/Seoul' }).format(d)` 또는 `toLocaleDateString('en-CA')`.

- [x] **R4. 단건 reminder 삭제 시 자식 cascade 가 영속성 컨텍스트와 비동기**
  - 위치: `src/main/kotlin/com/bong/reminder/reminder/application/service/DefaultReminderCommandService.kt:74-79`
  - 증상: `repository.deleteById(parentId)` 가 DB FK `ON DELETE CASCADE` 로 자식 row 는 지우지만 영속성 컨텍스트의 자식 매니지드 인스턴스는 stale.
  - 고치기: 자식부터 명시 삭제 → 부모 삭제, 또는 bulk delete + clear/flush 로 통일.

- [x] **R5. `Reminder.completed` / `ReminderList.color` getter 가 final → Hibernate LAZY 프록시 비활성**
  - 위치: `src/main/kotlin/com/bong/reminder/reminder/domain/Reminder.kt`, `list/domain/ReminderList.kt` (도메인 컨벤션 `final var x: T = ...`)
  - 증상: bootRun 로그에 `Getter methods of lazy classes cannot be final` 경고. 향후 `entity.list.name` 식 접근 시 N+1 즉시 발생.
  - 고치기: 도메인 프로퍼티의 `final` 키워드 제거(allOpen 플러그인이 처리), 또는 Hibernate enhance gradle 플러그인 도입.

---

## 🟡 Design / Maintainability

- [x] **M1. PATCH 의 `null = 무변경` 시맨틱 — 명시적 clear 경로 부재**
  - 위치: `reminder/application/command/UpdateReminderCommand.kt`, `adapter/in/web/dto/ReminderUpdateRequest.kt`
  - 증상: `dueAt`, `notes`, `parentId` 를 비울 방법 없음. FE Shift+Tab outdent 가 비활성된 근본 원인.
  - 고치기: JSON Merge Patch (RFC 7386) 도입, `JsonNullable<T>` wrapper, 또는 `dueAtClear: boolean` 류 별도 플래그.

- [x] **M2. `BaseEntity.createdAt/updatedAt` 의 `Instant.EPOCH` 디폴트가 버그를 가린다**
  - 위치: `src/main/kotlin/com/bong/reminder/common/BaseEntity.kt:25,30`
  - 고치기: `lateinit var createdAt: Instant` + DB NOT NULL, 또는 nullable 로 두고 service-side check.

- [x] **M3. List cascade 가 코드 명시 + DB `@OnDelete` 두 경로로 중복**
  - 위치: `list/application/service/DefaultReminderListCommandService.kt:43-48`, `reminder/domain/Reminder.kt` 의 `parent` 만 `@OnDelete` 적용
  - 고치기: 정책 단일화 — DB FK 로 일관할 거면 `Reminder.list` 에도 `@OnDelete(CASCADE)` + 코드 제거, 코드로 명시할 거면 `@OnDelete` 떼기.

- [x] **M4. `Reminder.changeParent` 의 자기참조 검증이 `id == null` 일 때 우회됨**
  - 위치: `src/main/kotlin/com/bong/reminder/reminder/domain/Reminder.kt:128`
  - 고치기: `require(newParent !== this)` (참조 동등) 로 변경.

- [x] **M5. `ReminderRow.previousSiblingId` 계산이 부모-자식 섞인 목록에서 indent 불가**
  - 위치: `frontend/src/app/(app)/lists/[id]/page.tsx:43-49`
  - 고치기: `previousSiblingId = idx > 0 ? (incomplete[idx-1].parentId ?? incomplete[idx-1].id) : null`.

- [x] **M6. 검색 정렬 결과 비결정적 — `updatedAt` 만으로 페이징**
  - 위치: `src/main/kotlin/com/bong/reminder/reminder/adapter/out/persistence/ReminderJpaRepository.kt:14-22`
  - 고치기: 보조 정렬키 `, r.id desc` 추가.

- [x] **M7. `HealthController` 가 `@RestControllerAdvice` 와 같은 advice 컨텍스트**
  - 위치: `src/main/kotlin/com/bong/reminder/common/HealthController.kt`
  - 고치기: Spring Boot Actuator 도입 → `/actuator/health` 사용, 도메인 advice 분리.

- [x] **M8. CORS `allowCredentials=true` + 단일 origin 조합이 인증 도입 시 깨짐**
  - 위치: `src/main/kotlin/com/bong/reminder/config/WebConfig.kt:15-19`
  - 고치기: 인증 도입 시점에 `allowedOriginPatterns` 환경 분리 + 쿠키 SameSite 정책 명시.

- [x] **M9. `tasks.md` 가 BE/FE 한 파일에서 관리 — 모노레포 확장 시 소유권 분리 약화**
  - 고치기: `frontend/docs/tasks.md` 분리 또는 항목 prefix `[BE]`/`[FE]`.

---

## 🟢 Improvements

- [x] **N1. `api.ts` 요청 timeout 부재**
  - 위치: `frontend/src/lib/api.ts:14`
  - 고치기: `AbortController` + 10s setTimeout.

- [x] **N2. 검색 입력 길이 제한 부재**
  - 위치: `src/main/kotlin/com/bong/reminder/reminder/adapter/in/web/ReminderSearchController.kt:16`, `application/service/DefaultReminderSearchService.kt`
  - 고치기: `@Size(max=200)` 또는 service 내 검증.

- [x] **N3. 스마트 뷰 페이징 부재** (today 뷰만 적용 — 나머지 뷰는 동일 패턴 후속 작업)
  - 위치: `application/service/DefaultReminderViewQueryService.kt`
  - 고치기: `Pageable` 도입 자리 잡기 (1차에서는 1000건 가정 OK).

- [x] **N4. `completed` 단독 + `completedAt` 인덱스 부재**
  - 위치: `reminder/domain/Reminder.kt:18-22`
  - 고치기: `@Index(columnList = "completed")`, `@Index(columnList = "completed,completed_at")` 추가.

- [ ] **N5. Frontend 입력 폼 클라이언트 검증 없음 (zod 미적용)**
  - 위치: `NewListDialog`, `NewReminderInput`, `ReminderExpander`
  - 고치기: zod + react-hook-form (Phase 3 계획 그대로 적용).

- [x] **N6. `reminderKeys.byList` 가 boolean 을 키 포함 — 두 캐시 동기화 부담**
  - 위치: `frontend/src/lib/queries/reminders.ts:8-10`
  - 고치기: 단일 캐시 + 클라이언트 필터링.

- [ ] **N7. E2E 테스트가 추가 컨텍스트 부팅 → 빌드 시간 폭증 위험**
  - 위치: `src/test/kotlin/com/bong/reminder/e2e/ReminderE2ETest.kt`
  - 고치기: shared baseline 컨텍스트 + 필요한 시나리오만 RANDOM_PORT 분리.

- [ ] **N8. Tab indent 시 input 포커스 처리 / 다음 행 이동 UX 미정의**
  - 위치: `frontend/src/components/ReminderRow.tsx:90-96`

- [x] **N9. `useUpdateList` 가 응답을 캐시에 직접 안 넣고 invalidate 만**
  - 위치: `frontend/src/lib/queries/lists.ts:46-51`
  - 고치기: `setQueryData(listKeys.all, (old) => old?.map(...))` 로 round-trip 절약.

---

## ✅ 유지 (회귀 금지)

- 헥사고날 + CQRS-lite 경계 — 입력 포트 우회 없음, 도메인이 어댑터를 모름.
- Clock 빈 일관 주입 — `Instant.now(clock)` + `@Primary FixedClock` 으로 today 경계값 검증.
- 트랜잭션 경계 — 서비스 `@Transactional`, 쿼리 `readOnly=true`, 컨트롤러 통합 테스트 롤백, E2E 는 명시적 cleanup.
- `@Modifying(clearAutomatically=true, flushAutomatically=true)` 가 bulk delete 에 정확히 적용.
- 테스트 피라미드 — 88건 / 7~10s, 도메인 단위 → 서비스 통합 → 컨트롤러 → E2E.

---

## 권장 우선순위 (1주 안)

1. **R1** ZoneId 예외 매핑 — 5분
2. **R2** ReminderExpander 첫 PATCH + state 동기화 — 30분
3. **R3** scheduled 그룹핑 timezone — 5분
4. **R5** 도메인 `final` 제거 + LAZY 정상화 — 30분 + 회귀 테스트
5. **M1** PATCH 의 명시적 clear 의도 결정 — 설계 토론 후 적용
6. **M3** cascade 정책 단일화 — 30분

---

## 2026-05-02 코드 품질 리포트 후속 이슈

### 🔴 Bugs / Risks

- [x] **R6. Reminder mutation 시 smart view / search 캐시 미무효화**
  - 위치: `frontend/src/lib/queries/reminders.ts:42-46, 68-71, 100-103, 111-114`
  - 증상: 토글/생성/수정/삭제 직후 사이드바 스마트 카드 카운트, `/views/*` 페이지, 검색 결과가 stale.
  - 고치기: 각 mutation `onSettled`/`onSuccess` 에서 `viewKeys.counts(tz)`, `viewKeys.list(type, tz)`, `["search"]` 까지 invalidate.

- [x] **R7. `Reminder.completedAt` 불변식이 도메인에서 강제되지 않음**
  - 위치: `src/main/kotlin/com/bong/reminder/reminder/domain/Reminder.kt:91-93`
  - 증상: `completed=false` 인데 `completedAt` 이 남아 있는 상태가 setter 우회로 가능.
  - 고치기: `init` 또는 `@PrePersist/@PreUpdate` 에 `assertCompletionConsistent()` 추가.

- [x] **R8. `Reminder.init` 에서 `validateNotes` 미호출 → 도메인이 notes 길이 검증을 DTO 에만 의존**
  - 위치: `src/main/kotlin/com/bong/reminder/reminder/domain/Reminder.kt:30, 49-52`
  - 고치기: `init` 블록에 `validateNotes(notes)` 한 줄 추가.

### 🟡 Design / Maintainability

- [ ] **M10. `useReminders` 가 listId 당 incomplete/completed 두 번 fetch 후 클라에서 합침**
  - 위치: `frontend/src/lib/queries/reminders.ts:13-23`
  - 증상: 페이지 진입마다 fetch 2회 + concat 시 정렬 손실.
  - 고치기: 백엔드 `GET /lists/{id}/reminders` 가 `completed` 미지정 시 전체 반환하도록 합의 → 단일 fetch + 클라 분리.

- [ ] **M11. `frontend/eslint.config.*` 부재 + lint/typecheck/format 스크립트 부재**
  - 위치: `frontend/package.json:5-11`, `frontend/` 디렉토리 전반
  - 증상: PR 게이트 없음. 의미 없는 `eslint-disable-next-line` 주석이 그대로 남음 (`ReminderExpander.tsx:45`, `SearchBar.tsx:37`).
  - 고치기: `eslint.config.mjs` (Next 16 official preset) + `.prettierrc` + `"lint"`, `"typecheck": "tsc --noEmit"`, `"format"` 스크립트 추가.

- [ ] **M12. 네이티브 `confirm()` 사용 → spec §7 "Apple 스타일 confirm dialog" 불일치 + 접근성 약함**
  - 위치: `frontend/src/components/Sidebar.tsx:74`
  - 고치기: 공용 `ConfirmDialog` 컴포넌트 추출 (`role="dialog"`, `aria-modal="true"`, 포커스 트랩, Esc 닫기). reminder 삭제 등 후속 케이스도 흡수.

- [ ] **M13. `ReminderRow` 단일 컴포넌트 비대 (177L)**
  - 위치: `frontend/src/components/ReminderRow.tsx`
  - 증상: 체크박스 / 제목 인라인 편집 / Tab indent / 익스팬더 토글 / 삭제 / 메타 표시가 한 컴포넌트에.
  - 고치기: `useReminderRowKeys(reminder, previousSiblingId)` 훅, `<RowCheckbox/>`, `<RowMeta/>` 컴포넌트 추출.

- [x] **M14. `application.yml` 의 `show-sql` / `h2.console.enabled` 이 모든 환경에 기본 true**
  - 위치: `src/main/resources/application.yml:22-33`
  - 증상: 테스트 빌드 로그가 SQL 로 도배되어 실패 메시지가 묻힘. 운영 배포 시 H2 콘솔 노출 위험.
  - 고치기: 두 옵션을 `dev` 프로파일 블록으로 이동, `application-test.yml` 신설.

- [ ] **M15. 클라이언트 폼 검증이 ad-hoc — react-hook-form + zod 미도입**
  - 위치: `frontend/src/components/{NewListDialog,NewReminderInput,ReminderExpander}.tsx`
  - 증상: `useState` + 수동 trim 만, 백엔드 `fieldErrors` 가 `ApiError.body.fieldErrors` 까지 정의되어 있는데 어디에서도 소비되지 않음.
  - 고치기: react-hook-form + zod 도입, 서버 fieldErrors 를 form error 로 매핑 (plan Phase 3 약속 이행).

- [ ] **M16. hover-only 액션이 키보드 포커스에 노출되지 않음**
  - 위치: `frontend/src/components/Sidebar.tsx:114, 121`
  - 증상: `invisible group-hover:visible` 만 있어 키보드 사용자에게 이름 변경 / 삭제 진입점이 사라짐.
  - 고치기: `group-focus-within:visible` 추가.

- [ ] **M17. 라이트 모드 메타 텍스트 색상 대비 부족 (WCAG AA 미달)**
  - 위치: `frontend/src/app/globals.css` (`--muted: #8E8E93` on `--sidebar-bg: #f5f5f7` ≈ 3.2:1)
  - 고치기: `--muted` 라이트 토큰을 `#6B6B70` 류로 어둡게 조정 또는 텍스트 사이즈 조건부 분리.

- [ ] **M18. 거의 모든 page 가 `"use client"` — RSC 미활용**
  - 위치: `frontend/src/app/(app)/{page,lists/[id]/page,views/[type]/page,search/page}.tsx`
  - 고치기: layout 에서 `prefetchQuery` + hydration boundary 도입, 인터랙션이 필요한 부분만 client island 로 분리.

### 🟢 Improvements

- [ ] **N10. 컴포넌트/훅 테스트 토대 부재**
  - 위치: `frontend/package.json` (RTL/MSW 미설치), 컴포넌트 테스트 0건
  - 고치기: `@testing-library/react` + `@testing-library/user-event` + `msw` 설치 후 ① `ReminderRow` Tab indent → `parentId` PATCH 호출, ② `useToggleReminder` 낙관 업데이트 + 에러 롤백, ③ `SearchBar` debounce 250ms 후 `router.push` 3개 테스트 작성.

- [ ] **N11. `formatDueDate` 단위 테스트 부재**
  - 위치: `frontend/src/lib/dueDate.ts` (테스트 파일 없음)
  - 고치기: "오늘 / 내일 / 어제 / 미래 / 과거" 분기 + KST 자정 경계 테스트 추가.

- [ ] **N12. `useSmartView` default 인자가 매 렌더 `Intl.DateTimeFormat()` 호출**
  - 위치: `frontend/src/lib/queries/views.ts:23`
  - 고치기: 모듈 스코프 `const DEFAULT_TZ = ...` 또는 `useMemo` 로 1회 계산.

- [ ] **N13. `staleTime: 5_000` 의 일률 적용 — 검색/lists/views 같은 정책 공유**
  - 위치: `frontend/src/app/providers.tsx:12-15`
  - 고치기: query 별 `staleTime` 차등 (lists 60s, views 10s, search 0).

- [ ] **N14. `next/font` 미사용 — SF Pro fallback chain 만 의존**
  - 위치: `frontend/src/app/globals.css:53-56`, `frontend/next.config.ts`
  - 고치기: 시스템 폰트 유지 정책이면 의도 주석 명시. 아니면 `next/font/local` 또는 Inter 도입 검토 (spec §7 fallback 정책에 맞춰).

- [ ] **N15. `next.config.ts` 가 빈 객체 — 정적 최적화 누락**
  - 위치: `frontend/next.config.ts:3-5`
  - 고치기: `experimental.optimizePackageImports: ["lucide-react"]` 등 lucide 트리쉐이킹 추가.

- [x] **N16. 출력 포트 `ReminderRepositoryPort` 비대 (13개 메서드)**
  - 위치: `src/main/kotlin/com/bong/reminder/reminder/application/port/out/ReminderRepositoryPort.kt:5-27`
  - 고치기: 카운트/검색용 read 메서드를 `ReminderQueryReadModel` 등 별도 포트로 분리 — CQRS-lite 의도 강화.

- [x] **N17. `DefaultReminderSearchService` 가 빈 query 일 때 emptyList 반환 — 정책이 service 에 위치**
  - 위치: `src/main/kotlin/com/bong/reminder/reminder/application/service/DefaultReminderSearchService.kt:21-22`
  - 고치기: 컨트롤러 `@RequestParam @NotBlank q` 로 400 응답으로 단일화.

- [ ] **N18. 인라인 스타일과 Tailwind arbitrary value 혼재**
  - 위치: `frontend/src/components/ReminderRow.tsx:68-71, 134`, `frontend/src/components/SmartListGrid.tsx:42`, `frontend/src/app/(app)/page.tsx:20`
  - 고치기: 정적 토큰은 `@theme inline` 에 노출 후 Tailwind 클래스로, 동적 색상(리스트 컬러)만 인라인 유지.

- [ ] **N19. Apple 13색 + 다크 모드 변형 토큰 부재**
  - 위치: `frontend/src/app/globals.css:30-38`
  - 증상: `prefers-color-scheme: dark` 한 블록만 있고 Apple 컬러 13종은 다크 변형 없음.
  - 고치기: 다크 모드에서도 가독성 검증된 13색 변형 정의 + 수동 토글 컴포넌트 (spec §7).

- [ ] **N20. `ReminderE2ETest` 가 별도 컨텍스트로 부팅**
  - 위치: `src/test/kotlin/com/bong/reminder/e2e/ReminderE2ETest.kt:22-28`
  - 고치기: 공유 `@TestConfiguration` baseline + `@DirtiesContext(BEFORE_CLASS)` 로 격리, 또는 testcontainers 도입.
