# Project Conventions

## 언어
- 사용자 노출 문구는 한글, 마침표(`.`)로 끝맺음.

## 프로젝트 구조
헥사고날 + CQRS-lite 아키텍처를 준수한다.

```
<aggregate>/
├── domain/                              # JPA 엔티티(=도메인), 도메인 예외, 도메인 메서드
├── application/
│   ├── port/
│   │   ├── in/                          # …CommandService, …QueryService 인터페이스 (입력 포트)
│   │   └── out/                         # …RepositoryPort (어그리게이트당 단일, 출력 포트)
│   ├── command/                         # 쓰기 입력 객체 (Create/Update/Delete…Command)
│   ├── query/                           # 읽기 출력 객체 (…View)
│   └── service/                         # Default…CommandService, Default…QueryService (인터페이스 기본 구현체)
└── adapter/
    ├── in/web/                          # 매퍼(Request↔Command, View→Response) + dto/
    │   └── dto/                         # web Request / Response DTO (validation 어노테이션)
    └── out/persistence/                 # …JpaRepository(Spring Data), …PersistenceAdapter(@Component, RepositoryPort 구현)
```

## 도메인 / JPA
- 엔티티 = JPA 엔티티 단일 클래스, `common/BaseEntity` 상속
- 엔티티 프로퍼티는 **`final var` + `private set`**. 

## 네이밍
- 입력 포트(인터페이스): `…CommandService` / `…QueryService` — `UseCase` 등 별도 접미사 붙이지 않음.
- 기본 구현체: `Default…CommandService` / `Default…QueryService` — `@Service` + 인터페이스 구현. 어그리게이트당 1개 (다른 구현이 등장하면 이름은 그 구현의 의도를 표현).
- 의존성·테스트 주입은 모두 인터페이스 타입으로. `@Import` 슬라이스 인자만 구현체 클래스(`Default…`).

## 테스트
- 기능 추가/수정 시 테스트를 같은 작업 단위에 작성. 
- 도메인·매퍼: 순수 단위 테스트 
- 서비스·어댑터: `@DataJpaTest` + `@Import(JpaConfig::class, …PersistenceAdapter::class, …Service::class)` 통합 테스트. 트랜잭션·dirty checking·auditing 까지 검증.
- 통합 테스트는 트랜잭션 **밖**에서 실행한다. `@DataJpaTest` 가 메타-어노테이션으로 `@Transactional` 을 기본 적용하므로, 클래스에 `@Transactional(propagation = NOT_SUPPORTED)` 를 명시해 그 기본값을 끈다. 이유: 서비스 자체가 `@Transactional` 이므로 매 호출이 독립 tx 로 commit/rollback 되어야 후속 SELECT 가 디스크 상태를 정직하게 본다(테스트와 서비스가 같은 tx 에 있으면 dirty 상태가 1차 캐시로 비치고, 롤백 검증이 거짓 양성). 격리는 `afterEach { jpaRepository.deleteAll() }`.
- `SpringExtension` 은 `src/test/kotlin/com/bong/reminder/ProjectConfig.kt` 에서 전역 등록 — 클래스마다 `extension(SpringExtension)` 반복 금지.

## 참고문서
- @docs/spec.md : 기능 명세
- @docs/plan.md : 개발 계획
- @docs/tasks.md : 구현 태스크 체크리스트
