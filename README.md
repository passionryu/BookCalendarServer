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

### 🧪Swagger UI
학과 서버 swagger 입장 주소 
```
http://ceprj.gachon.ac.kr:60001/api/swagger-ui/index.html
```
로컬 서버 swagger입장 주소 
```
http://localhost:60001/swagger-ui/index.html
```

### 🧱 Infra Structure

![arch](https://github.com/user-attachments/assets/6659213b-0e42-481c-908d-8b08647e8ce8)

> 설계자 : 류성열

### 🧬 DB Structure

![BookCalendar](https://github.com/user-attachments/assets/3baff7b3-c9c5-4801-99e7-2f24b822ea4d)

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
