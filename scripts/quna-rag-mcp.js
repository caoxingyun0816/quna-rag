#!/usr/bin/env node

const baseUrl = (process.env.QUNA_RAG_BASE_URL || 'http://127.0.0.1:8080').replace(/\/$/, '');
let token = process.env.QUNA_RAG_TOKEN || '';

const RESOURCE_URIS = {
  serverInfo: 'quna-rag://server/info',
  docs: 'quna-rag://docs'
};

let buffer = Buffer.alloc(0);

process.stdin.on('data', chunk => {
  buffer = Buffer.concat([buffer, chunk]);
  readMessages();
});

function readMessages() {
  while (true) {
    const headerEnd = buffer.indexOf('\r\n\r\n');
    if (headerEnd < 0) return;

    const header = buffer.slice(0, headerEnd).toString('utf8');
    const match = header.match(/content-length:\s*(\d+)/i);
    if (!match) {
      buffer = buffer.slice(headerEnd + 4);
      continue;
    }

    const length = Number(match[1]);
    const bodyStart = headerEnd + 4;
    const bodyEnd = bodyStart + length;
    if (buffer.length < bodyEnd) return;

    const raw = buffer.slice(bodyStart, bodyEnd).toString('utf8');
    buffer = buffer.slice(bodyEnd);
    const message = JSON.parse(raw);
    handleMessage(message).catch(error => {
      sendError(message.id || null, -32603, error.message || String(error));
    });
  }
}

async function handleMessage(message) {
  if (!message.method) return;

  if (message.method === 'initialize') {
    sendResult(message.id, {
      protocolVersion: '2024-11-05',
      capabilities: {
        tools: {},
        resources: {}
      },
      serverInfo: { name: 'quna-rag', version: '1.0.0' }
    });
    return;
  }

  if (message.method === 'notifications/initialized') return;

  if (message.method === 'tools/list') {
    sendResult(message.id, { tools: tools() });
    return;
  }

  if (message.method === 'tools/call') {
    const { name, arguments: args = {} } = message.params || {};
    const result = await callTool(name, args);
    sendResult(message.id, {
      content: [{ type: 'text', text: typeof result === 'string' ? result : JSON.stringify(result, null, 2) }]
    });
    return;
  }

  if (message.method === 'resources/list') {
    sendResult(message.id, { resources: resources() });
    return;
  }

  if (message.method === 'resources/templates/list') {
    sendResult(message.id, { resourceTemplates: resourceTemplates() });
    return;
  }

  if (message.method === 'resources/read') {
    const { uri } = message.params || {};
    const result = await readResource(uri);
    sendResult(message.id, result);
    return;
  }

  sendError(message.id, -32601, `Unsupported method: ${message.method}`);
}

function resources() {
  return [
    {
      uri: RESOURCE_URIS.serverInfo,
      name: 'quna-rag 服务信息',
      description: '当前 quna-rag MCP 服务和后端地址信息。',
      mimeType: 'application/json'
    },
    {
      uri: RESOURCE_URIS.docs,
      name: 'quna-rag 文档列表',
      description: '当前登录用户可访问的文档列表。读取前需要先调用 quna_login，或设置 QUNA_RAG_TOKEN。',
      mimeType: 'application/json'
    }
  ];
}

function resourceTemplates() {
  return [
    {
      uriTemplate: 'quna-rag://docs{?keyword,source,status}',
      name: 'quna-rag 文档列表查询',
      description: '按文档名称关键词、来源、状态查询当前登录用户可访问的文档列表。',
      mimeType: 'application/json'
    }
  ];
}

function tools() {
  return [
    {
      name: 'quna_login',
      description: '登录趣拿 RAG 平台并缓存 JWT token，后续工具会复用该 token。',
      inputSchema: {
        type: 'object',
        properties: {
          username: { type: 'string', description: '平台账号' },
          password: { type: 'string', description: '平台密码' }
        },
        required: ['username', 'password']
      }
    },
    {
      name: 'quna_doc_list',
      description: '查询当前用户的文档列表。',
      inputSchema: {
        type: 'object',
        properties: {
          keyword: { type: 'string', description: '文档名称关键词' },
          source: { type: 'string', description: '来源，例如 本地上传 或 钉钉' },
          status: { type: 'string', description: '状态，例如 已入库 或 停用' }
        }
      }
    },
    {
      name: 'quna_doc_search',
      description: '执行知识库检索测试。默认只返回命中片段以提升速度，需要时可打开 includeAnswer 生成回答。',
      inputSchema: {
        type: 'object',
        properties: {
          question: { type: 'string', description: '检索问题' },
          topK: { type: 'number', description: '返回命中数量，默认 3' },
          includeAnswer: { type: 'boolean', description: '是否额外调用大模型生成回答，默认 false' }
        },
        required: ['question']
      }
    }
  ];
}

async function callTool(name, args) {
  if (name === 'quna_login') {
    const data = await request('/api/user/login', {
      method: 'POST',
      body: JSON.stringify({ username: args.username, password: args.password })
    }, false);
    if (data.code !== 200 || !data.token) {
      throw new Error(data.msg || '登录失败');
    }
    token = data.token;
    return { code: 200, msg: '登录成功' };
  }

  if (name === 'quna_doc_list') {
    const params = new URLSearchParams();
    for (const key of ['keyword', 'source', 'status']) {
      if (args[key]) params.set(key, args[key]);
    }
    return request(`/api/doc/list${params.toString() ? `?${params}` : ''}`);
  }

  if (name === 'quna_doc_search') {
    return request('/api/doc/search-test', {
      method: 'POST',
      body: JSON.stringify({
        question: args.question,
        topK: args.topK || 3,
        includeAnswer: Boolean(args.includeAnswer)
      })
    });
  }

  throw new Error(`Unknown tool: ${name}`);
}

async function readResource(uri) {
  if (uri === RESOURCE_URIS.serverInfo) {
    return resourceText(uri, {
      name: 'quna-rag',
      version: '1.0.0',
      baseUrl,
      authenticated: Boolean(token)
    });
  }

  if (uri === RESOURCE_URIS.docs || uri.startsWith(`${RESOURCE_URIS.docs}?`)) {
    const args = parseDocsResourceArgs(uri);
    const data = await callTool('quna_doc_list', args);
    return resourceText(uri, data);
  }

  throw new Error(`Unknown resource: ${uri}`);
}

function parseDocsResourceArgs(uri) {
  const parsed = new URL(uri);
  return {
    keyword: parsed.searchParams.get('keyword') || '',
    source: parsed.searchParams.get('source') || '',
    status: parsed.searchParams.get('status') || ''
  };
}

function resourceText(uri, data) {
  return {
    contents: [
      {
        uri,
        mimeType: 'application/json',
        text: JSON.stringify(data, null, 2)
      }
    ]
  };
}

async function request(path, options = {}, requireToken = true) {
  if (requireToken && !token) {
    throw new Error('未登录：请先调用 quna_login，或在 MCP 配置里设置 QUNA_RAG_TOKEN。');
  }

  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {})
  };
  if (requireToken) headers.Authorization = `Bearer ${token}`;

  const response = await fetch(`${baseUrl}${path}`, {
    ...options,
    headers
  });
  const text = await response.text();
  const data = text ? JSON.parse(text) : {};
  if (!response.ok) {
    throw new Error(data.msg || `HTTP ${response.status}`);
  }
  return data;
}

function sendResult(id, result) {
  send({ jsonrpc: '2.0', id, result });
}

function sendError(id, code, message) {
  send({ jsonrpc: '2.0', id, error: { code, message } });
}

function send(message) {
  const body = Buffer.from(JSON.stringify(message), 'utf8');
  process.stdout.write(`Content-Length: ${body.length}\r\n\r\n`);
  process.stdout.write(body);
}
