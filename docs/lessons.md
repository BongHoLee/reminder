# Reminders 클론 프로젝트 — 학습 정리

본 문서는 Spring Boot 4 + Kotlin 2.3 + JPA 백엔드와 Next.js 16 + TanStack Query 프론트엔드로
Apple Reminders 클론을 만드는 과정에서 부딪힌 **결정·실수·교정**을 그대로 정리한 자료다.
"무엇을 했다" 보다 **"왜 그렇게 했고, 어떤 함정을 피했는가"** 에 초점을 맞춘다.

목차
1. [헥사고날 + CQRS-lite 구조 결정](#1-헥사고날--cqrs-lite-구조-결정)
2. [Kotlin + JPA 엔티티 컨벤션과 함정](#2-kotlin--jpa-엔티티-컨벤션과-함정)
3. [테스트 피라미드 — 같은 코드, 4개의 격리 수준](#3-테스트-피라미드--같은-코드-4개의-격리-수준)
4. [영속성 컨텍스트와 cascade — 두 번 데인 자리](#4-영속성-컨텍스트와-cascade--두-번-데인-자리)
5. [@ManyToOne, LAZY, EAGER, N+1](#5-manytoone-lazy-eager-n1)
6. [REST PATCH 의 시맨틱 설계](#6-rest-patch-의-시맨틱-설계)
7. [시간대 (Asia/Seoul ↔ UTC) 처리](#7-시간대-asiaseoul--utc-처리)
8. [프론트엔드 TanStack Query 패턴](#8-프론트엔드-tanstack-query-패턴)
9. [Exit Criteria 와 코드 리뷰의 가치](#9-exit-criteria-와-코드-리뷰의-가치)
10. [TDD 적용 사례 5건 — Red·Green·Refactor 의 실제 모습](#10-tdd-적용-사례-5건--redgreenrefactor-의-실제-모습)
11. [커밋·이슈 관리 워크플로](#11-커밋이슈-관리-워크플로)

---

## 1. 헥사고날 + CQRS-lite 구조 결정

### 1.1 어그리게이트당 디렉토리

```
<aggregate>/
├── domain/                  # JPA 엔티티(=도메인) + 도메인 예외 + 도메인 메서드
├── application/
│   ├── port/in/             # …CommandService, …QueryService 인터페이스 (입력 포트)
│   ├── port/out/            # …RepositoryPort (어그리게이트당 단일, 출력 포트)
│   ├── command/             # 쓰기 입력 객체 (Create/Update/Delete…Command)
│   ├── query/               # 읽기 출력 객체 (…View)
│   └── service/             # Default…CommandService, Default…QueryService
└── adapter/
    ├── in/web/              # 매퍼(Request↔Command, View→Response) + dto/
    │   └── dto/             # web Request / Response DTO (validation 어노테이션)
    └── out/persistence/     # …JpaRepository(Spring Data), …PersistenceAdapter(@Component, RepositoryPort 구현)
```

### 1.2 왜 CQRS-lite ?

같은 어그리게이트인데도 **쓰기 흐름과 읽기 흐름의 모델이 다르다**.
- 쓰기: Command → Service → Repository → Domain → Repository → View
- 읽기: Query Service → Repository → View

읽기 전용 트랜잭션(`@Transactional(readOnly = true)`) 분리, 입력 포트 분리(`*CommandService` / `*QueryService`)로
"쓰기 의도 vs 조회 의도" 의 도메인 의미를 코드 위에 그대로 새길 수 있다.

### 1.3 단방향 매핑 — `@OneToMany` 를 두지 않는다

`Reminder` 는 `@ManyToOne ReminderList` 를 갖지만, `ReminderList` 는 `@OneToMany List<Reminder>` 를 들고 있지 않다.
- 어그리게이트 경계가 명확해지고, list 객체 하나로 reminder 컬렉션이 묻혀 들어오는 상황을 차단.
- cascade 도 JPA 의 묵시적 cascade 가 아니라 **서비스 레이어의 명시적 호출**(`reminderRepository.deleteByListId`) 로 처리.

> 학습 포인트: 양방향이 "기본" 처럼 보이지만, 단방향이 단순한 경우가 더 많다.
> 양방향은 동기화 책임(addChild/removeChild 가 양쪽 컬렉션을 챙겨야 하는 부담) 을 끌고 들어온다.

---

## 2. Kotlin + JPA 엔티티 컨벤션과 함정

### 2.1 처음 정한 컨벤션 (실패)

```kotlin
@Entity
class Reminder(...) : BaseEntity() {
    final var title: String = title
        private set
}
```

- `final` 로 외부에서 override 불가
- `private set` 으로 외부 mutate 차단
- 캡슐화 좋고 immutable-ish 하게 보임

### 2.2 그런데 startup 로그가 경고

```
HibernateException: Getter methods of lazy classes cannot be final:
  com.bong.reminder.reminder.domain.Reminder#getCompleted
  com.bong.reminder.list.domain.ReminderList#getColor
```

**왜?** Hibernate 의 LAZY 프록시는 entity 를 **subclass 로 감싸 모든 getter 를 override** 한다.
final getter 는 override 불가능 → 프록시 생성 실패 → LAZY 의 본래 의도(필드 접근 시점에 SELECT) 가 깨진다.

### 2.3 교정한 컨벤션

```kotlin
var title: String = title
    protected set
```

- `final` 제거 → allOpen 플러그인이 자연스럽게 열어줌 → Hibernate 가 override 가능
- `private set` → `protected set` (Kotlin 은 open 프로퍼티의 private setter 를 금지)
- 외부 mutate 는 여전히 차단됨 (protected 는 같은 클래스 + 서브클래스 + 프록시만 접근)

### 2.4 함께 쓰이는 빌드 설정

```kotlin
// build.gradle.kts
plugins {
    kotlin("plugin.spring") version "..."
    kotlin("plugin.jpa") version "..."   // noArg + allOpen 자동 적용
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}
```

- `kotlin("plugin.jpa")` → Hibernate 가 요구하는 no-arg 생성자를 자동 합성 (`var` + 디폴트 값으로도 가능)
- `allOpen` → `@Entity` 클래스와 멤버를 `open` 처리, 프록시 작성 가능

### 2.5 BaseEntity — 공통 PK + auditing

```kotlin
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    @CreatedDate @Column(nullable = false, updatable = false)
    var createdAt: Instant = Instant.EPOCH
        protected set

    @LastModifiedDate @Column(nullable = false)
    var updatedAt: Instant = Instant.EPOCH
        protected set
}
```

- `@EnableJpaAuditing` 을 별도 `@Configuration` 에 두어 활성화
- **`Instant.EPOCH` 디폴트의 함정**: auditing 이 안 걸리는 케이스를 silently 통과시킨다 → 테스트는 `shouldNotBe Instant.EPOCH` 로 우회. 이상적으로는 `lateinit var` + DB NOT NULL.

---

## 3. 테스트 피라미드 — 같은 코드, 4개의 격리 수준

이 프로젝트는 같은 기능에 대해 **격리 수준이 다른 4종류의 테스트** 를 의도적으로 분리해 둔다.

| 종류 | 어노테이션 | 격리 | 속도 | 검증 영역 |
|---|---|---|---|---|
| 도메인 단위 | (없음 — 순수 Kotlin) | JVM 만 | 매우 빠름 | 불변/검증/도메인 메서드 |
| 서비스 통합 | `@SpringBootTest(webEnvironment=NONE)` + `@Transactional` | 트랜잭션 롤백 | 빠름 | 서비스 ↔ JPA, dirty checking, 영속성 |
| 컨트롤러 통합 | `@SpringBootTest` + `@AutoConfigureMockMvc` + `@Transactional` | 트랜잭션 롤백 | 빠름 | URL 매핑, validation, 예외 매핑, JSON |
| E2E | `@SpringBootTest(webEnvironment=RANDOM_PORT)` + JDK `HttpClient` | DB cleanup 필요 | 느림 | 실제 톰캣 + HTTP wire 검증 |

### 3.1 컨트롤러 슬라이스(`@WebMvcTest`) 에서 통합 테스트로 옮긴 이유

이 프로젝트는 처음에 컨트롤러 슬라이스 + `@MockkBean` 으로 시작했다가 통합 테스트로 옮겼다.
- 슬라이스는 빠르지만, 서비스 동작·트랜잭션·JPA 까지 한 번에 검증하지 못한다.
- "리스트 cascade 삭제" 같은 회귀는 슬라이스에선 잡을 수 없고 통합 테스트에서만 잡힌다.
- 약간 느려진 대가로 **회귀 안전망의 의미가 비교 불가능하게 커진다**.

### 3.2 Kotest + kotest-extensions-spring 의 트랜잭션 롤백

```kotlin
@SpringBootTest @AutoConfigureMockMvc @Transactional
class ReminderControllerTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val jpaRepository: ReminderJpaRepository,
) : DescribeSpec({
    describe("GET /api/v1/lists") {
        it("...") { /* 각 it 블록은 자체 트랜잭션 안에서 실행 후 롤백 */ }
    }
})
```

- `ProjectConfig` 에서 `SpringExtension` 을 등록해야 Kotest 와 Spring TestContext 가 연결된다.
- `@Transactional` 은 각 `it` 단위로 시작/롤백된다 (Spring TestExecutionListener 가 처리).
- DB 가 `jdbc:h2:mem:reminderdb` 로 같은 인스턴스라도 트랜잭션 롤백으로 격리된다.

### 3.3 E2E 의 함정 — 실 서버 트랜잭션은 따로

```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReminderE2ETest(...) : DescribeSpec({
    fun cleanDb() { ... deleteAllInBatch() ... }
    beforeEach { cleanDb() }
    afterSpec { cleanDb() }   // ← 실 서버는 commit 하므로, 후속 @Transactional spec 으로 누수 방지
})
```

- `RANDOM_PORT` 는 별도 Tomcat 스레드. 테스트 클래스의 `@Transactional` 이 거기엔 적용 안 됨 → 실제 DB 에 commit 됨.
- 따라서 E2E 가 끝난 뒤에도 데이터가 남아 후속 `@Transactional` 컨트롤러 테스트의 어설션을 깨트림 (실제로 한 번 깨졌다).
- 해결: `beforeEach`+`afterSpec` 명시 cleanup.

### 3.4 PATCH 를 위해 JDK HttpClient

`TestRestTemplate` 의 기본 `SimpleClientHttpRequestFactory` 는 PATCH 를 지원하지 않는다.
HttpComponents 의존성 추가 대신 JDK 24+ 의 `java.net.http.HttpClient` 를 직접 사용해 외부 의존성 0 으로 해결.

```kotlin
val httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build()
val req = HttpRequest.newBuilder(URI.create(url))
    .header("Content-Type", "application/json")
    .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
    .build()
val res = httpClient.send(req, HttpResponse.BodyHandlers.ofString())
```

---

## 4. 영속성 컨텍스트와 cascade — 두 번 데인 자리

### 4.1 첫 번째 사고 — bulk delete 와 영속성 컨텍스트의 비동기

```kotlin
@Modifying
@Query("delete from Reminder r where r.list.id = :listId")
fun deleteByListId(listId: Long)
```

이걸 `@Transactional` 같은 트랜잭션 안에서 호출 후 곧바로 `count()` 같은 쿼리를 호출했더니 `TransientPropertyValueException` 발생.

**원인**:
1. `reminderJpaRepository.save(reminder)` → 영속성 컨텍스트에 reminder 가 매니지드 상태로 등록 (실제 INSERT 는 flush 시점)
2. `deleteByListId(...)` 가 JPQL bulk DELETE 를 실행. 그러나 **영속성 컨텍스트의 매니지드 인스턴스는 모름**.
3. 후속 쿼리(`count()` 등) 가 autoflush 를 트리거 → 매니지드 reminder 들을 INSERT 하려는데 그들이 참조하는 list 가 이미 사라짐 → 예외.

**해결**:
```kotlin
@Modifying(clearAutomatically = true, flushAutomatically = true)
@Query("delete from Reminder r where r.list.id = :listId")
fun deleteByListId(listId: Long)
```
- `flushAutomatically=true`: 실행 직전 모든 pending 변경을 flush
- `clearAutomatically=true`: 실행 직후 영속성 컨텍스트를 비움 (stale managed entity 제거)

### 4.2 두 번째 사고 — 자기참조 FK + bulk delete

테스트는 통과하던 cascade 가 실 운영(브라우저로 reminder 부모-자식 만든 후 list 삭제) 에서 500. 백엔드 로그:
```
ConstraintViolation ... reminder.parent_id ...
```

**원인**: `reminder.parent_id → reminder.id` 자기참조 FK. JPQL `delete from Reminder where list_id = ?` 는 한 SQL 로 모든 행을 지우려 하지만, H2 가 row-by-row FK 검사 → 부모 row 가 먼저 지워지면 자식 row 의 FK 가 위반.

**해결**: 도메인 측에 `@OnDelete(CASCADE)` 추가.
```kotlin
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "parent_id")
@OnDelete(action = OnDeleteAction.CASCADE)
var parent: Reminder? = parent
    protected set
```

**교훈**: 단위 테스트는 부모-자식 reminder 케이스를 커버하지 않았다. cascade 시나리오는 **실제 데이터 위상**을 모방한 테스트가 있어야 한다 → 회귀 테스트 추가:
```kotlin
it("자기참조 부모-자식 reminder 가 섞여 있어도 cascade 가 성공한다") {
    val parent = save(Reminder(list, title = "장보기"))
    save(Reminder(list, title = "우유", parent = parent))
    save(Reminder(list, title = "빵", parent = parent))
    mockMvc.delete("/api/v1/lists/${list.id}").andExpect { status { isNoContent() } }
    reminderJpaRepository.count() shouldBe 0L
}
```

---

## 5. @ManyToOne, LAZY, EAGER, N+1

### 5.1 LAZY 가 N+1 을 안 만드는 조건

`Reminder.list` 가 `@ManyToOne(LAZY)` 일 때, `reminder.list.id` **만 읽으면** 추가 SELECT 가 안 나간다.
- LAZY 프록시는 **PK 만은 따로** 들고 있어서 id 접근만으로 초기화되지 않는다.
- 그래서 ReminderView.from(entity) 가 `entity.list.id` / `entity.parent?.id` 만 읽으면 N건의 reminder 를 가져와도 추가 SELECT 0회.

### 5.2 LAZY 를 쉽게 깨는 패턴

```kotlin
data class ReminderView(val listName: String, ...) {
    fun from(entity: Reminder) = ReminderView(
        listName = entity.list.name,   // ← 이 한 줄이 N+1 의 시작
        ...
    )
}
```

- name 같은 일반 필드는 PK 가 아니므로 프록시 초기화 → SELECT 1회 추가 → 100건이면 100회 추가.
- 방어책: View 에는 PK 만 노출, 이름은 별도 endpoint 에서 join fetch 또는 `@EntityGraph`.

### 5.3 EAGER 가 N+1 을 만드는 경로

EAGER 는 단건 `findById` 에서는 join 으로 한 방에 끝나지만, **JPQL/파생 쿼리**(`select r from Reminder r ...`) 에서는 다르다.
1. `select * from reminder where ...` (1회)
2. EAGER 필드 채우기 → reminder 마다 추가 select → N회
- "EAGER = JOIN" 이라는 직관은 단건에만 통한다.
- 다건 + EAGER 는 **항상 N+1**. fetch join 이나 `@EntityGraph` 명시가 있어야 한 방에 끝난다.

### 5.4 final getter 가 LAZY 를 망친다

Kotlin 의 `final var x: T = ...; private set` 패턴은 `final` 키워드를 명시한다.
- allOpen 플러그인이 클래스를 열어도 멤버에 `final` 이 명시돼 있으면 그대로 final.
- Hibernate 의 LAZY 프록시는 모든 getter 를 override 해야 동작 → final getter 1개라도 있으면 프록시 작성 실패 → fallback (보통 EAGER 처럼 동작).
- **표면**적으로는 코드가 잘 동작하는 것처럼 보이지만, **내부적으로는 LAZY 가 무력화** 된다.
- startup 경고를 무시하지 말 것.

### 5.5 검증 도구 — Hibernate Statistics

```kotlin
val stats: Statistics = sessionFactory.statistics
stats.clear()
stats.isStatisticsEnabled = true

val reminders = repo.findByListIdAndCompleted(listId, false)
reminders.forEach { it.list.id }   // PK 만 접근

stats.prepareStatementCount shouldBe 1L   // 추가 select 0회
```

`prepareStatementCount` 가 1보다 크면 LAZY 가 안 먹고 있다는 신호. 이런 회귀 테스트가 N+1 잠복을 막는다.

---

## 6. REST PATCH 의 시맨틱 설계

### 6.1 처음 선택 — `null = no change`

```kotlin
data class UpdateReminderCommand(
    val title: String? = null,
    val notes: String? = null,
    val dueAt: Instant? = null,
    ...
)
```

서비스에서 `command.title?.let(entity::rename)` 식으로 null 은 변경 없음.
- 단순하고 직관적.
- 그러나 **명시적으로 비우기 (clear)** 가 불가능. dueAt 을 null 로 set 하고 싶어도 "변경 없음" 으로 해석.

### 6.2 문제가 표면화 — Shift+Tab outdent 가 안 됨

프론트엔드의 키보드 흐름에서 자식 reminder 를 다시 최상위로 빼고 싶은데 (parentId 를 null 로) 백엔드가 처리 못함.
ReminderExpander 의 dueAt 을 비우려 input 을 비워도 백엔드는 무시.

### 6.3 교정 — `*Clear` 명시 플래그

```kotlin
data class ReminderUpdateRequest(
    val notes: String? = null,
    val notesClear: Boolean = false,
    val dueAt: Instant? = null,
    val dueAtClear: Boolean = false,
    val parentId: Long? = null,
    val parentIdClear: Boolean = false,
    ...
)

data class UpdateReminderCommand(...) {
    init {
        require(!(dueAt != null && dueAtClear)) {
            "dueAt 와 dueAtClear 를 동시에 지정할 수 없습니다."
        }
        // ... 같은 검증을 notes/parentId 에도 적용
    }
}
```

서비스의 분기:
```kotlin
when {
    command.dueAtClear -> entity.changeDueAt(null)
    command.dueAt != null -> entity.changeDueAt(command.dueAt)
    // 둘 다 없으면 변경 없음
}
```

### 6.4 대안 — JSON Merge Patch (RFC 7386)

이 패턴 대신 JSON Merge Patch 를 도입하면 "키 부재 = 변경 없음 / null = 명시적 clear" 가 자연스럽다.
하지만 Jackson 기본은 두 케이스를 구분 못함 → `JsonNullable<T>` (openapitools) 또는 `JsonPatch` 라이브러리 필요.
이 프로젝트 규모에선 명시 플래그가 더 직관적이고 단순하다고 판단.

---

## 7. 시간대 (Asia/Seoul ↔ UTC) 처리

### 7.1 백엔드 — 모든 저장은 UTC `Instant`

- DB 컬럼 `due_at TIMESTAMP` 는 UTC 로 저장.
- `Instant` 만 사용. `LocalDateTime` 은 도메인에 등장하지 않음.
- `application.yml`: `spring.jackson.time-zone=UTC` — JSON 직렬화 일관성 보장.

### 7.2 스마트 뷰의 "오늘" 경계 — Clock + ZoneId 주입

```kotlin
fun today(zone: ZoneId): List<ReminderView> {
    val (start, end) = todayBoundary(zone)
    return reminderRepository.findDueBetween(start, end).map(ReminderView::from)
}

private fun todayBoundary(zone: ZoneId): Pair<Instant, Instant> {
    val today = LocalDate.now(clock.withZone(zone))
    val start = today.atStartOfDay(zone).toInstant()
    val end = today.plusDays(1).atStartOfDay(zone).toInstant()
    return start to end
}
```

- `Clock` 빈을 주입 → 테스트에서 `@Primary FixedClock` 으로 시간 고정 가능
- `clock.withZone(zone)` 으로 zone 의 LocalDate 계산
- 결과는 다시 `Instant` 로 변환해 DB 쿼리 (UTC 로 저장됐으니 일관)

### 7.3 invalid timezone 의 함정

```
?tz=Invalid/Zone
```
`ZoneId.of(...)` 는 `ZoneRulesException` 을 던진다. 매핑이 없으면 Spring 디폴트 500 → 클라이언트는 우리의 `ErrorResponse` 를 받지 못함.

```kotlin
@ExceptionHandler(DateTimeException::class)
fun handleDateTime(ex: DateTimeException) =
    ResponseEntity.status(BAD_REQUEST)
        .body(ErrorResponse(code = "INVALID_TIMEZONE", message = ex.message ?: "..."))
```

### 7.4 프론트엔드 — `toISOString().slice(0,10)` 의 함정

```ts
// 잘못
const key = new Date(r.dueAt).toISOString().slice(0, 10);
// '2026-04-30T15:00:00Z' (KST 5/1 00:00) → '2026-04-30' 으로 잡힘 → KST 5/1 그룹이 어제로 묶임
```

`toISOString()` 은 항상 UTC. 로컬 날짜로 묶고 싶다면 `Intl.DateTimeFormat` 의 `timeZone` 옵션을 써야 한다.

```ts
// 올바름
const formatter = new Intl.DateTimeFormat("en-CA", {
    timeZone: "Asia/Seoul", year: "numeric", month: "2-digit", day: "2-digit",
});
const key = formatter.format(new Date(r.dueAt));   // '2026-05-01'
```

`en-CA` 로케일은 `YYYY-MM-DD` 포맷을 자연스럽게 만들어준다 (정렬 친화적).

---

## 8. 프론트엔드 TanStack Query 패턴

### 8.1 쿼리 키 설계

```ts
export const reminderKeys = {
    byList: (listId: number, completed: boolean) =>
        ["reminders", "list", listId, completed] as const,
    children: (parentId: number) => ["reminders", "children", parentId] as const,
};
```

- 계층적 prefix (`["reminders", "list", listId, ...]`) → invalidate 시 부분 매칭으로 특정 listId 의 모든 캐시 한 번에 무효화.
- `as const` 로 타입 좁힘.

### 8.2 invalidate 의 정밀도

```ts
useUpdateReminder() {
    return useMutation({
        mutationFn: ({ id, listId, ...rest }) => api.patch(`/reminders/${id}`, rest),
        onSuccess: (_data, vars) => {
            qc.invalidateQueries({ queryKey: ["reminders", "list", vars.listId] });
        },
    });
}
```

- 전 `["reminders"]` 를 invalidate 하면 다른 list 캐시까지 폭격 → 불필요한 refetch.
- listId 까지 좁혀 그 list 의 미완료/완료 두 캐시만 무효화.

### 8.3 낙관적 업데이트 (optimistic update)

체크 토글은 즉시 반응이 가장 중요하다.

```ts
useToggleReminder() {
    return useMutation({
        mutationFn: ({ id }) => api.post(`/reminders/${id}/toggle`),
        onMutate: async (vars) => {
            const completedKey = reminderKeys.byList(vars.listId, vars.completed);
            const otherKey = reminderKeys.byList(vars.listId, !vars.completed);
            await qc.cancelQueries({ queryKey: completedKey });

            const prev = qc.getQueryData<Reminder[]>(completedKey);
            // 토글 즉시 현재 섹션에서 제거
            qc.setQueryData<Reminder[]>(completedKey, (old) =>
                (old ?? []).filter((r) => r.id !== vars.id),
            );
            return { prev, completedKey };
        },
        onError: (_err, _vars, ctx) => {
            if (ctx) qc.setQueryData(ctx.completedKey, ctx.prev);   // 롤백
        },
        onSettled: (_data, _err, vars) => {
            qc.invalidateQueries({ queryKey: ["reminders", "list", vars.listId] });
        },
    });
}
```

- `onMutate` 가 변경 즉시 캐시를 mutate → UI 가 0ms 내 반응
- `onError` 에서 stash 한 prev 로 롤백
- `onSettled` 에서 무조건 refetch 하여 서버 진실값과 동기화

### 8.4 디바운스된 PATCH 의 함정

`ReminderExpander` 가 `useEffect(..., [notes, dueAt, priority, flagged])` 로 500ms debounce 후 PATCH 를 보내도록 했다.
처음 구현은 두 가지 버그가 있었다:

1. **마운트 즉시 1회 PATCH 가 발사**됨 — useEffect 가 첫 렌더에도 실행되니까. 사용자는 ⓘ 만 펼쳤는데 변경 없는 PATCH 가 한 번 나간다.
2. **부모가 `invalidateQueries` 후 새 reminder 데이터를 내려보내도 로컬 state 는 stale** 한 채 남는다 (useState 초기값은 첫 렌더에만 적용).

해결:
```ts
const [form, setForm] = useState(() => reminderToFormState(reminder));
const [isDirty, setIsDirty] = useState(false);

// reminder 가 바뀌면 폼 동기화 + dirty 리셋
useEffect(() => {
    setForm(reminderToFormState(reminder));
    setIsDirty(false);
}, [reminder.id, reminder.updatedAt]);

// 사용자 변경이 있을 때만 debounced PATCH
useEffect(() => {
    if (!isDirty) return;
    const timer = setTimeout(() => mutate(buildPatchPayload(form)), 500);
    return () => clearTimeout(timer);
}, [form, isDirty]);

function patch(partial) {
    setForm((p) => ({ ...p, ...partial }));
    setIsDirty(true);
}
```

- `isDirty` 가 첫 렌더 PATCH 를 차단
- `[reminder.id, reminder.updatedAt]` 의존성으로 새 데이터 들어오면 폼을 다시 채움
- 변환 로직(`reminderToFormState`, `buildPatchPayload`) 을 순수 함수로 추출 → vitest 단위 테스트 가능

---

## 9. Exit Criteria 와 코드 리뷰의 가치

### 9.1 Exit Criteria 가 발견한 자기참조 cascade 버그

테스트 88건이 모두 그린이고 phase 4 까지 모두 끝난 것처럼 보였다.
그런데 **실 서버를 띄우고 손으로 한 번 만져보니** parent-child reminder 가 있는 list 삭제가 500 으로 깨졌다.

→ Exit Criteria 검증 (실 서버 + curl 시나리오 + 실제 사용 패턴) 이 **유닛 테스트가 못 잡는 데이터 위상 회귀** 를 잡는다.
→ 그 자리에서 회귀 테스트를 추가해 다시는 같은 회귀가 일어나지 않게 한다.

### 9.2 코드 리뷰가 식별한 잠복 버그

Phase 4 까지 다 끝나고 나서 코드 리뷰를 받았다. 단순히 "잘 짰는지" 가 아니라 **운영에서 어떻게 깨질지** 를 묻는 리뷰.
발견된 28건 중 5건은 즉시 운영에 영향:

| 이슈 | 잠복 형태 |
|---|---|
| invalid tz → 500 | 우리 ErrorResponse 가 아닌 Spring 디폴트 500 |
| ReminderExpander 마운트 PATCH | ⓘ 만 펼쳐도 빈 PATCH 발사 |
| scheduled UTC 그룹핑 | KST 자정 직후 항목이 어제 그룹 |
| LAZY 프록시 비활성 | startup 경고만 있고 표면은 작동, 잠복 N+1 |
| PATCH 명시 clear 불가 | outdent / dueAt 비우기 불가 |

→ "테스트 다 그린 = 끝" 이 아니다. 리뷰 + Exit Criteria + 손 테스트를 같이 돌려야 한다.

---

## 10. TDD 적용 사례 5건 — Red·Green·Refactor 의 실제 모습

위 5건을 모두 TDD 사이클로 처리했다. **실패하는 테스트부터** 작성하니 자연스럽게:
- 의도가 코드 위에 명시 (테스트가 곧 스펙)
- 회귀 안전망이 자동으로 생김
- "미세하게 잘못 고치는" 함정을 차단 (잘못 고치면 테스트가 실패)

### 10.1 R1 — invalid tz 매핑 (5분)

**Red**: 컨트롤러 통합 테스트
```kotlin
it("유효하지 않은 timezone 은 400 + INVALID_TIMEZONE") {
    mockMvc.get("/api/v1/views/today?tz=Invalid/Zone")
        .andExpect {
            status { isBadRequest() }
            jsonPath("$.code") { value("INVALID_TIMEZONE") }
        }
}
```
초기 결과 → FAILED (500 Internal Server Error).

**Green**: GlobalExceptionHandler 에 매핑 추가
```kotlin
@ExceptionHandler(DateTimeException::class)
fun handleDateTime(ex: DateTimeException) =
    ResponseEntity.status(BAD_REQUEST).body(
        ErrorResponse(code = "INVALID_TIMEZONE", message = ex.message ?: "..."))
```

**Refactor**: 추가 정리 없음. 전체 회귀 88 → 그린.

### 10.2 R3 — timezone-aware 그룹핑

**Red**: 순수 함수로 분리하고 vitest 5케이스
```ts
it("KST 기준으로 5/1 새벽 0시(UTC 4/30 15:00)는 5/1 그룹", () => {
    const groups = groupByLocalDate([r(1, "2026-04-30T15:00:00Z")], "Asia/Seoul");
    expect([...groups.keys()]).toEqual(["2026-05-01"]);
});
```

**Green**: `Intl.DateTimeFormat("en-CA", { timeZone })` 기반 헬퍼 작성.

**Refactor**: 페이지에서 헬퍼 사용 + 그룹 라벨도 timezone-aware.

### 10.3 R2 — 마운트 PATCH + state stale

**Red**: 변환 로직(`reminderToFormState`, `buildPatchPayload`)을 순수 함수로 분리 → 단위 테스트 6건.
첫 PATCH 차단·재동기화 같은 컴포넌트 행위는 단위 테스트로는 어렵지만, 변환 로직만 단위 테스트해도 절반은 검증된다.

**Green**: 헬퍼 작성.

**Refactor**: 컴포넌트가 헬퍼 + isDirty + `[reminder.id, reminder.updatedAt]` 동기화 effect 사용.

### 10.4 R5 — final getter LAZY 정상화

**Red**: Hibernate Statistics 로 N+1 회귀 테스트
```kotlin
stats.clear(); stats.isStatisticsEnabled = true
val reminders = repo.findByListIdAndCompleted(listId, false)
reminders.forEach { it.list.id }   // PK 만 접근
stats.prepareStatementCount shouldBe 1L
```
초기 결과 → FAILED (실제로는 N+1 발생).

**Green**: 도메인의 `final var x` → `var x; protected set`.
컴파일 에러 (`Private setters for open properties are prohibited`) → `protected set` 으로 변경.

**Refactor**: CLAUDE.md 컨벤션 문구 갱신 — 이런 결정은 **컨벤션 문서에 적어 둬야 다음 사람이 같은 함정을 안 밟는다**.

### 10.5 M1 — PATCH 명시 clear

**Red**: 컨트롤러 통합 테스트 4건 (각 *Clear 플래그 + 충돌 케이스).

**Green**: DTO/Command/Service/도메인 require 추가.

**Refactor**: 프론트엔드 outdent 활성화 + ReminderExpander 빈 입력을 *Clear 로 보내기 + vitest 갱신.

---

## 11. 커밋·이슈 관리 워크플로

### 11.1 커밋 단위 = "한 가지 의도"

이 프로젝트의 커밋 히스토리를 보면:
```
fix(R1): 잘못된 timezone 파라미터 → 400 INVALID_TIMEZONE 매핑.
fix(R3): scheduled 뷰 그룹핑을 timezone-aware 로컬 날짜 기준으로 수정.
fix(R2): ReminderExpander 마운트 시 빈 PATCH 차단 + reminder prop 변경 동기화.
fix(R5): 엔티티 final var → var (protected set) 으로 변경, Hibernate LAZY 프록시 정상화.
fix(M1): PATCH 에 dueAtClear/notesClear/parentIdClear 명시적 clear 플래그 도입.
```

- 한 커밋 = 한 issue id.
- 이슈 추적 (`docs/issues.md`) 의 항목과 1:1 매핑 → 어떤 커밋이 어떤 이슈를 푸는지 자명.
- 메시지 본문에 **변경의 이유**(WHY) 와 **테스트 결과** 를 명시.

### 11.2 이슈를 살아 있는 문서로

```md
- [x] **R1. 잘못된 timezone 파라미터 → 500**
  - 위치: `.../ReminderViewController.kt:19,23,39`
  - 증상: ...
  - 고치기: ...
```

- 코드 리뷰 결과를 issues.md 에 GitHub task list 로 정리.
- 작업 끝나면 `[x]` 체크 + 같은 커밋에서 갱신.
- "지금 무엇이 남았는가" 가 항상 한 화면에 보임.

### 11.3 phase 별 커밋 + Exit Criteria 검증

각 phase 의 커밋은 다음을 모두 포함했다:
1. 구현
2. 단위/통합/E2E 테스트
3. tasks.md 의 해당 항목 `[x]` 체크
4. (phase 끝나면) 실 서버로 Exit Criteria 검증 후 그 결과까지 commit

Exit Criteria 단계에서 잡힌 cascade 버그처럼, **단위 테스트로는 잡히지 않는 회귀가 phase 마다 나오므로** 이 단계는 절대 생략하지 않는다.

---

## 부록 A — 자주 부딪힌 함정 빠른 참조

| 증상 | 의심 |
|---|---|
| `LazyInitializationException` | 트랜잭션 밖에서 LAZY 프록시 접근. View 매핑은 `@Transactional` 안에서. |
| `TransientPropertyValueException` | bulk delete 후 stale managed entity. `@Modifying(flushAutomatically=true, clearAutomatically=true)` |
| startup `Getter methods of lazy classes cannot be final` | 도메인 프로퍼티에 `final` 명시. 제거하고 `protected set` 으로. |
| `?tz=Invalid/Zone` → 500 | `DateTimeException` 매핑 누락. |
| KST 자정 직후 항목이 어제 그룹 | 프론트의 `toISOString().slice(0,10)` → `Intl.DateTimeFormat` + `timeZone`. |
| ⓘ 펼치기만 했는데 PATCH 발사 | useEffect 에 isDirty 가드 부재. |
| optimistic update 후 화면 깜빡임 | `cancelQueries` 누락 또는 `onSettled` invalidate 가 너무 광범위. |

## 부록 B — 빠른 결정 가이드

- 새 어그리게이트 추가? → `<aggregate>/{domain,application/{command,query,port,service},adapter/{in/web,out/persistence}}` 구조 그대로.
- 새 엔티티 프로퍼티? → `var x: T = init; protected set`. 도메인 메서드를 통해서만 mutate.
- 새 PATCH 필드? → null 이 의미 있으면 `*Clear` 플래그 추가. require 로 충돌 검증.
- 새 시간 관련 기능? → `Instant` 만 사용. zone 은 입력 파라미터로 받고 `Clock` 빈 주입.
- 새 reminder 다건 쿼리? → `findBy*OrderBy*` 파생 쿼리 또는 `@Query` + 명시적 정렬키 (정렬 비결정성 방지).
- 새 React 컴포넌트의 사이드이펙트? → 변환 로직은 순수 함수로 추출 → vitest 단위 테스트.
- 새 mutation? → invalidate 키는 가능한 좁게. 즉각 반응이 중요하면 `onMutate` 낙관적 업데이트 + `onError` 롤백.

---

## 한 줄 요약

> "테스트 그린 = 끝" 이 아니다. **실 서버 손 테스트 + 코드 리뷰 + 회귀 테스트** 가 같이 돌아야
> 정말로 깨지지 않는 코드가 된다. TDD 사이클은 그 회귀 테스트를 자동으로 만들어 주는 도구다.
