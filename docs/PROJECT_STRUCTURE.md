# 留学者指南 项目结构说明

## 项目根目录结构

```
AdmitGenius/
├── backend/                    # 后端服务（Spring Boot）
│   ├── docs/                   # 后端文档
│   │   ├── CONFIGURATION.md    # 配置说明
│   │   ├── FEATURES.md         # 功能特性文档
│   │   ├── PROJECT_STRUCTURE.md # 原项目结构文档
│   │   ├── README.md           # 后端说明文档
│   │   ├── TESTING_CHECKLIST.md # 测试检查表
│   │   ├── class_diagram.puml  # UML类图
│   │   └── doc.md              # 其他文档
│   ├── sql/                    # 数据库脚本
│   │   ├── create_table.sql    # 数据表创建脚本
│   │   ├── update_schema.sql   # 数据库更新脚本
│   │   └── update_school_images.sql # 学校图片更新脚本
│   ├── src/                    # 源代码
│   │   ├── main/
│   │   │   ├── java/com/admitgenius/
│   │   │   │   ├── backend/    # 主应用类
│   │   │   │   ├── controller/ # 控制器层
│   │   │   │   ├── dto/        # 数据传输对象
│   │   │   │   ├── entity/     # 实体类
│   │   │   │   ├── exception/  # 异常处理
│   │   │   │   ├── repository/ # 数据访问层
│   │   │   │   ├── security/   # 安全配置
│   │   │   │   └── service/    # 业务逻辑层
│   │   │   └── resources/      # 配置文件
│   │   └── test/               # 测试代码
│   ├── .mvn/                   # Maven wrapper
│   ├── target/                 # Maven构建目录
│   ├── pom.xml                 # Maven配置文件
│   ├── mvnw                    # Maven wrapper script (Unix)
│   ├── mvnw.cmd                # Maven wrapper script (Windows)
│   ├── lombok.config           # Lombok配置
│   ├── deploy.sh               # 部署脚本
│   └── .gitignore              # Git忽略文件
│
├── frontend/                   # 前端应用（Vue 3 + TypeScript）
│   ├── public/                 # 静态资源
│   ├── src/                    # 源代码
│   │   ├── api/                # API调用层
│   │   ├── assets/             # 资源文件
│   │   ├── components/         # Vue组件
│   │   ├── config/             # 配置文件
│   │   ├── layouts/            # 布局组件
│   │   ├── router/             # 路由配置
│   │   ├── stores/             # Pinia状态管理
│   │   ├── types/              # TypeScript类型定义
│   │   ├── utils/              # 工具函数
│   │   ├── views/              # 页面视图
│   │   ├── App.vue             # 根组件
│   │   ├── main.ts             # 入口文件
│   │   ├── config.ts           # 配置文件
│   │   └── env.d.ts            # 环境类型定义
│   ├── node_modules/           # Node.js依赖（Git忽略）
│   ├── package.json            # Node.js依赖配置
│   ├── package-lock.json       # 锁定版本文件
│   ├── vite.config.ts          # Vite构建配置
│   ├── tsconfig.json           # TypeScript配置
│   ├── index.html              # HTML模板
│   ├── auto-imports.d.ts       # 自动导入类型定义
│   └── components.d.ts         # 组件类型定义
│
├── docs/                       # 项目文档
│   ├── OVERVIEW.md             # 前端概览文档
│   ├── README.md               # 前端说明文档
│   └── PROJECT_STRUCTURE.md    # 本文档
│
├── data/                       # 数据文件
│   └── uploads/                # 用户上传文件
│       └── avatars/            # 用户头像
│
├── .vscode/                    # VS Code配置
├── .idea/                      # IntelliJ IDEA配置
├── .git/                       # Git版本控制
├── README.md                   # 主说明文档
├── .gitignore                  # Git忽略规则
├── start.bat                   # Windows启动脚本
└── start.sh                    # Linux/macOS启动脚本
```

## 目录说明

### 后端 (backend/)
- **docs/**: 存放后端相关的技术文档
- **sql/**: 存放数据库脚本，包括表结构和数据初始化
- **src/main/java/**: Java源代码，按MVC架构分层
- **src/main/resources/**: 配置文件（如application.properties）
- **src/test/**: 单元测试和集成测试代码

### 前端 (frontend/)
- **src/**: Vue 3 + TypeScript 源代码
- **public/**: 静态资源文件
- **node_modules/**: NPM依赖包（不提交到Git）

### 文档 (docs/)
- 集中存放项目级别的文档
- 包括用户指南、开发文档等

### 数据 (data/)
- **uploads/**: 用户上传的文件，如头像、文档等
- 运行时生成的数据文件

## 文件路径约定

### 后端代码中的路径引用
- 上传文件路径: `data/uploads/`
- 数据库脚本: `backend/sql/`
- 配置文件: `backend/src/main/resources/`

### 前端代码中的路径引用
- API请求: `/api/*` (通过Vite代理转发到后端)
- 静态资源: `/src/assets/`
- 组件: `/src/components/`

## Git忽略规则

以下文件/目录不提交到Git：
- `node_modules/` - NPM依赖
- `target/` - Maven构建产物
- `dist/` - 前端构建产物
- `data/uploads/` - 用户上传文件
- `.env*` - 环境配置文件
- IDE配置文件

## 启动方式

1. **一键启动**: 运行根目录的 `start.bat` (Windows) 或 `start.sh` (Linux/macOS)
2. **手动启动**:
   - 后端: `cd backend && mvn spring-boot:run`
   - 前端: `cd frontend && npm run dev`

## 端口配置

- 后端服务: http://localhost:7077
- 前端服务: http://localhost:5174
- 前端代理: `/api/*` -> `http://localhost:7077/api/*` 