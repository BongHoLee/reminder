# Project Conventions

## 언어
- 사용자 노출 문구는 한글, 마침표(`.`)로 끝맺음.

## 도메인 / JPA
- 엔티티 프로퍼티는 **`final var` + `private set`** 으로 외부 변경 차단.
- 모든 엔티티는 `common/BaseEntity` 상속

## 테스트 (Kotest)
- 기능 추가/수정 시 반드시 테스트를 함께 작성
- 도메인 테스트는 순수 단위 테스트

## 참고문서
- @docs/spec.md : 기능 명세 
- @docs/plan.md : 개발 계획
- @docs/tasks.md : 구현 태스크 체크리스트