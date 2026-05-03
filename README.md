# Dental Clinic Management System

毕业设计版口腔诊所管理系统，包含患者端、医生端、管理员端三类角色。

## Tech Stack

- Frontend: Vue 3, Vite, TypeScript, Pinia, Vue Router, Element Plus, Axios
- Backend: Spring Boot 3, Java 17, Spring Security JWT, MyBatis-Plus, MySQL 8
- AI: OpenAI-compatible external API, configured from the local MySQL `ai_config` table

## Default Accounts

| Role | Username | Password |
| --- | --- | --- |
| Admin | `admin` | `123456` |
| Doctor | `doctor_chen` | `123456` |
| Patient | `patient_li` | `123456` |

## Startup

1. Create the database:

   ```sql
   CREATE DATABASE dental_clinic DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. Update MySQL username/password in `backend/src/main/resources/application.yml` if needed.

3. Start backend:

   ```powershell
   cd backend
   .\mvnw.cmd spring-boot:run
   ```

   AI dentist uses an OpenAI-compatible external API. Store the real API info in local MySQL, not in source files:

   ```sql
   UPDATE ai_config
   SET base_url = 'https://api.openai.com/v1',
       api_key = 'your_api_key',
       model = 'gpt-4o-mini',
       enabled = 1,
       updated_at = NOW()
   WHERE id = 1;
   ```

   `application.yml` only keeps non-secret runtime settings such as timeout.

4. Start frontend:

   ```powershell
   cd frontend
   npm install
   npm run dev
   ```

Open the frontend URL printed by Vite. API requests are proxied to `http://localhost:8080`.

## Notes

- The first version is intended for graduation-project demonstration and does not integrate real payment, SMS, logistics, insurance, or medical compliance systems.
- AI consultation calls an external OpenAI-compatible `/chat/completions` API. Its output is only preliminary reference and cannot replace a doctor's diagnosis.
