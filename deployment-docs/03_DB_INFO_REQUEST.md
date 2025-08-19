# 📋 **DB 담당자에게 요청할 Oracle DB 정보**

## 🗄️ **Team-FOG MSA 프로젝트 Oracle DB 연결 정보 요청**

안녕하세요! Team-FOG MSA 프로젝트에서 EC2에 설치된 Oracle DB에 연결하기 위해 다음 정보가 필요합니다.

### **📝 요청 정보**

#### **필수 정보**
```bash
# EC2 인스턴스 정보
EC2_PRIVATE_IP=10.0.x.x
EC2_SECURITY_GROUP_ID=sg-xxxxxxxxx

# Oracle DB 정보
ORACLE_PORT=1521
ORACLE_SID=ORCL
DB_USERNAME=teamfog_user
DB_PASSWORD=your_secure_password
```

### **🔧 필요한 작업**

#### **1. 보안 그룹 설정**
ECS 서비스에서 Oracle DB에 접근할 수 있도록 보안 그룹을 설정해주세요:

```bash
# ECS 보안 그룹에서 Oracle DB 포트 접근 허용
aws ec2 authorize-security-group-ingress \
    --group-id sg-xxxxxxxxx \
    --protocol tcp \
    --port 1521 \
    --source-group sg-xxxxxxxxx \
    --description "Oracle DB access from ECS tasks"
```

#### **2. Oracle DB 사용자 계정 생성**
```sql
-- Oracle DB에서 실행
CREATE USER teamfog_user IDENTIFIED BY your_secure_password;
GRANT CONNECT, RESOURCE TO teamfog_user;
GRANT CREATE SESSION TO teamfog_user;
GRANT UNLIMITED TABLESPACE TO teamfog_user;
GRANT CREATE TABLE TO teamfog_user;
GRANT CREATE SEQUENCE TO teamfog_user;
```

### **📋 응답 형식**

다음 형식으로 정보를 제공해주세요:

```yaml
ec2_private_ip: "10.0.x.x"
oracle_port: "1521"
oracle_sid: "ORCL"
db_username: "teamfog_user"
db_password: "your_secure_password"
security_group_id: "sg-xxxxxxxxx"
```

---

**감사합니다!** ��

**Team-FOG 개발팀**
