# bootstrap-dev

用于新电脑开发环境的一键启动脚本。

## 默认行为

运行 `bootstrap-dev.cmd` 或 `bootstrap-dev.ps1` 后，脚本会：

1. 检查 `java`、`node`、`npm`、`mysql`
2. 校验 Node.js 主版本是否大于等于 `18`
3. 创建 `dental_clinic` 数据库
4. 导入 `schema.sql`
5. 导入 `data.sql`
6. 检查并安装前端依赖
7. 先启动后端，再启动前端

## 前置环境

- Java 17
- Node.js 18 及以上
- npm
- MySQL 8

如果本机 Node.js 低于 18，脚本会直接停止并提示升级，不会自动下载安装新版本。

## 默认数据库参数

- Host: `localhost`
- Port: `3306`
- Database: `dental_clinic`
- User: `root`
- Password: ``

## 自定义数据库参数

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\bootstrap-dev\bootstrap-dev.ps1 `
  -DbUser root `
  -DbPassword 123456 `
  -DbName dental_clinic
```

## 可选参数

- `-SkipDatabaseInit`：跳过建库与导入
- `-SkipFrontendInstall`：跳过 `npm install`
