# 留学者指南 留学申请助手系统

留学者指南 是一个智能留学申请助手系统，帮助用户完成留学申请流程。系统提供用户管理、AI文书生成与润色、学校推荐、论坛交流等核心功能。

## 项目结构

```
AdmitGenius/
├── backend/           # 后端代码（Spring Boot）
│   ├── docs/          # 后端相关文档
│   ├── sql/           # 数据库脚本
│   └── src/           # 源代码
├── frontend/          # 前端代码（Vue 3 + TypeScript）
├── docs/              # 项目文档
├── data/              # 数据文件
│   └── uploads/       # 上传文件目录
├── start.bat          # Windows 一键启动脚本
└── start.sh           # Linux/macOS 一键启动脚本
```

## 技术栈

### 后端
- Spring Boot 3.4.3
- Java 17
- MySQL 8.0
- Spring Security + JWT
- Spring Data JPA
- OpenAI GPT API

### 前端
- Vue 3
- TypeScript
- Vite
- Element Plus
- Pinia
- Vue Router

## 快速开始

### 环境要求
- Java 17 或更高版本
- Node.js 16 或更高版本
- MySQL 8.0 或更高版本
- Maven 3.6 或更高版本

### 第一步：数据库配置

**创建数据库：**
```powershell
# 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS admitgenius_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 导入数据表结构（使用SOURCE命令，在项目根目录执行）
mysql -u root -p admitgenius_db -e "SOURCE ./backend/sql/create_table.sql;"
```


### 第二步：后端配置

1. **进入后端目录：**
```powershell
cd backend
```

2. **配置数据库连接：**
   - 复制 `src/main/resources/application.properties.template` 中的内容，粘贴覆盖到 `application.properties`
   - 修改数据库配置：
```properties
spring.datasource.username=root
spring.datasource.password=你的数据库密码
```

3. **配置 JWT 密钥：**
```properties
app.jwt.secret=你的JWT密钥（建议使用 32 位以上随机字符串，可以使用powershell生成）
```

4. **配置 kimi ai API 密钥（可选）：**

如果你有自己申请的 Kimi AI 或 OpenAI 的 API Key，通过环境变量注入，避免把密钥写入仓库。


推荐使用：本地（PowerShell）临时设置（只在当前终端会话有效）：

```powershell
$env:OPENAI_API_KEY = 'sk-你的-kimi-或-openai-key'
$env:OPENAI_API_PROXY_URL = 'https://api.moonshot.cn/v1'
cd backend
.\mvnw.cmd spring-boot:run
```

其他可用方式：（1）PowerShell 永久设置（跨会话，需重启终端或重新登录）：

```powershell
setx OPENAI_API_KEY "sk-你的-kimi-或-openai-key"
setx OPENAI_API_PROXY_URL "https://api.moonshot.cn/v1"
```

（2）Linux / macOS（bash / zsh）示例：

```bash
export OPENAI_API_KEY='sk-你的-kimi-或-openai-key'
export OPENAI_API_PROXY_URL='https://api.moonshot.cn/v1'
./mvnw spring-boot:run
```



### 第三步：前端配置

1. **进入前端目录：**
```powershell
cd frontend
```

2. **安装依赖：**
```powershell
npm install
```

3. **配置 API 地址：**
   
   前端已配置了 Vite 代理，自动将 `/api` 请求转发到后端服务。
   如需修改，请编辑 `vite.config.ts` 文件。

### 第四步：🚀 一键启动

**Windows 用户（推荐）：**
```powershell
# 回到项目根目录，直接双击 start.bat 文件，或运行：
.\start.bat
```

**Linux/macOS 用户：**
```bash
./start.sh
```

启动脚本会自动：
1. 启动后端服务（端口：7077）
2. 等待 10 秒确保后端完全启动
3. 启动前端服务（端口：5174）

### 手动启动（可选）

如果不使用一键启动脚本，也可以手动启动：

**启动后端：**
```powershell
cd backend
mvn spring-boot:run
```

**启动前端：**
```powershell
cd frontend
npm run dev
```

## 访问地址

- **前端应用：** http://localhost:5174
- **后端 API：** http://localhost:7077


## 功能特性

- ✅ 用户管理：注册、登录、个人信息管理
- ✅ AI文书系统：文书生成、润色、管理
- ✅ 智能推荐：基于用户背景的学校推荐
- ✅ 论坛交流：帖子发布、评论互动
- ✅ 管理员系统：学校数据管理、用户管理

## 开发指南

### 后端开发
- 遵循 RESTful API 设计规范
- 使用 JWT 进行身份认证
- 实现全局异常处理
- 使用 Spring Data JPA 进行数据访问

### 前端开发
- 使用 Vue 3 Composition API
- 使用 TypeScript 进行类型检查
- 使用 Element Plus 组件库
- 使用 Pinia 进行状态管理

## 部署

### 后端部署
```powershell
cd backend
mvn clean package
java -jar target/AdmitGeniusBackEnd-0.0.1-SNAPSHOT.jar
```

### 前端部署
```powershell
cd frontend
npm run build
# 将 dist 目录部署到 Web 服务器
```

## 故障排除

### 常见问题

1. **端口被占用：**
   ```powershell
   # 查看端口占用情况
   netstat -ano | findstr "7077"
   netstat -ano | findstr "5174"
   ```

2. **数据库连接失败：**
   - 确保 MySQL 服务已启动
   - 检查数据库用户名和密码
   - 确认数据库已创建

3. **Maven 构建失败：**
   ```powershell
   # 清理并重新构建
   mvn clean install -DskipTests
   ```

4. **npm 安装失败：**
   ```powershell
   # 清理缓存并重新安装
   npm cache clean --force
   Remove-Item -Recurse -Force node_modules
   npm install
   ```

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

MIT License 