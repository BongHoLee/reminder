# Project Conventions

## 언어
- 사용자 노출 문구는 한글, 마침표(`.`)로 끝맺음.

## 도메인 / JPA
- 엔티티 프로퍼티는 **`final var` + `private set`** 으로 외부 변경 차단.
- 모든 엔티티는 `common/BaseEntity` 상속.
- 도메인 엔티티 = JPA 엔티티 (단일 클래스, 분리 안 함).

## 아키텍처 (헥사고날 + CQRS-lite)
- 어그리게이트별 패키지 트리: `<aggregate>/{domain, application/{port/{in,out}, command, query, service}, adapter/{in/web, out/persistence}}`.
- 입력 포트: `…CommandUseCase` / `…QueryUseCase` (Command 와 Query 분리, 트랜잭션 정책도 분리: `@Transactional` vs `@Transactional(readOnly = true)`).
- 출력 포트: 어그리게이트당 단일 `…RepositoryPort`. JPA 는 `adapter/out/persistence` 의 `…JpaRepository` + `…PersistenceAdapter` 로 캡슐화. `Optional` 등 인프라 타입은 어댑터 안에서만.
- 응용 계층은 `Command` 입력 / `View` 출력. 웹 DTO ↔ Command/View 변환은 `adapter/in/web` 의 mapper 책임. 응용 계층은 web DTO 를 import 하지 않음.

## 테스트 (Kotest)
- 기능 추가/수정 시 반드시 테스트를 함께 작성
- 도메인 테스트는 순수 단위 테스트

## 참고문서
- @docs/spec.md : 기능 명세 
- @docs/plan.md : 개발 계획
- @docs/tasks.md : 구현 태스크 체크리스트