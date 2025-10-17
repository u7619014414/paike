# 学生选课系统 - 移动端

这是学生选课系统的移动端版本,使用 Vue 3 + Vant UI 构建,专为手机浏览器优化。

## 功能特性

- **学生登录** - 使用学号和密码登录系统
- **学生注册** - 新学生在线注册
- **选课中心** - 浏览课程表并在线选课/退课
- **学生管理** - 查看和管理学生信息
- **个人中心** - 查看个人信息和已选课程

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **TypeScript** - 类型安全
- **Vant 4** - 移动端 UI 组件库
- **Pinia** - 状态管理
- **Vue Router** - 路由管理
- **Axios** - HTTP 客户端
- **Day.js** - 日期处理
- **Vite** - 构建工具

## 开发环境要求

- Node.js 16+
- pnpm (推荐) 或 npm

## 快速开始

### 1. 安装依赖

```bash
cd student-course-mobile
pnpm install
```

或使用 npm:

```bash
npm install
```

### 2. 启动开发服务器

```bash
pnpm dev
```

或使用 npm:

```bash
npm run dev
```

项目将在 `http://localhost:3001` 启动

### 3. 构建生产版本

```bash
pnpm build
```

或使用 npm:

```bash
npm run build
```

构建后的文件将生成在 `dist/` 目录下。

## 项目结构

```
student-course-mobile/
├── src/
│   ├── api/              # API 请求函数
│   │   ├── index.ts      # API 接口定义
│   │   └── request.ts    # Axios 请求封装
│   ├── router/           # 路由配置
│   │   └── index.ts      # 路由定义和守卫
│   ├── stores/           # Pinia 状态管理
│   │   └── user.ts       # 用户状态
│   ├── styles/           # 全局样式
│   │   └── global.scss   # 全局样式文件
│   ├── types/            # TypeScript 类型定义
│   │   └── index.ts      # 类型定义
│   ├── views/            # 页面组件
│   │   ├── Login.vue            # 登录页面
│   │   ├── CourseSelection.vue  # 选课页面
│   │   ├── StudentManagement.vue # 学生管理页面
│   │   └── My.vue               # 个人中心页面
│   ├── App.vue           # 根组件
│   ├── main.ts           # 入口文件
│   └── env.d.ts          # 环境变量类型定义
├── index.html            # HTML 模板
├── vite.config.ts        # Vite 配置
├── tsconfig.json         # TypeScript 配置
├── package.json          # 项目依赖
└── README.md             # 项目说明
```

## 环境变量

可以创建 `.env` 文件配置环境变量:

```env
# API 基础地址
VITE_API_BASE_URL=http://asdnn.com:45081/api
```

## API 配置

后端 API 代理配置在 `vite.config.ts` 中:

```typescript
server: {
  port: 3001,
  host: '0.0.0.0',
  proxy: {
    '/api': {
      target: 'http://localhost:45081',
      changeOrigin: true
    }
  }
}
```

## 页面路由

- `/login` - 登录页面
- `/course-selection` - 选课中心
- `/student-management` - 学生管理
- `/my` - 个人中心

## 移动端适配

- 使用 Vant UI 组件,完美适配移动设备
- 支持下拉刷新和上拉加载
- 响应式布局,适配不同屏幕尺寸
- 支持触摸手势操作
- 底部标签栏导航

## 主要功能说明

### 登录和注册
- 学生使用学号和密码登录
- 新学生可以在线注册,系统自动生成学号
- 支持 JWT Token 认证

### 选课功能
- 按周查看课程表
- 查看课程详情(教室、时间、人数等)
- 点击课程卡片进行选课/退课
- 实时显示已选课程状态
- 课程容量提示

### 学生管理
- 浏览所有学生信息
- 按年龄组筛选学生
- 搜索学生姓名或学号
- 查看学生详细信息

### 个人中心
- 查看个人信息
- 查看已选课程列表
- 查看家长联系信息
- 退出登录

## 注意事项

1. **后端服务**: 确保后端服务运行在 `http://localhost:45081`
2. **数据库**: 确保数据库配置正确且数据已导入
3. **网络**: 移动设备访问时需要确保设备和开发机在同一网络
4. **端口**: 默认端口 3001,避免与桌面版(3000)冲突

## 移动端调试

### 真机调试

1. 确保手机和电脑在同一局域网
2. 获取电脑 IP 地址(如 192.168.1.100)
3. 在手机浏览器访问 `http://192.168.1.100:3001`

### Chrome DevTools 模拟

1. 打开 Chrome DevTools (F12)
2. 点击设备工具栏图标 (Ctrl+Shift+M)
3. 选择移动设备型号进行模拟

## 浏览器兼容性

- iOS Safari 10+
- Android Chrome 60+
- 微信内置浏览器
- 其他现代移动浏览器

## 开发提示

- 使用 `console.log()` 查看调试信息
- Vant 组件文档: https://vant-ui.github.io/vant/
- Vue 3 文档: https://cn.vuejs.org/

## 故障排除

### 依赖安装失败
```bash
# 清除缓存
pnpm store prune
# 重新安装
pnpm install
```

### 端口被占用
修改 `vite.config.ts` 中的端口号:
```typescript
server: {
  port: 3002  // 更改为其他端口
}
```

### 无法连接后端
检查 `vite.config.ts` 中的代理配置是否正确

## 版本历史

- v1.0.0 - 初始版本
  - 基础登录注册功能
  - 选课和退课功能
  - 学生信息管理
  - 个人中心

## License

MIT
