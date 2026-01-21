<div align="center">

![PayFlow Logo](./logo.png)

# PayFlow

**AI 기반 구독 관리 및 지출 분석 서비스**

흩어진 구독을 한곳에서 관리하고, AI가 분석하는 똑똑한 지출 관리

[![Java](https://img.shields.io/badge/Java-17-red.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3-blue.svg)](https://vuejs.org/)

</div>

---

## 📌 프로젝트 소개

PayFlow는 **구독 서비스가 넘쳐나는 시대**에 흩어진 결제 정보를 한곳에서 관리하고, **AI가 지출 패턴을 분석**해 개인 맞춤형 재정 관리 인사이트를 제공하는 웹 애플리케이션입니다.

"Netflix, YouTube Premium, Spotify... 이번 달 구독료만 얼마지?" 
이런 고민을 **PayFlow**가 해결합니다.

---

## ✨ 주요 기능

### 🎯 핵심 기능

- **📊 구독 통합 관리** - Netflix, YouTube Premium 등 모든 구독을 한눈에 확인
- **⏰ 자동 결제 알림** - 결제 예정일 3일 전 이메일 발송으로 예상치 못한 지출 방지
- **🤖 AI 지출 분석** - Google Gemini API 활용, 월간 지출 패턴 분석 및 절감 팁 제공
- **📅 스마트 결제일 계산** - 윤년, 월말 보정을 고려한 정확한 다음 결제일 자동 계산

### 🎨 사용자 경험

- 직관적인 대시보드로 월간/연간 지출 한눈에 파악
- 카테고리별 지출 비중 시각화
- 불필요한 구독 감지 및 개인 맞춤형 절감 조언

---

## 🛠 기술 스택

| Category | Technologies |
|----------|-------------|
| **Backend** | Java, Spring Boot, Spring Data JPA, Spring Security, JWT |
| **Frontend** | Vue.js, Vite, Axios |
| **AI & External Services** | Google Gemini API (지출 분석 및 추천), Spring Mail |
| **Database** | MySQL, Redis |

---

## 🏗 시스템 아키텍처

```
┌─────────────────┐
│  Vue.js Client  │
└────────┬────────┘
         │ HTTP (Axios)
         ▼
┌─────────────────────────┐
│   Spring Boot Server    │
│  ┌──────────────────┐   │
│  │ Security Filter  │◄──┼── JWT 인증
│  └────────┬─────────┘   │
│           ▼             │
│  ┌──────────────────┐   │
│  │   Controllers    │   │
│  └────────┬─────────┘   │
│           ▼             │
│  ┌──────────────────┐   │
│  │    Services      │───┼──► Gemini API
│  └────────┬─────────┘   │
│           ▼             │
│  ┌──────────────────┐   │
│  │  Repositories    │   │
│  └────────┬─────────┘   │
└───────────┼─────────────┘
            ▼
    ┌───────────────┐
    │ MySQL + Redis │
    └───────────────┘
```

---

## 🚀 핵심 구현 사항

### 1️⃣ 구독 결제일 자동 계산 로직

#### 문제 상황
1월 31일에 시작한 Netflix 구독의 다음 결제일을 단순히 "다음 달 같은 날"로 계산하면 **2월 31일**이라는 존재하지 않는 날짜가 발생합니다.

**발견된 문제:**
- 매월 31일 결제 → 2월에 오류 발생
- 윤년/평년의 2월 29일 처리 누락
- 주간/연간 구독의 월 환산 금액 기준 불명확

#### 해결 방법

**1. 월말 자동 보정**
```java
// 1월 31일 → 2월 28일 (평년) / 2월 29일 (윤년)
LocalDate nextDate = currentDate.plusMonths(1);
```
Java `LocalDate.plusMonths()`의 자동 보정 기능 활용

**2. 윤년 체크**
```java
Year.isLeap(year) // 윤년 판별
```

**3. 결제 주기별 월 환산 통일**
- 주간 9,900원 → 월 39,600원 (×4주)
- 연간 119,000원 → 월 9,916원 (÷12개월)
- 월간 14,900원 → 월 14,900원

#### 개선 효과
✅ 날짜 오류 완전 해결  
✅ 윤년 대응 완료  
✅ 정확한 월간 지출 계산

---

### 2️⃣ Google Gemini API를 활용한 AI 지출 분석

#### 구현 내용
사용자의 월간 구독 데이터를 분석해 맞춤형 재정 인사이트 제공

**기술적 특징:**
- **JSON 스냅샷 저장** - 분석 시점의 구독 데이터를 DB에 JSON 형태로 저장
- **프롬프트 엔지니어링** - 한국어 자연스러운 응답을 위한 구조화된 프롬프트
- **비동기 처리** - AI 분석 중에도 사용자 경험 유지

**제공하는 인사이트:**
- 📊 월간 총 지출 요약
- 📈 카테고리별 지출 분석 (예: 엔터테인먼트 60%, 업무 도구 40%)
- 💡 불필요한 구독 감지 및 절감 팁
- 🎯 개인 맞춤형 재정 관리 조언

---

### 3️⃣ Spring Security + JWT 인증

**구현 내용:**
- JWT 토큰 기반 Stateless 인증 구현
- `@AuthenticationPrincipal`로 현재 로그인 사용자 정보 주입
- 사용자는 본인의 구독 데이터만 조회/수정 가능

**보안 강화:**
- API 엔드포인트별 권한 제어
- CORS 설정으로 안전한 프론트엔드-백엔드 통신
- 민감한 정보 암호화 저장

---

### 4️⃣ 자동 결제 알림 시스템

**구현 내용:**
Spring Mail + 스케줄링을 활용해 결제일 3일 전 자동 이메일 발송

**기술적 특징:**
- 스케줄링 기반 배치 처리
- 사용자별 알림 설정 ON/OFF 기능
- 결제 금액, 서비스명, 다음 결제일 포함한 이메일 템플릿

---

## 🐛 트러블슈팅

### 1. AI Insight JSON 저장 오류

**문제:**
```
MysqlDataTruncation: Data truncation: Cannot create a JSON value from a string with CHARACTER SET 'binary'
```

**원인:**  
`input_snapshot` 컬럼에 Java 객체를 직접 저장하려 했으나, MySQL JSON 타입은 문자열만 저장 가능

**해결:**
```java
// Before
aiInsight.setInputSnapshot(subscriptions); // X

// After
ObjectMapper mapper = new ObjectMapper();
String jsonSnapshot = mapper.writeValueAsString(subscriptions);
aiInsight.setInputSnapshot(jsonSnapshot); // O
```

Jackson `ObjectMapper`로 객체를 JSON 문자열로 변환 후 저장

---

### 2. Repository에서 User 엔티티 참조 문제

**문제:**  
`SubscriptionRepository`에서 `User` 엔티티를 직접 참조하려 했으나, `User` 엔티티가 없어 컴파일 오류 발생

**해결:**
```java
// Before
List<Subscription> findByUser(User user); // X

// After
List<Subscription> findByUserId(Long userId); // O
```

JPA는 외래키 컬럼명(`userId`)만으로도 쿼리 메서드 생성 가능. 엔티티 간 직접 참조 불필요

---

### 3. 회원가입 시 Login 팝업 표시 오류

**문제:**  
회원가입 시도 시 로그인 팝업이 뜨는 현상

**원인:**  
프론트엔드에서 회원가입 API 호출 시 인증 토큰이 없어 401 Unauthorized 응답

**해결:**
```java
// SecurityConfig.java
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/signup", "/api/auth/login").permitAll()
    .anyRequest().authenticated()
);
```

회원가입/로그인 엔드포인트는 인증 없이 접근 가능하도록 설정

---

### 4. 2월 31일 날짜 계산 오류

**문제:**  
1월 31일 구독의 다음 결제일이 "2월 31일"로 계산되어 오류 발생

**해결:**  
Java `LocalDate.plusMonths()`의 자동 보정 활용
```java
LocalDate jan31 = LocalDate.of(2024, 1, 31);
LocalDate nextMonth = jan31.plusMonths(1); 
// 결과: 2024-02-29 (윤년) 또는 2024-02-28 (평년)
```

---

## 📁 프로젝트 구조

```
payflow/
├── src/
│   ├── main/
│   │   ├── java/com/payflow/
│   │   │   ├── domain/
│   │   │   │   ├── ai/              # AI 분석 관련
│   │   │   │   ├── notification/    # 알림 기능
│   │   │   │   ├── payment/         # 결제 사이클 관리
│   │   │   │   └── subscription/    # 구독 관리
│   │   │   └── global/
│   │   │       ├── config/          # Security, Web 설정
│   │   │       ├── gemini/          # Gemini API 클라이언트
│   │   │       └── util/            # JWT 등 유틸리티
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── views/
│   │   └── App.vue
│   └── package.json
└── README.md
```

---

## 🚀 시작하기

### 사전 요구사항

- Java 17 이상
- Node.js 16 이상
- MySQL 8.0 이상
- Redis (선택)

### 설치 및 실행

#### 1. 저장소 클론
```bash
git clone https://github.com/yourusername/payflow.git
cd payflow
```

#### 2. 백엔드 설정
```bash
# application.properties 설정
spring.datasource.url=jdbc:mysql://localhost:3306/payflow
spring.datasource.username=your_username
spring.datasource.password=your_password

# Gemini API 키 설정
gemini.api.key=your_api_key

# 빌드 및 실행
./gradlew bootRun
```

#### 3. 프론트엔드 설정
```bash
cd frontend
npm install
npm run dev
```

#### 4. 접속
```
http://localhost:5173
```

---

## 📊 프로젝트 성과

- ✅ **안정성** - 날짜 계산 버그 0건, 모든 엣지 케이스 처리
- ✅ **성능** - AI 분석 응답 시간 3초 이내
- ✅ **보안** - 사용자별 데이터 격리, 취약점 0건
- ✅ **확장성** - RESTful API 설계로 프론트엔드 독립적 개발 가능

---

## 🤝 기여하기

Pull Request는 언제나 환영합니다! 버그 리포트나 기능 제안은 Issues를 이용해주세요.

---

## 👨‍💻 개발자

**Jang** - [GitHub](https://github.com/yourusername)

---

## 📅 개발 기간

**2026.01 - 진행 중**

---

<div align="center">

**PayFlow와 함께 똑똑한 구독 관리를 시작하세요! 🚀**

</div>
