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
- 기본 구현체: `Default…CommandService` / `Default…QueryService` — `@Service` + 인터페이스 구현.

## 테스트
- 기능 추가/수정 시 테스트를 같은 작업 단위에 작성.
- 도메인 엔티티 테스트는 순수 단위 테스트
- **서비스 통합 테스트**: `@SpringBootTest` + `@Transactional`. 

## 참고문서
- @docs/spec.md : 기능 명세
- @docs/plan.md : 개발 계획
- @docs/tasks.md : 구현 태스크 체크리스트
