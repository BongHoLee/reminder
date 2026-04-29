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
│   │   ├── in/                          # …CommandUseCase, …QueryUseCase
│   │   └── out/                         # …RepositoryPort (어그리게이트당 단일)
│   ├── command/                         # 쓰기 입력 객체 (Create/Update/Delete…Command)
│   ├── query/                           # 읽기 출력 객체 (…View)
│   └── service/                         # …CommandService, …QueryService (UseCase 구현)
└── adapter/
    ├── in/web/                          # 매퍼(Request↔Command, View→Response) + dto/
    │   └── dto/                         # web Request / Response DTO (validation 어노테이션)
    └── out/persistence/                 # …JpaRepository(Spring Data), …PersistenceAdapter(@Component, RepositoryPort 구현)
```

## 도메인 / JPA
- 엔티티 = JPA 엔티티 단일 클래스, `common/BaseEntity` 상속
- 엔티티 프로퍼티는 **`final var` + `private set`**. 

## 테스트
- 기능 추가/수정 시 테스트를 같은 작업 단위에 작성.

## 참고문서
- @docs/spec.md : 기능 명세
- @docs/plan.md : 개발 계획
- @docs/tasks.md : 구현 태스크 체크리스트
