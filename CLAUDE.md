# Project Conventions

## 프로젝트 개발 규칙
- 에이전틱 프로그래밍 SDD 스타일로 진행
- @docs/spec.md, @docs/plan.md, @docs/tasks.md 문서 참조

## 언어

- **모든 사용자 노출 문구는 한글.** 마침표(`.`)로 끝맺음.
  - `require` / `check` / 도메인 예외 메시지
  - API 응답의 `message`, validation 오류, `ErrorResponse`
  - 테스트명 (`describe` / `it` / 백틱 함수명)
  - 커밋 메시지 (요약 + 본문 모두)
- 로그 메시지는 영문 허용 (운영자 대상).

## 코드 스타일

- 주석은 비자명한 *이유(WHY)* 가 있을 때만 한 줄로. 식별자가 설명을 대신.
- 불필요한 추상화/방어 코드 금지. 검증은 시스템 경계(컨트롤러 입력, 외부 API)와 도메인 내부에서.

## 도메인 / JPA

- 엔티티 상태 변경은 **도메인 메서드**로만 (`rename`, `recolor` 등). setter 직접 호출 금지.
- 검증(`require`)은 도메인 내부.
- 엔티티 프로퍼티는 **`final var` + `private set`** 패턴으로 외부 변경 차단. (`allOpen` 의 기본 open 동작은 `final` 명시로 오버라이드)
- 시간은 `Instant` (UTC). `@CreatedDate` / `@LastModifiedDate` + `@EnableJpaAuditing` 자동 관리.

## 테스트 (Kotest)

- 프레임워크: **Kotest** 기본. 스타일은 `DescribeSpec` 우선, 단순 케이스만 `StringSpec`.
- 어설션: `io.kotest.matchers.*` (`shouldBe`, `shouldThrow`, `shouldBeNull` 등). JUnit `assertEquals` 지양.
- Mocking: **MockK** (Spring 컨텍스트는 `springmockk`).
- 도메인 규칙·검증 로직은 단위 테스트로 모두 커버.

## API

- 경로: `/api/v1/...`
- 시각: ISO-8601 UTC. 클라 시간대는 `?tz=Asia/Seoul`.

## 기술 스택 (참고)

- Backend: Kotlin 2.3 + Spring Boot 4.0.3 + JPA/H2, JDK 25
- Frontend: Next.js 15 + TS + Tailwind v4 + TanStack Query (예정)
