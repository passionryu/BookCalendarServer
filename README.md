### BookCalendar AI 어플리케이션 서버 구현   
SpringBoot프레임워크(Java)로 서버를 구현한다.   
서버가 배포될 환경은 가천대학교 컴퓨터 공학과 학생들이 사용가능한 온프레미스 서버이며,
70명 가까이 되는 학생들이 128GB로 나눠서 사용해야 하므로 용량은 턱없이 부족하다.  

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
학과 서버에서는 Docker를 사용할 수 없으며, 그나마 비슷한 Podman을 활용할 수 있다한다.   
Podman으로 총 5개의 컨테이너를 구현하여 서버 구축, 프록시, Nosql 활용, 메트릭 수집&모니터링을 진행한다.

### DB Structure

![image](https://github.com/user-attachments/assets/5d0b6f20-ab53-40c4-8aac-f2c52c864a6d)

### Wire Frame  
Link : https://www.figma.com/design/ndspvub92U64eh9J2MDZSV/Untitled?node-id=0-1&p=f&t=GYPT6faNrPDJDjhF-0
![image](https://github.com/user-attachments/assets/d10e1946-0ff0-40ca-81f1-df5589b581c0)




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
