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

- [ ] **R4. 단건 reminder 삭제 시 자식 cascade 가 영속성 컨텍스트와 비동기**
  - 위치: `src/main/kotlin/com/bong/reminder/reminder/application/service/DefaultReminderCommandService.kt:74-79`
  - 증상: `repository.deleteById(parentId)` 가 DB FK `ON DELETE CASCADE` 로 자식 row 는 지우지만 영속성 컨텍스트의 자식 매니지드 인스턴스는 stale.
  - 고치기: 자식부터 명시 삭제 → 부모 삭제, 또는 bulk delete + clear/flush 로 통일.

- [ ] **R5. `Reminder.completed` / `ReminderList.color` getter 가 final → Hibernate LAZY 프록시 비활성**
  - 위치: `src/main/kotlin/com/bong/reminder/reminder/domain/Reminder.kt`, `list/domain/ReminderList.kt` (도메인 컨벤션 `final var x: T = ...`)
  - 증상: bootRun 로그에 `Getter methods of lazy classes cannot be final` 경고. 향후 `entity.list.name` 식 접근 시 N+1 즉시 발생.
  - 고치기: 도메인 프로퍼티의 `final` 키워드 제거(allOpen 플러그인이 처리), 또는 Hibernate enhance gradle 플러그인 도입.

---

## 🟡 Design / Maintainability

- [ ] **M1. PATCH 의 `null = 무변경` 시맨틱 — 명시적 clear 경로 부재**
  - 위치: `reminder/application/command/UpdateReminderCommand.kt`, `adapter/in/web/dto/ReminderUpdateRequest.kt`
  - 증상: `dueAt`, `notes`, `parentId` 를 비울 방법 없음. FE Shift+Tab outdent 가 비활성된 근본 원인.
  - 고치기: JSON Merge Patch (RFC 7386) 도입, `JsonNullable<T>` wrapper, 또는 `dueAtClear: boolean` 류 별도 플래그.

- [ ] **M2. `BaseEntity.createdAt/updatedAt` 의 `Instant.EPOCH` 디폴트가 버그를 가린다**
  - 위치: `src/main/kotlin/com/bong/reminder/common/BaseEntity.kt:25,30`
  - 고치기: `lateinit var createdAt: Instant` + DB NOT NULL, 또는 nullable 로 두고 service-side check.

- [ ] **M3. List cascade 가 코드 명시 + DB `@OnDelete` 두 경로로 중복**
  - 위치: `list/application/service/DefaultReminderListCommandService.kt:43-48`, `reminder/domain/Reminder.kt` 의 `parent` 만 `@OnDelete` 적용
  - 고치기: 정책 단일화 — DB FK 로 일관할 거면 `Reminder.list` 에도 `@OnDelete(CASCADE)` + 코드 제거, 코드로 명시할 거면 `@OnDelete` 떼기.

- [ ] **M4. `Reminder.changeParent` 의 자기참조 검증이 `id == null` 일 때 우회됨**
  - 위치: `src/main/kotlin/com/bong/reminder/reminder/domain/Reminder.kt:128`
  - 고치기: `require(newParent !== this)` (참조 동등) 로 변경.

- [ ] **M5. `ReminderRow.previousSiblingId` 계산이 부모-자식 섞인 목록에서 indent 불가**
  - 위치: `frontend/src/app/(app)/lists/[id]/page.tsx:43-49`
  - 고치기: `previousSiblingId = idx > 0 ? (incomplete[idx-1].parentId ?? incomplete[idx-1].id) : null`.

- [ ] **M6. 검색 정렬 결과 비결정적 — `updatedAt` 만으로 페이징**
  - 위치: `src/main/kotlin/com/bong/reminder/reminder/adapter/out/persistence/ReminderJpaRepository.kt:14-22`
  - 고치기: 보조 정렬키 `, r.id desc` 추가.

- [ ] **M7. `HealthController` 가 `@RestControllerAdvice` 와 같은 advice 컨텍스트**
  - 위치: `src/main/kotlin/com/bong/reminder/common/HealthController.kt`
  - 고치기: Spring Boot Actuator 도입 → `/actuator/health` 사용, 도메인 advice 분리.

- [ ] **M8. CORS `allowCredentials=true` + 단일 origin 조합이 인증 도입 시 깨짐**
  - 위치: `src/main/kotlin/com/bong/reminder/config/WebConfig.kt:15-19`
  - 고치기: 인증 도입 시점에 `allowedOriginPatterns` 환경 분리 + 쿠키 SameSite 정책 명시.

- [ ] **M9. `tasks.md` 가 BE/FE 한 파일에서 관리 — 모노레포 확장 시 소유권 분리 약화**
  - 고치기: `frontend/docs/tasks.md` 분리 또는 항목 prefix `[BE]`/`[FE]`.

---

## 🟢 Improvements

- [ ] **N1. `api.ts` 요청 timeout 부재**
  - 위치: `frontend/src/lib/api.ts:14`
  - 고치기: `AbortController` + 10s setTimeout.

- [ ] **N2. 검색 입력 길이 제한 부재**
  - 위치: `src/main/kotlin/com/bong/reminder/reminder/adapter/in/web/ReminderSearchController.kt:16`, `application/service/DefaultReminderSearchService.kt`
  - 고치기: `@Size(max=200)` 또는 service 내 검증.

- [ ] **N3. 스마트 뷰 페이징 부재**
  - 위치: `application/service/DefaultReminderViewQueryService.kt`
  - 고치기: `Pageable` 도입 자리 잡기 (1차에서는 1000건 가정 OK).

- [ ] **N4. `completed` 단독 + `completedAt` 인덱스 부재**
  - 위치: `reminder/domain/Reminder.kt:18-22`
  - 고치기: `@Index(columnList = "completed")`, `@Index(columnList = "completed,completed_at")` 추가.

- [ ] **N5. Frontend 입력 폼 클라이언트 검증 없음 (zod 미적용)**
  - 위치: `NewListDialog`, `NewReminderInput`, `ReminderExpander`
  - 고치기: zod + react-hook-form (Phase 3 계획 그대로 적용).

- [ ] **N6. `reminderKeys.byList` 가 boolean 을 키 포함 — 두 캐시 동기화 부담**
  - 위치: `frontend/src/lib/queries/reminders.ts:8-10`
  - 고치기: 단일 캐시 + 클라이언트 필터링.

- [ ] **N7. E2E 테스트가 추가 컨텍스트 부팅 → 빌드 시간 폭증 위험**
  - 위치: `src/test/kotlin/com/bong/reminder/e2e/ReminderE2ETest.kt`
  - 고치기: shared baseline 컨텍스트 + 필요한 시나리오만 RANDOM_PORT 분리.

- [ ] **N8. Tab indent 시 input 포커스 처리 / 다음 행 이동 UX 미정의**
  - 위치: `frontend/src/components/ReminderRow.tsx:90-96`

- [ ] **N9. `useUpdateList` 가 응답을 캐시에 직접 안 넣고 invalidate 만**
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
