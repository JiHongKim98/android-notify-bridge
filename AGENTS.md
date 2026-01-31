# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## 빌드 및 실행 명령

```bash
# 디버그 빌드
./gradlew :app:assembleDebug

# 릴리스 빌드
./gradlew :app:assembleRelease

# 연결된 디바이스에 설치 및 실행
./gradlew :app:installDebug

# 전체 클린 빌드
./gradlew clean assembleDebug

# 단위 테스트 실행
./gradlew :app:test

# 특정 테스트 클래스 실행
./gradlew :app:test --tests "com.example.kakaodiscord.<TestClassName>"

# 林트 검사
./gradlew :app:lint
```

> Gradle 래퍼(`gradlew`)를 항상 사용한다. 시스템 gradle 직접 호출 불가.

---

## 아키텍처 개요

카카오톡 알림을 감시하여 특정 채팅방 · 발신자 조건에 맞으면 Discord Webhook으로 전송하는 단일 모듈 Android 앱이다.

```
카카오톡 알림
    │
    ▼
KakaoNotificationListenerService   ← OS가 유지하는 백그라운드 서비스
    │  패키지 필터 (com.kakao.talk)
    │  채팅방명 필터 (대소문자 무시, contains)
    │  발신자명 필터 (대소문자 무시, contains)
    │  중복 검증 (NotificationCache)
    ▼
DiscordWebhookSender               ← 단일 스레드풀로 비동기 HTTP POST
    │
    ▼
Discord Webhook URL
```

### 핵심 설계 원칙 (PRD 기반)

- **폴링·타이머·ForegroundService·WorkManager·WakeLock 사용 금지.** `onNotificationPosted()` 이벤트만 사용한다.
- **재시도 없음.** Webhook 전송 실패 시 로그만 남긴다.
- **Webhook URL은 외부로 출력하지 않는다.** SharedPreferences 저장만 가능.

### 주요 클래스 역할

| 클래스 | 역할 |
|---|---|
| `MainActivity` | 설정 UI (Webhook URL, 채팅방명, 발신자명 입력 및 저장, 권한 상태 표시) |
| `KakaoNotificationListenerService` | 알림 수신 → 필터링 파이프라인 |
| `DiscordWebhookSender` | HTTP POST로 Webhook 실행 (단일 스레드풀) |
| `NotificationCache` | LRU 캐시 기반 중복 알림 방지 (TTL 5s, 최대 50개) |
| `PreferenceManager` | SharedPreferences 키-값 저장소 (`kakao_discord_prefs`) |

### 중복 방지 캐시 키 형식

```
roomName|sender|message
```

### Discord 메시지 페이로드

```json
{
  "content": "**[채팅방명] 발신자**\n메시지 내용"
}
```

---

## 프로젝트 구성

- **루트 모듈:** `:app` (단일 모듈 프로젝트)
- **패키지명:** `com.example.kakaodiscord`
- **compileSdk:** 34 / **minSdk:** 26 (Android 8.0) / **targetSdk:** 34
- **Kotlin:** 1.9.21 / **JVM target:** 17
- **Android Gradle Plugin:** 8.12.0
- **의존성:** core-ktx, appcompat, Material Components, ConstraintLayout (외부 네트워크 라이브러리 없음 — HttpURLConnection 사용)

---

## 주의사항

- `NotificationListenerService`는 앱 프로세스와 독립적으로 OS가 유지한다. 앱 종료 후에도 서비스가 동작하므로, 설정값은 항상 SharedPreferences에서 읽어야 한다.
- 알림 미리보기 설정에 따라 카카오톡이 메시지 본문을 잘라 전달할 수 있다.
- `settings.gradle.kts`의 `rootProject.name`은 `KakaoDiscord`로 설정되어 있다.
