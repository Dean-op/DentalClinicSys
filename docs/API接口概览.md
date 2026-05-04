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
- `GET /api/patient/doctors/{id}`
- `GET /api/patient/medicines`
- `POST /api/patient/cart`
- `GET /api/patient/cart`
- `DELETE /api/patient/cart/{id}`
- `POST /api/patient/orders`
- `GET /api/patient/orders`
- `PUT /api/patient/orders/{id}/refund`
- `PUT /api/patient/orders/{id}/delivery`
- `POST /api/patient/appointments`
- `GET /api/patient/appointments`
- `PUT /api/patient/appointments/{id}/cancel`
- `PUT /api/patient/appointments/{id}/reschedule`
- `GET /api/patient/records`
- `POST /api/patient/ai/consult`
- `GET /api/patient/reminders`
- `GET /api/patient/reminders/alerts`
- `POST /api/patient/messages`
- `GET /api/patient/messages`
- `POST /api/patient/reviews`

## 医生端

- `GET /api/doctor/profile`
- `GET /api/doctor/announcements`
- `PUT /api/doctor/profile`
- `GET /api/doctor/schedules`
- `POST /api/doctor/schedules`
- `PUT /api/doctor/schedules/{id}`
- `DELETE /api/doctor/schedules/{id}`
- `GET /api/doctor/appointments`
- `PUT /api/doctor/appointments/{id}`
- `POST /api/doctor/records`
- `GET /api/doctor/records`
- `GET /api/doctor/records/{id}`
- `GET /api/doctor/patients/{patientId}/records`
- `POST /api/doctor/prescriptions`
- `GET /api/doctor/medicines`
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
- `GET /api/admin/qualifications`
- `GET /api/admin/schedules`
- `POST /api/admin/schedules`
- `PUT /api/admin/schedules/{id}`
- `DELETE /api/admin/schedules/{id}`
- `GET /api/admin/medicines`
- `POST /api/admin/medicines`
- `PUT /api/admin/medicines/{id}`
- `DELETE /api/admin/medicines/{id}`
- `GET /api/admin/announcements`
- `POST /api/admin/announcements`
- `PUT /api/admin/announcements/{id}`
- `DELETE /api/admin/announcements/{id}`
- `GET /api/admin/records`
- `GET /api/admin/prescriptions`
- `GET /api/admin/orders`
- `PUT /api/admin/orders/{id}/status`
- `GET /api/admin/appointments`
- `GET /api/admin/stats`
- `GET /api/admin/logs`

## 公共接口

- `POST /api/files/upload`
