## About US
Project : Gachon Univ. 2025-1 CapStone Design - AI Project   
Application Name : Book Calendar    
Team : AI Vengers  
- User Server Backend Developer / Team Leader ( 류성열 )
- User Mobile APP Developer ( 박우현 )
- Admin WEB Developer ( 김광수 )
- AI Developer ( 안서현, 이정현 )



### Pain Point
SpringBoot프레임워크(Java)로 서버를 구현한다.   
서버가 배포될 환경은 가천대학교 컴퓨터 공학과 학생들이 사용가능한 온프레미스 서버이며,
70명 가까이 되는 학생들이 메모리 소비량이 많은 AI프로젝트를 128GB로 나눠서 사용해야 하므로 용량은 턱없이 부족하다.    
서버의 Redis는 2GB밖에 되지 않으므로, 고가용성이 보장되지 않는다.  

### Swagger UI
```
http://localhost:8080/swagger-ui/index.html
```

### 디렉토리 구조  
```
📦 BookCalendar 
├── 📂 springboot            # Spring Boot 애플리케이션 
│   ├── 📂 src
│   ├── 📂 target
│   ├── 📄 Dockerfile     # Spring Boot 컨테이너용 Dockerfile
│   ├── 📄 pom.xml
│   └── ...
├── 📂 nginx              # Nginx 설정
│   ├── 📄 Dockerfile     # Nginx 컨테이너용 Dockerfile
│   ├── 📄 nginx.conf
│   └── ...
├── 📂 redis              # Redis 설정
│   ├── 📄 Dockerfile     # Redis 컨테이너용 Dockerfile
│   ├── 📄 redis.conf
│   └── ...
├── 📂 grafana            # Grafana 설정
│   ├── 📄 Dockerfile     # Grafana 컨테이너용 Dockerfile
│   ├── 📄 grafana.ini
│   └── ...
├── 📂 prometheus         # Prometheus 설정
│   ├── 📄 Dockerfile     # Prometheus 컨테이너용 Dockerfile
│   ├── 📄 prometheus.yml
│   └── ...
├── 📂 .github            # GitHub Actions 설정
│   ├── 📂 workflows
│   │   ├── 📄 ci-cd.yaml
│   └── ...
├── 📄 podman-compose.yaml # Podman Compose 설정 파일
└── 📄 README.md          # 프로젝트 설명
```
### Infra Structure

![image](https://github.com/user-attachments/assets/acff6ff0-c5d5-4ba3-a716-e0dff7fded3a)
위 인프라 구조도는, 가천대학교 학과 서버를 기준으로 설계되었다.  
내가 담당하고 있는 부분은 좌측 하단에 5개의 컨테이너가 띄어져 있는 유저 서버 부분이며,   
Github Actions를 통해 학과서버에 CI/CD를 진행한다.  


학과 서버에서는 Docker를 사용할 수 없으며, 그나마 비슷한 Podman을 활용할 수 있다한다.   
Podman으로 총 5개의 컨테이너를 구현하여 서버 구축, 프록시, Nosql 활용, 메트릭 수집&모니터링을 진행한다.

그리고 Fast-API에 있는 4가지의 AI 모델을 사용하기 위해, Spring 프레임워크에 내장되어 있는 Http Client인 WebClinet를 활용하여 Fast-API에 요청을 보내고,
Fast-API 앞단에 Router를 배치하여 요청에 맞는 알맞는 AI 모델로 라우팅하여 반환값을 다시 Spring 서버로 받아온다.  

외부 API는 GPT-turbo3.5 와 알라딘 API를 활용하며, 
Redis는 학과서버에 설치되어 있는 Redis와 유저 서버 내부에 컨테이너를 활용해 하나의 Redis를 추가하며,  
- 학과서버 Redis = 캐싱용 Redis (Only Read)   
- 컨테이너 Redis = 상태관리 및 서비스 구현용 Redis (Read & Write)

로 역할을 나눠서 구현한다.   
학과 서버 Redis 를 캐싱용 Reids로 정한 이유는 컨테이너 Redis와 학과 서버 Redis 중 응답속도 성능이 더 좋은 것은 컨테이너 Redis 보다는 학과 서버 Redis이기 때문이다.  

컨테이너는 기본적으로 메모리를 제한하지 않으면 무제한으로 자원을 소비할 수 있기에 5개의 각 컨테이너는 compose 파일에서 최대 메모리를 제한하고,   
특히 Spring Boot 서버의 경우 최초 시작 메모리와 최대 메모리를 동일하게 설정하여 동적 메모리 할당을 방지하여 성능 향상을 유도한다.

> 설계자 : 류성열

### Caching Strategy
캐싱 전략 

### Nginx Strategy 
엔진 X 전략 

### DB Structure

![Copy of Copy of BookCalendar (4)](https://github.com/user-attachments/assets/c782ce1b-4f56-40aa-9cbb-8bc5d51fb862)

DB 구조도는 ERD Cloud 서비스로 제작하였고,  
파란색 선은 식별 관계, 분홍색 선은 비식별 관계를 의미한다. 
그리고 서비스 안에서 쿼리의 성능을 올리기 위해, 적절히 인덱싱을 설정하였다.  
위 DB는 우선 유저 서버를 위한 DB만 표현하고 있다.  
> 제작자 : 류성열


### Wire Frame  
Link : https://www.figma.com/design/ndspvub92U64eh9J2MDZSV/Untitled?node-id=0-1&p=f&t=GYPT6faNrPDJDjhF-0
![image](https://github.com/user-attachments/assets/d10e1946-0ff0-40ca-81f1-df5589b581c0)

와이어 프레임은 피그마로 제작하였다. 
> 제작자 : 류성열


### 깃모지

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
