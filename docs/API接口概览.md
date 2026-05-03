# API接口概览

所有接口返回统一结构：

```json
{
  "success": true,
  "message": "ok",
  "data": {}
}
```

除 `/api/auth/**` 外，其余接口需要请求头：

```text
Authorization: Bearer <token>
```

## 认证

- `POST /api/auth/login`
- `POST /api/auth/register`
- `GET /api/me`

## 患者端

- `GET /api/patient/profile`
- `PUT /api/patient/profile`
- `GET /api/patient/announcements`
- `GET /api/patient/doctors`
- `GET /api/patient/medicines`
- `POST /api/patient/orders`
- `GET /api/patient/orders`
- `PUT /api/patient/orders/{id}/refund`
- `POST /api/patient/appointments`
- `GET /api/patient/appointments`
- `PUT /api/patient/appointments/{id}/cancel`
- `GET /api/patient/records`
- `POST /api/patient/ai/consult`
- `GET /api/patient/reminders`

## 医生端

- `GET /api/doctor/profile`
- `PUT /api/doctor/profile`
- `GET /api/doctor/schedules`
- `POST /api/doctor/schedules`
- `GET /api/doctor/appointments`
- `PUT /api/doctor/appointments/{id}`
- `POST /api/doctor/records`
- `POST /api/doctor/prescriptions`
- `GET /api/doctor/messages`
- `PUT /api/doctor/messages/{id}/reply`
- `GET /api/doctor/stats`

## 管理员端

- `GET /api/admin/users`
- `POST /api/admin/users`
- `PUT /api/admin/users/{id}/status`
- `PUT /api/admin/users/{id}/password`
- `GET /api/admin/doctors`
- `PUT /api/admin/doctors/{id}/review`
- `GET /api/admin/medicines`
- `POST /api/admin/medicines`
- `PUT /api/admin/medicines/{id}`
- `GET /api/admin/announcements`
- `POST /api/admin/announcements`
- `PUT /api/admin/announcements/{id}`
- `GET /api/admin/records`
- `GET /api/admin/prescriptions`
- `GET /api/admin/orders`
- `PUT /api/admin/orders/{id}/status`
- `GET /api/admin/appointments`
- `GET /api/admin/stats`
- `GET /api/admin/logs`
