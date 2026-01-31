# PRD – KakaoTalk Notification → Discord Webhook Router

## 1. 제품 개요 (Overview)

본 제품은 **안드로이드 기기에서 카카오톡 알림(Notification)을 감시**하여  
**특정 오픈채팅방의 특정 사용자가 보낸 메시지 알림만 필터링**한 뒤,  
이를 **사용자가 입력한 Discord Webhook URL로 실시간 전송**하는 내부용 안드로이드 애플리케이션이다.

본 앱은 **카카오톡 앱 자체나 메시지 프로토콜을 직접 제어하지 않으며**,  
안드로이드 OS가 제공하는 **NotificationListenerService 이벤트 기반 구조**만 사용한다.

---

## 2. 목표 (Goals)

- 이미 참여 중인 **오픈채팅방 / 1:1 채팅방을 그대로 유지**
- **추가 서버 없이** 안드로이드 단말 단독으로 동작
- **배터리 사용 최소화** (폴링, 타이머, 포그라운드 서비스 미사용)
- 카카오톡 알림 중:
  - 특정 오픈채팅방
  - 특정 발신자
  만 필터링하여 Discord로 라우팅

---

## 3. 비목표 (Non-Goals)

- 카카오톡 메시지 전체 본문 수집
- 이미지, 파일, 링크 미리보기 처리
- 카카오톡 메시지 발송/응답
- 여러 Discord Webhook 동시 전송
- 다중 계정/다중 디바이스 동기화
- iOS 지원

---

## 4. 사용자 시나리오 (User Flow)

1. 사용자는 앱을 설치한다.
2. 최초 실행 시:
   - Discord Webhook URL을 입력한다.
   - 필터링할 오픈채팅방 이름을 입력한다.
   - 필터링할 발신자 이름을 입력한다.
3. 사용자는 시스템 설정에서 **알림 접근(Notification Access)** 권한을 허용한다.
4. 이후:
   - 카카오톡 알림이 수신될 때마다
   - 조건에 일치하면
   - Discord Webhook으로 즉시 전송된다.
5. 앱은 UI 없이 **백그라운드에서 자동 동작**한다.

---

## 5. 핵심 기능 요구사항 (Functional Requirements)

### 5.1 Notification Listener

- Android `NotificationListenerService` 기반
- `onNotificationPosted()` 이벤트만 사용
- **폴링 / 타이머 / 주기 작업 사용 금지**

---

### 5.2 카카오톡 알림 필터링

#### 5.2.1 패키지 필터
- `sbn.packageName == "com.kakao.talk"` 인 경우만 처리

#### 5.2.2 오픈채팅방 필터
- 알림 `EXTRA_TITLE` 또는 `EXTRA_SUB_TEXT`에서
- 사용자가 설정한 **채팅방 이름과 일치**하는 경우만 통과

#### 5.2.3 발신자 필터
- 알림 본문 또는 제목에서
- 사용자가 설정한 **발신자 이름 포함 여부**로 필터링

> 문자열 비교 기준:
> - 기본: contains
> - 대소문자 무시

---

### 5.3 Discord Webhook 전송

- Discord Bot API ❌
- Discord Webhook Execute API ⭕
- HTTPS POST 1회 요청
- Payload 구조:

```json
{
  "content": "**[채팅방명] 발신자**\n메시지 미리보기"
}
```

- 전송 실패 시:
  - 재시도 ❌
  - 로그만 남김

---

### 5.4 중복 알림 방지

- 동일 알림이 여러 번 전달되는 것을 방지
- 기준 키:
  - `(채팅방명 + 발신자 + 메시지 + timestamp)`
- 메모리 기반 LRU 캐시 사용
- TTL: 3~5초

---

## 6. 비기능 요구사항 (Non-Functional Requirements)

### 6.1 배터리 효율

- Foreground Service ❌
- WorkManager ❌
- AlarmManager ❌
- WakeLock ❌
- WebSocket ❌

> 알림 이벤트 발생 시에만 CPU / Network 사용

---

### 6.2 안정성

- 앱이 종료되어도 NotificationListenerService는 OS에 의해 유지
- 앱 재부팅 시 설정값 유지 (SharedPreferences)

---

### 6.3 보안

- Discord Webhook URL은:
  - 로컬 SharedPreferences에 저장
  - 외부 공유/로그 출력 금지
- 앱 외부 통신은 Discord Webhook URL 단일 목적

---

## 7. 설정 UI 요구사항

### 7.1 설정 항목

- Discord Webhook URL (필수)
- 필터링할 채팅방 이름 (필수)
- 필터링할 발신자 이름 (필수)
- 알림 접근 권한 상태 표시 (읽기 전용)

---

### 7.2 UI 특성

- Activity 1개
- 단순 Form 기반
- Material UI 사용 여부 무관
- 앱 실행 후 설정 완료 시 추가 상호작용 없음

---

## 8. 기술 스택

| 항목 | 선택 |
|----|----|
| Language | Kotlin |
| Min SDK | Android 8.0 (API 26) 이상 |
| Network | OkHttp 또는 HttpURLConnection |
| Storage | SharedPreferences |
| Service | NotificationListenerService |

---

## 9. 제약 사항 / 한계

- 알림 미리보기 설정에 따라 메시지 내용이 일부 잘릴 수 있음
- 카카오톡 알림 정책 변경 시 동작 영향 가능
- 이미지/이모지/첨부파일은 텍스트로만 전달됨

---

## 10. 성공 지표 (Success Metrics)

- 알림 발생 → Discord 전송까지 평균 지연 < 500ms
- 하루 배터리 사용량 증가 < 1%
- 알림 누락률 < 1%

---

## 11. 향후 확장 가능성 (Out of Scope)

- 다중 Webhook 지원
- 서버 중계 옵션
- 키워드 기반 필터링
- 알림 요약 전송
- 로그 업로드 / 원격 설정
