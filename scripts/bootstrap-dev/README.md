# bootstrap-dev

用于新电脑开发环境的一键启动脚本。

## 默认行为

运行 `bootstrap-dev.cmd` 或 `bootstrap-dev.ps1` 后，脚本会：

1. 检查 `java`、`node`、`npm`、`mysql`
2. 创建 `dental_clinic` 数据库
3. 导入 `schema.sql`
4. 导入 `data.sql`
5. 检查并安装前端依赖
6. 先启动后端，再启动前端

## 默认数据库参数

- Host: `localhost`
- Port: `3306`
- Database: `dental_clinic`
- User: `root`
- Password: `123456`

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
