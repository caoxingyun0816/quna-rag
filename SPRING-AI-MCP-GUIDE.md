# Spring AI MCP Server 实现指南

## 📋 概述

本项目已使用 **Spring AI** 官方框架实现了标准的 MCP (Model Context Protocol) Server。相比之前的自定义实现，这种方式更加简洁、标准化且易于维护。

## ✨ 核心优势

| 特性 | 自定义实现 | Spring AI 实现 |
|------|-----------|---------------|
| 代码量 | ~600 行 | ~200 行 |
| 协议处理 | 手动实现 JSON-RPC | ✅ 自动处理 |
| 工具注册 | 手动定义 schema | ✅ @Tool 注解自动注册 |
| 资源管理 | 手动实现 | ✅ @Resource 注解（可选） |
| 类型安全 | 部分 | ✅ 完全类型安全 |
| 维护成本 | 高 | ✅ 低 |
| 生态集成 | 无 | ✅ Spring AI 生态 |

## 🏗️ 架构设计

### 组件结构

```
com.quna.rag.mcp.springai/
├── SpringAiMcpTools.java      # MCP 工具服务（@Tool）
├── SpringAiMcpResources.java  # MCP 资源服务（@Resource）
└── SpringAiMcpConfig.java     # MCP 配置类
```

### 工作流程

```
AI Client → HTTP Request → Spring AI MCP Server 
                                ↓
                         @Tool/@Resource Methods
                                ↓
                         Business Services
                                ↓
                         Database / Milvus
```

## 🚀 快速开始

### 1. 依赖配置

已在 `pom.xml` 中添加：

```xml

<!-- Spring AI MCP Server Starter -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
    <version>1.1.2</version>
</dependency>
```

### 2. 应用配置

在 `application.yml` 中配置：

```yaml
spring:
  ai:
     mcp:
        server:
           enabled: true
           name: quna-rag
           version: 1.0.0
           type: SYNC
           protocol: STREAMABLE
           streamable-http:
              mcp-endpoint: /mcp

```

### 3. 启动应用

```bash
# 设置 Java 17
export JAVA_HOME=/Users/caoxingyun/Library/Java/JavaVirtualMachines/ms-17.0.18/Contents/Home

# 启动
mvn spring-boot:run
```

MCP endpoint 将自动暴露在：`http://localhost:8080/mcp`

## 🛠️ 可用的 MCP 工具

### 1. qunaLogin - 登录

**描述**: 登录趣拿 RAG 平台并获取 JWT token

**参数**:
- `username` (string, required): 平台账号
- `password` (string, required): 平台密码

**返回**:
```json
{
  "success": true,
  "message": "登录成功",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**代码示例**:
```java
@Tool(description = "登录趣拿 RAG 平台并获取 JWT token")
public Map<String, Object> qunaLogin(
        @ToolParam(description = "平台账号") String username,
        @ToolParam(description = "平台密码") String password) {
    // 实现逻辑
}
```

### 2. qunaDocList - 查询文档列表

**描述**: 查询当前用户的文档列表，支持按关键词、来源、状态筛选

**参数**:
- `keyword` (string, optional): 文档名称关键词
- `source` (string, optional): 来源（本地上传、钉钉）
- `status` (string, optional): 状态（已入库、停用）

**返回**:
```json
{
  "success": true,
  "total": 10,
  "documents": [...]
}
```

### 3. qunaDocSearch - 知识库检索

**描述**: 执行知识库检索测试。默认只返回命中片段，需要时可打开 includeAnswer 生成回答

**参数**:
- `question` (string, required): 检索问题
- `topK` (integer, optional): 返回命中数量，默认 3
- `includeAnswer` (boolean, optional): 是否生成回答，默认 false

**返回**:
```json
{
  "success": true,
  "question": "你的问题",
  "context": "相关文档内容",
  "hits": [],
  "answer": "",
  "includeAnswer": false
}
```

## 📦 可用的 MCP 资源

### 1. 服务器信息

- **URI**: `quna-rag://server/info`
- **描述**: 当前 quna-rag MCP 服务和后端地址信息
- **MIME Type**: `application/json`

## 💻 开发指南

### 添加新工具

只需在 `SpringAiMcpTools` 类中添加方法并使用 `@Tool` 注解：

```java
@Tool(description = "工具描述")
public Map<String, Object> newTool(
        @ToolParam(description = "参数1描述") String param1,
        @ToolParam(description = "参数2描述", required = false) Integer param2) {
    
    // 业务逻辑
    Map<String, Object> result = new HashMap<>();
    result.put("success", true);
    result.put("data", "");
    return result;
}
```

**就这么简单！** Spring AI 会自动：
1. 解析 `@Tool` 和 `@ToolParam` 注解
2. 生成工具 schema
3. 注册到 MCP Server
4. 处理 JSON-RPC 通信

### 添加新资源

在 `SpringAiMcpResources` 类中添加方法并使用 `@Resource` 注解：

```java
@Resource(
    uri = "quna-rag://custom/resource",
    name = "资源名称",
    description = "资源描述",
    mimeType = "application/json"
)
public String getCustomResource() {
    // 返回 JSON 字符串
    return "{\"data\": \"...\"}";
}
```

## 🔐 认证与安全

### 当前实现

目前工具方法中的 `getCurrentUserId()` 返回 `null`，需要完善认证逻辑。

### 推荐方案

#### 方案一：从 HTTP Header 提取 Token

```java
@Autowired
private HttpServletRequest request;

private Long getCurrentUserId() {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);
        Claims claims = jwtUtil.parseToken(token);
        return claims.get("userId", Long.class);
    }
    return null;
}
```

#### 方案二：使用 ThreadLocal + Interceptor

创建拦截器在请求开始时解析 token 并设置到 ThreadLocal：

```java
@Component
public class McpAuthInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) {
        String token = extractToken(request);
        if (token != null) {
            Long userId = jwtUtil.getUserIdFromToken(token);
            UserContext.setUserId(userId);
        }
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, 
                               HttpServletResponse response, 
                               Object handler, 
                               Exception ex) {
        UserContext.clear();
    }
}
```

## 🧪 测试

### 使用 curl 测试

```bash
# 1. 初始化
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize"}'

# 2. 获取工具列表
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":2,"method":"tools/list"}'

# 3. 调用登录工具
curl -X POST http://localhost:8080/mcp \
  -H "Content-Type: application/json" \
  -d '{
    "jsonrpc":"2.0",
    "id":3,
    "method":"tools/call",
    "params":{
      "name":"qunaLogin",
      "arguments":{"username":"admin","password":"123456"}
    }
  }'
```
