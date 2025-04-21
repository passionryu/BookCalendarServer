## 📌 About Us
프로젝트 명 : Gachon Univ. 2025-1 CapStone Design - AI Project   
팀 이름 : AI Vengers  
- User Server Backend Developer(DevOps) / Team Leader ( 류성열 )
- User Mobile APP Developer ( 박우현 )
- Admin WEB Developer ( 김광수 )
- AI Developer ( 안서현, 이정현 )

## 📱 About Our App
앱 이름 : Book Calendar  

🎯해결하려는 문제 : 우리 나라의 과도한 IT문화의 발달로 인해, 쇠퇴한 성인 독서율 문제에 대해 해결책을 간구한다.  
특히 본 서비스는 독서 동기부여 및 독서 습관 형성을 위한 AI 서비스 및 커뮤니티 기능을 제공하여 해당 문제를 해결하고자 한다.  

### 📖 1. 독후감 나눠 쓰기 기능 
우리는 흔히 독후감이라 하면, 책을 완독 한 후 한번만 기록하는 것으로 알고 있다.  
그러나 우리 BoolCalendar앱에서는 한 권의 도서에 대해 여러번 독후감을 나눠서 작성할 수 있으며,   
각 독서 기록은 캘린더에 추적되며, 매 독서 활동 마다 AI 서비스들(내 독후감을 기반으로 한 질문지 작성 & AI 사서의 독후감 리뷰)이 적용된다. 

### 🗣️ 2. 도서 커뮤니티 
게시판 형식의 도서 커뮤니티를 지원한다.  
유저들은 챌린지를 통해 메달을 획득, 본인의 랭킹을 올릴 수 있다.   
본인의 메달 및 랭킹은 도서 커뮤니티에서 활동을 할 떄, 본인 사진 옆에 자동으로 표시되어 동기부여 효과를 기대한다. 

### 🤖 3. AI 서비스 
1. 유저 맞춤형 질문지 생성 서비스-> 유저의 당일 독후감을 스캔하여 3개의 질문지를 맞춤형 생성
2. AI BookCalendar 사서의 독후감 리뷰 -> 유저의 당일 독후감과 질문지 답변 기록을 AI가 스캔하여 리뷰 및 격려 메시지를 반환
3. AI BookCalendar 사서의 챗봇 서비스 -> 유저와의 챗봇 서비스를 통해, 유저의 니즈를 파악하여 맞춤형 추천 도서 5가지를 추천이유와 함께 반환

## 🙋‍♂️ About Me _ in this project
### 🧭 TeamLeading
* 매주 2회 팀 회의 리딩 (15주)
* 매주 팀장 발표 전담 (15주)
* 서비스 기획 및 와이어프레임 제작(Figma)
### ⚙️Backend 
* Springboot 3.3.6 (Java 17)
* JPA - 단순 CRUD 서비스 구현
* MyBatis - 복잡한 쿼리(JOIN,서브쿼리 등), 성능 최적화가 필요한 서비스 구현
* Spring AI - GPT api 연결
* JWT, Spring Security - Refresh Token Rotation(RTR방식)을 통한 높은 보안의 인증&인가 시스템 구축
* WebClient - Fast-API AI 서버와 통신 
### 🚀DevOps 
* 시스템 아키텍쳐 설계 : Gachon univ. Onpremise server 
* CI/CD 파이프 라인 구축 : Github Actions
* 컨테이너 관리  : Podman, Podman-compose
* 모니터링/로깅 시스템 구축 : Prometheus, Grafana
### 🛢️DB  (MariaDB)
* DB 설계
* Container Redis(Port:6381) - Session Redis
* Server Redis(Port:6382) - Cahcing Redis 
* INDEX - 쿼리 기능 최적화 
* ON DELETE CASECADE 
### 🤝Collaboration
* Figma - wireframe  
* Swagger - for FE & BE 
* Notion, Discord -for Team 
---

### 🧪Swagger UI
학과 서버 swagger 입장 주소 
```
http://ceprj.gachon.ac.kr:60001/api/swagger-ui/index.html
```
로컬 서버 swagger입장 주소 
```
http://localhost:8081/swagger-ui/index.html
```

### 🧱 Infra Structure

![image](https://github.com/user-attachments/assets/88ed10de-ca05-4ba2-bff5-a9178057abeb)


위 인프라 구조도는, 가천대학교 학과 서버를 기준으로 설계되었다.  
내가 담당하고 있는 부분은 좌측 하단에 8개의 컨테이너가 띄어져 있는 유저 서버 부분이며,   
Github Actions를 통해 학과서버에 CI/CD를 진행한다.  


학과 서버에서는 Docker를 사용할 수 없으며, 그나마 비슷한 Podman을 활용할 수 있다한다.   
Podman으로 총 8개의 컨테이너를 구현하여 서버 구축, 리버스 프록시, DB 구축, 메트릭 수집&모니터링을 진행한다.

그리고 Fast-API에 있는 4가지의 AI 모델을 사용하기 위해, Spring 프레임워크에 내장되어 있는 Http Client인 WebClinet를 활용하여 Fast-API에 요청을 보내고,
Fast-API 앞단에 Router를 배치하여 요청에 맞는 알맞는 AI 모델로 라우팅하여 반환값을 다시 Spring 서버로 받아온다.  

외부 API는 GPT-turbo3.5 와 알라딘 API를 활용하며, 
Redis는 학과서버에 설치되어 있는 Redis와 유저 서버 내부에 컨테이너를 활용해 하나의 Redis를 추가하며,  
- 6381포트 Redis Container = 세션 및 기능 구현 Redis (Read & Write)  
- 6382포트 Redis Container = 캐싱용 Redis (Only Read)   

로 역할을 나눠서 구현한다.   

컨테이너는 기본적으로 메모리를 제한하지 않으면 무제한으로 자원을 소비할 수 있기에 8개의 각 컨테이너는 compose 파일에서 최대 메모리를 제한하고,   
특히 Spring Boot 서버의 경우 최초 시작 메모리와 최대 메모리를 동일하게 설정하여 동적 메모리 할당을 방지하여 성능 향상을 유도한다.

> 설계자 : 류성열
### 🤖자동화 파일 Automation Code
1. rebuild_springboot.sh
> springboot 코드를 수정한 후, 모든 컨테이너 중지 -> jar file rebuild -> image rebuild ->  모든 컨테이너 재시작
``` 
bash rebuild_springboot.sh
```
2.  check-logs.sh
> redis-session, redis-exporter-session, redis-cache, redis-exporter-cache의 현재 로그 모두 조회 후 최종적으로 springboot 로그 실시간 조회
```
bash check_logs.sh  
```

### 🧠 역할과 책임에 따른 Redis의 분할

#### 1. Redis-Cache
안정성과 보안성 보다는 빠른 데이터 반응속도를 염두에 두고 설계

#### 2. Redis-Session
유실되면 안되는 데이터와 민감한 데이터가 입력될 수 있으므로 안정성과 보안성을 염두에 두고 설계

#### 3. 각 저장소 접근 방식 
redis-cache 접근 
```
podman exec -it redis-cache redis-cli
```
redis-session 접근
```
podman exec -it redis-session redis-cli
```
비밀번호 입력 
```
auth (비밀번호 입력)
```

### 🌐 Nginx Strategy 

### 🧬 DB Structure

![Copy of Copy of BookCalendar (4)](https://github.com/user-attachments/assets/c782ce1b-4f56-40aa-9cbb-8bc5d51fb862)

DB 구조도는 ERD Cloud 서비스로 제작하였고,  
파란색 선은 식별 관계, 분홍색 선은 비식별 관계를 의미한다. 
그리고 서비스 안에서 쿼리의 성능을 올리기 위해, 적절히 인덱싱을 설정하였다.  
위 DB는 우선 유저 서버를 위한 DB만 표현하고 있다.  
> 제작자 : 류성열


### 🧾 Wire Frame  
Link : https://www.figma.com/design/ndspvub92U64eh9J2MDZSV/Untitled?node-id=0-1&p=f&t=GYPT6faNrPDJDjhF-0
![image](https://github.com/user-attachments/assets/d10e1946-0ff0-40ca-81f1-df5589b581c0)

와이어 프레임은 피그마로 제작하였다. 
> 제작자 : 류성열


### 깃모지
> 가독성 높은 Commit을 기록한다.  

| 아이콘 | 타이틀 | 설명 | 원문 |
| --- | --- | --- | --- |
| ✨ | [기능 추가] | 새 기능 | Introduce new features. |
| 💡 | [주석 추가] | 주석 추가/수정 | Add or update comments in source code. |
| 🔥 | [코드 제거] | 코드/파일 삭제 | Remove code or files. |
| 🔀 | [브랜치 병합] | 브랜치 합병 | Merge branches. |
| ♻️ | [리팩토링] | 코드 리팩토링 | Refactor code. |
| 🎨 | [구조 개선] | 코드의 구조/형태 개선 | Improve structure / format of the code. |
| ⚡️ | [성능 향상] | 성능 개선 | Improve performance. |
| 🐛 | [버그 수정] | 버그 수정 | Fix a bug. |
| 🚑 | [긴급 수정] | 긴급 수정 | Critical hotfix. |
| 📝 | [문서화] | 문서 추가/수정 | Add or update documentation. |
| 💄 | [UI 업데이트] | UI/스타일 파일 추가/수정 | Add or update the UI and style files. |
| 🎉 | [프로젝트 시작] | 프로젝트 시작 | Begin a project. |
| ✅ | [테스트] | 테스트 추가/수정 | Add or update tests. |
| 🔒 | [보안 수정] | 보안 이슈 수정 | Fix security issues. |
| 🔖 | [릴리즈 태그] | 릴리즈/버전 태그 | Release / Version tags. |
| 💚 | [CI 수정] | CI 빌드 수정 | Fix CI Build. |
| 📌 | [의존성 고정] | 특정 버전 의존성 고정 | Pin dependencies to specific versions. |
| 👷 | [CI 구성] | CI 빌드 시스템 추가/수정 | Add or update CI build system. |
| 📈 | [분석 추가] | 분석, 추적 코드 추가/수정 | Add or update analytics or track code. |
| ➕ | [의존성 추가] | 의존성 추가 | Add a dependency. |
| ➖ | [의존성 제거] | 의존성 제거 | Remove a dependency. |
| 🔧 | [구성 변경] | 구성 파일 추가/삭제 | Add or update configuration files. |
| 🔨 | [스크립트 작업] | 개발 스크립트 추가/수정 | Add or update development scripts. |
| 🌐 | [국제화] | 국제화/현지화 | Internationalization and localization. |
| 💩 | [코드 개선 필요] | 똥싼 코드 | Write bad code that needs to be improved. |
| ⏪ | [롤백] | 변경 내용 되돌리기 | Revert changes. |
| 📦 | [패키지 작업] | 컴파일된 파일 추가/수정 | Add or update compiled files or packages. |
| 👽 | [API 수정] | 외부 API 변화로 인한 수정 | Update code due to external API changes. |
| 🚚 | [리소스 이동] | 리소스 이동, 이름 변경 | Move or rename resources. |
| 📄 | [라이센스] | 라이센스 추가/수정 | Add or update license. |
| 🗃 | [DB 변경] | 데이터베이스 관련 수정 | Perform database related changes. |
| 🔊 | [로그 업데이트] | 로그 추가/수정 | Add or update logs. |
| 🙈 | [.gitignore] | .gitignore 추가/수정 | Add or update a .gitignore file. |
