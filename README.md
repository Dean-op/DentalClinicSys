# Dental Clinic Management System

毕业设计版口腔诊所管理系统，包含患者端、医生端、管理员端三类角色。

## 技术栈

- 前端：Vue 3、Vite、TypeScript、Pinia、Vue Router、Element Plus、Axios
- 后端：Spring Boot 3、Java 17、Spring Security JWT、MyBatis-Plus、MySQL 8
- AI：OpenAI 兼容外部接口，配置保存在本地 MySQL 的 `ai_config` 表

## 当前功能

- 患者端：公告浏览、医生查询、药品查看/购买、在线预约、预约管理、订单管理、病例查看、留言管理、AI 牙医、用药提醒
- 医生端：公告查看、排班维护、预约审核、待接诊、接诊与病例维护、电子药单、病例管理、留言回复、个人统计
- 管理员端：用户管理、医生审核、排班管理、药品管理、公告管理、病例监督、处方监督、订单管理、预约管理、统计分析、系统日志


## 新环境启动

直接使用脚本目录下的启动脚本：

```powershell
.\scripts\bootstrap-dev\bootstrap-dev.cmd
```

这套脚本会按以下顺序执行：

1. 检查 `Java`、`Node.js`、`npm`、`mysql` 命令是否可用
2. 自动创建 `dental_clinic` 数据库
3. 导入 `backend/src/main/resources/schema.sql`
4. 导入 `backend/src/main/resources/data.sql`
5. 如前端依赖缺失则执行 `npm install`
6. 先启动后端，再等待后端可访问后启动前端

如果你的 MySQL 账号密码不是默认值，可以直接传参：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\bootstrap-dev\bootstrap-dev.ps1 `
  -DbUser root `
  -DbPassword 123456 `
  -DbName dental_clinic
```


## 数据库与初始化

- 应用默认连接信息在 [application.yml](backend/src/main/resources/application.yml)
- 后端启动时会执行：
  - `backend/src/main/resources/schema.sql`
  - `backend/src/main/resources/data.sql`
- 新脚本也会在启动前主动建库并导入，适合空 MySQL 环境


## 说明

- 本项目用于毕业设计演示，不接真实支付、短信、物流、医保或医疗合规系统
- AI 问诊输出仅供初步参考，不能替代医生诊断
