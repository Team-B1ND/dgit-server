# DGit Server 배포 가이드

## 목차
1. [사전 준비](#사전-준비)
2. [로컬에서 이미지 빌드](#로컬에서-이미지-빌드)
3. [우분투 서버로 이미지 전송](#우분투-서버로-이미지-전송)
4. [서버에서 컨테이너 실행](#서버에서-컨테이너-실행)
5. [문제 해결](#문제-해결)

---

## 사전 준비

### 1. 환경 변수 설정
프로젝트 루트에 `.env` 파일이 있는지 확인하고, 다음 환경 변수들이 설정되어 있는지 확인하세요:

```bash
# 데이터베이스
DATABASE_URL=jdbc:mysql://호스트:3306/데이터베이스명
DATABASE_USERNAME=사용자명
DATABASE_PASSWORD=비밀번호

# DAuth
DAUTH_CLIENT_ID=your_client_id
DAUTH_CLIENT_SECRET=your_client_secret

# JWT
JWT_SECRET_KEY=your_jwt_secret_key

# GitHub
GITHUB_TOKEN=your_github_token
```

### 2. 필요한 도구 확인
- Docker: `docker --version`
- Docker Compose: `docker compose version`

---

## 로컬에서 이미지 빌드

### 방법 1: Docker Compose 사용 (권장)

```bash
# 프로젝트 디렉토리로 이동
cd /Users/mingyupark/Desktop/Dev/dgit-server

# ARM64 플랫폼용 이미지 빌드
docker compose build --no-cache

# 빌드 확인
docker images | grep dgit-server-v2
```

### 방법 2: Docker 직접 사용

```bash
# ARM64 플랫폼용 이미지 빌드
docker build --platform linux/arm64 -t dgit-server-v2:latest .

# 빌드 확인
docker images | grep dgit-server-v2
```

---

## 우분투 서버로 이미지 전송

### 방법 1: Docker Save/Load (네트워크가 느린 경우)

#### 맥북에서 이미지 저장
```bash
# 이미지를 tar 파일로 저장
docker save dgit-server-v2:latest | gzip > dgit-server-v2.tar.gz

# 파일 크기 확인
ls -lh dgit-server-v2.tar.gz
```

#### 서버로 전송
```bash
# SCP로 전송 (서버 주소와 경로는 실제 환경에 맞게 수정)
scp dgit-server-v2.tar.gz user@server-ip:/home/user/

# 또는 rsync 사용
rsync -avz --progress dgit-server-v2.tar.gz user@server-ip:/home/user/
```

#### 우분투 서버에서 이미지 로드
```bash
# 서버에 SSH 접속
ssh user@server-ip

# 이미지 로드
gunzip -c dgit-server-v2.tar.gz | docker load

# 로드 확인
docker images | grep dgit-server-v2

# tar 파일 삭제 (선택사항)
rm dgit-server-v2.tar.gz
```

### 방법 2: Docker Registry 사용 (Docker Hub 또는 Private Registry)

#### Docker Hub 사용
```bash
# 맥북에서 로그인
docker login

# 이미지에 태그 추가 (username을 본인의 Docker Hub username으로 변경)
docker tag dgit-server-v2:latest username/dgit-server-v2:latest

# 이미지 푸시
docker push username/dgit-server-v2:latest

# 우분투 서버에서 풀
ssh user@server-ip
docker pull username/dgit-server-v2:latest
docker tag username/dgit-server-v2:latest dgit-server-v2:latest
```

---

## 서버에서 컨테이너 실행

### 1. 프로젝트 파일 전송
서버에 `docker-compose.yml`과 `.env` 파일을 전송합니다:

```bash
# 맥북에서 실행
# 서버에 디렉토리 생성
ssh user@server-ip "mkdir -p ~/dgit-server"

# 필요한 파일들 전송
scp docker-compose.yml .env user@server-ip:~/dgit-server/
```

### 2. 기존 네트워크 확인 (선택사항)
15개의 기존 컨테이너와 같은 네트워크를 사용하려면:

```bash
# 서버에서 실행
# 기존 네트워크 목록 확인
docker network ls

# 특정 컨테이너가 사용하는 네트워크 확인
docker inspect 컨테이너명 | grep NetworkMode
```

기존 네트워크를 사용하려면 `docker-compose.yml`의 networks 섹션을 수정:
```yaml
networks:
  dgit-network:
    external: true
    name: 기존_네트워크_이름  # 예: my-network
```

### 3. 포트 충돌 확인
```bash
# 서버에서 8080 포트가 사용 중인지 확인
sudo lsof -i :8080

# 또는
sudo netstat -tulpn | grep 8080
```

포트가 사용 중이면 `docker-compose.yml`에서 포트를 변경:
```yaml
ports:
  - "8081:8080"  # 호스트 포트를 8081로 변경
```

### 4. 컨테이너 실행
```bash
# 서버에서 실행
cd ~/dgit-server

# 백그라운드로 컨테이너 실행
docker compose up -d

# 로그 확인
docker compose logs -f dgit-server-v2

# 컨테이너 상태 확인
docker compose ps
```

### 5. 동작 확인
```bash
# 헬스체크 (Actuator가 활성화되어 있는 경우)
curl http://localhost:8080/actuator/health

# Swagger UI 접속 (브라우저에서)
# http://서버IP:8080/swagger-ui.html

# 컨테이너 로그 실시간 확인
docker logs -f dgit-server-v2
```

---

## 컨테이너 관리 명령어

### 시작/중지/재시작
```bash
# 시작
docker compose up -d

# 중지
docker compose stop

# 재시작
docker compose restart

# 중지 및 삭제
docker compose down

# 중지, 삭제 및 볼륨도 삭제
docker compose down -v
```

### 로그 확인
```bash
# 전체 로그 확인
docker compose logs

# 실시간 로그 확인
docker compose logs -f

# 최근 100줄만 확인
docker compose logs --tail=100

# 특정 서비스만
docker compose logs dgit-server-v2
```

### 컨테이너 내부 접속
```bash
# Bash 쉘로 접속
docker compose exec dgit-server-v2 bash

# 또는
docker exec -it dgit-server-v2 bash
```

---

## 문제 해결

### 1. 컨테이너가 계속 재시작되는 경우
```bash
# 로그 확인
docker compose logs dgit-server-v2

# 일반적인 원인:
# - 환경 변수 누락
# - 데이터베이스 연결 실패
# - 포트 충돌
```

### 2. 데이터베이스 연결 실패
```bash
# MySQL 컨테이너가 같은 네트워크에 있는지 확인
docker network inspect dgit-network

# DATABASE_URL 형식 확인
# jdbc:mysql://mysql-container-name:3306/dbname
# (호스트명이 컨테이너명이어야 함)
```

### 3. 메모리 부족
`docker-compose.yml`에 리소스 제한 추가:
```yaml
services:
  dgit-server-v2:
    # ...
    deploy:
      resources:
        limits:
          memory: 1G
        reservations:
          memory: 512M
```

### 4. 이미지 다시 빌드
```bash
# 캐시 없이 다시 빌드
docker compose build --no-cache

# 실행 중인 컨테이너 중지 후 새 이미지로 재시작
docker compose down
docker compose up -d
```

### 5. 전체 재배포
```bash
# 맥북에서
docker save dgit-server-v2:latest | gzip > dgit-server-v2.tar.gz
scp dgit-server-v2.tar.gz user@server-ip:~/

# 서버에서
docker compose down
gunzip -c ~/dgit-server-v2.tar.gz | docker load
docker compose up -d
```

---

## 자동 업데이트 스크립트 (선택사항)

서버에 업데이트 스크립트 생성:

```bash
# ~/dgit-server/update.sh
#!/bin/bash

echo "Stopping container..."
docker compose down

echo "Loading new image..."
gunzip -c ~/dgit-server-v2.tar.gz | docker load

echo "Starting container..."
docker compose up -d

echo "Checking status..."
docker compose ps

echo "Showing recent logs..."
docker compose logs --tail=50 dgit-server-v2
```

실행 권한 부여:
```bash
chmod +x ~/dgit-server/update.sh
```

사용:
```bash
# 맥북에서 새 이미지 전송 후
ssh user@server-ip "cd ~/dgit-server && ./update.sh"
```

---

## 모니터링 팁

### 리소스 사용량 확인
```bash
# 실시간 리소스 모니터링
docker stats dgit-server-v2

# 디스크 사용량
docker system df
```

### 로그 로테이션
`docker-compose.yml`에 이미 로그 로테이션이 설정되어 있습니다:
- 최대 파일 크기: 10MB
- 최대 파일 개수: 3개

---

## 참고사항

1. **아키텍처 호환성**: M2 맥북(ARM64)과 우분투 aarch64(ARM64)는 동일한 아키텍처이므로 크로스 빌드가 필요 없습니다.

2. **보안**: `.env` 파일에 민감한 정보가 포함되어 있으므로 절대 Git에 커밋하지 마세요.

3. **백업**: 중요한 데이터는 정기적으로 백업하세요.

4. **업데이트**: 애플리케이션 업데이트 시 데이터베이스 마이그레이션을 먼저 확인하세요.
