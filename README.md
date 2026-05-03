# Dental Clinic Management System

毕业设计版口腔诊所管理系统，包含患者端、医生端、管理员端三类角色。

## 技术栈

- 前端：Vue 3、Vite、TypeScript、Pinia、Vue Router、Element Plus、Axios
- 后端：Spring Boot 3、Java 17、Spring Security JWT、MyBatis-Plus、MySQL 8
- AI：OpenAI 兼容外部接口，配置保存在本地 MySQL 的 `ai_config` 表

## 默认账号

| 角色 | 账号 | 密码 |
| --- | --- | --- |
| 管理员 | `admin` | `123456` |
| 医生 | `doctor_chen` | `123456` |
| 患者 | `patient_li` | `123456` |

## 新电脑启动

推荐直接使用新脚本目录下的启动脚本：

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

## 当前机器快速启动

如果数据库已经存在、依赖也已经安装好，可以继续使用根目录的快速启动脚本：

```powershell
.\start-dev.cmd
```

## 数据库与初始化

- 应用默认连接信息在 [application.yml](backend/src/main/resources/application.yml)
- 后端启动时会执行：
  - `backend/src/main/resources/schema.sql`
  - `backend/src/main/resources/data.sql`
- 新脚本也会在启动前主动建库并导入，适合空 MySQL 环境

## AI 配置

AI 牙医使用 OpenAI 兼容接口。真实 `base_url`、`api_key`、`model` 需要写入本地数据库 `ai_config`，不要提交到仓库：

```sql
UPDATE ai_config
SET base_url = 'https://api.openai.com/v1',
    api_key = 'your_api_key',
    model = 'gpt-4o-mini',
    enabled = 1,
    updated_at = NOW()
WHERE id = 1;
```

`application.yml` 只保留超时等非敏感运行参数。

## 说明

- 本项目用于毕业设计演示，不接真实支付、短信、物流、医保或医疗合规系统
- AI 问诊输出仅供初步参考，不能替代医生诊断
