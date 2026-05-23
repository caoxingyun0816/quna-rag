package com.quna.rag.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class DingTalkUtil {
    @Value("${dingtalk.app-key:}")
    private String appKey;
    
    @Value("${dingtalk.app-secret:}")
    private String appSecret;
    
    @Value("${dingtalk.kb-space-id:}")
    private String kbSpaceId;

    @Value("${dingtalk.operatorId:}")
    private String operatorId;
    
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    private String accessToken;
    private long tokenExpireTime;
    
    /**
     * 获取钉钉访问令牌
     */
    public String getAccessToken() throws Exception {
        if (accessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return accessToken;
        }
        
        String url = "https://oapi.dingtalk.com/gettoken?appkey=" + appKey + "&appsecret=" + appSecret;
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();
        
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        JSONObject json = JSON.parseObject(res.body());
        
        if (json.getIntValue("errcode") == 0) {
            accessToken = json.getString("access_token");
            tokenExpireTime = System.currentTimeMillis() + (json.getLongValue("expires_in") - 300) * 1000;
            return accessToken;
        } else {
            throw new RuntimeException("获取钉钉Token失败: " + json.getString("errmsg"));
        }
    }
    
    /**
     * 获取知识库文档列表
     */
    public JSONArray getKnowledgeBaseDocs(String spaceId) throws Exception {
        String token = getAccessToken();
        // 钉钉知识库 API v2.0：获取节点列表
        String url = "https://api.dingtalk.com/v2.0/wiki/nodes?parentNodeId=" + spaceId + "&maxResults=100&operatorId="+operatorId;
            
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("x-acs-dingtalk-access-token", token)
                .GET()
                .timeout(Duration.ofSeconds(20))
                .build();
            
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        log.info("钉钉文档列表响应: {}", res.body());
            
        JSONObject json = JSON.parseObject(res.body());
            
        // 检查是否有错误
        if (json.containsKey("code") || json.containsKey("errorCode")) {
            String errorMsg = json.getString("message") != null ? json.getString("message") : json.getString("errmsg");
            throw new RuntimeException("钉钉 API 错误: " + errorMsg);
        }
            
        return json.getJSONArray("nodes");
    }
    
    /**
     * 获取文档内容（文本/Markdown）
     * 注意：入参的 dentryUuid 会先转换为实际的 spaceId 和 dentryId
     */
    public String getDocContent(String dentryUuid) throws Exception {
        // 先通过 dentryUuid 获取实际的 spaceId 和 dentryId
        String[] ids = getSpaceIdAndDentryId(dentryUuid);
        String actualSpaceId = ids[0];
        String actualDentryId = ids[1];
        
        log.info("开始获取文档内容: dentryUuid={}, spaceId={}, dentryId={}", dentryUuid, actualSpaceId, actualDentryId);
        
        String token = getAccessToken();
        // 钉钉知识库 v2.0 API：获取文档内容
        String url = "https://api.dingtalk.com/v2.0/doc/workspaces/" + actualSpaceId + "/documents/" + actualDentryId + "/content?operatorId=" + operatorId;
            
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("x-acs-dingtalk-access-token", token)
                .GET()
                .timeout(Duration.ofSeconds(30))
                .build();
            
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        log.info("钉钉文档内容响应状态码: {}", res.statusCode());
        log.debug("钉钉文档内容响应: {}", res.body());
            
        JSONObject json = JSON.parseObject(res.body());
            
        // 检查是否有错误
        if (json.containsKey("code") || json.containsKey("errorCode")) {
            String errorMsg = json.getString("message") != null ? json.getString("message") : json.getString("errmsg");
            log.error("钉钉API返回错误: code={}, message={}", json.get("code"), errorMsg);
            throw new RuntimeException("钉钉 API 错误: " + errorMsg);
        }
            
        // 返回文档内容（Markdown 格式）
        String content = json.getString("content");
        if (content == null) {
            log.warn("文档内容字段为null，完整响应: {}", json.toJSONString());
        } else {
            log.info("文档内容获取成功，长度: {} 字符", content.length());
        }
        return content;
    }
        
    /**
     * 下载文档文件（用于 PPTX、XLSX、PDF 等文件）
     * 按照钉钉官方文档：知识库下载文件 API
     * 步骤 1: 获取下载信息（URL + headers）
     * 步骤 2: 带 headers 下载文件
     * 
     * @param dentryUuid 文档条目UUID（需先通过此UUID获取实际的spaceId和dentryId）
     */
    public byte[] downloadDocFile(String dentryUuid) throws Exception {
        // 先通过 dentryUuid 获取 spaceId 和 dentryId
        String[] ids = getSpaceIdAndDentryId(dentryUuid);
        String actualSpaceId = ids[0];
        String actualDentryId = ids[1];
        
        String token = getAccessToken();
        // 步骤 1: 调用下载信息接口，获取 internalResourceUrls 和 headers（使用实际的 spaceId 和 dentryId）
        String downloadInfoUrl = "https://api.dingtalk.com/v1.0/storage/spaces/" + actualSpaceId + "/dentries/" + actualDentryId + "/downloadInfos/query?unionId="+ operatorId;
        
        HttpRequest infoReq = HttpRequest.newBuilder()
                .uri(URI.create(downloadInfoUrl))
                .header("x-acs-dingtalk-access-token", token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .timeout(Duration.ofSeconds(30))
                .build();
        log.info("获取下载信息请求url: {}", downloadInfoUrl);
        HttpResponse<String> infoRes = httpClient.send(infoReq, HttpResponse.BodyHandlers.ofString());
        log.info("获取下载信息响应: {}", infoRes.body());
        
        JSONObject infoJson = JSON.parseObject(infoRes.body());
        
        // 检查是否有错误
        if (infoJson.containsKey("code") && infoJson.getInteger("code") != 0) {
            String errorMsg = infoJson.getString("message") != null ? infoJson.getString("message") : infoJson.getString("errmsg");
            throw new RuntimeException("获取下载信息失败: " + errorMsg);
        }
        
        // 获取 headerSignatureInfo（包含所有下载信息）
        JSONObject headerSignatureInfo = infoJson.getJSONObject("headerSignatureInfo");
        if (headerSignatureInfo == null) {
            log.error("响应中未找到 headerSignatureInfo，完整响应: {}", infoJson.toJSONString());
            throw new RuntimeException("获取下载信息失败: 响应格式异常");
        }
        
        // 获取 resourceUrls（可能返回多个链接，取第一个）
        JSONArray urls = headerSignatureInfo.getJSONArray("resourceUrls");
        if (urls == null || urls.isEmpty()) {
            log.error("下载信息中未找到 resourceUrls，完整响应: {}", headerSignatureInfo.toJSONString());
            throw new RuntimeException("下载链接为空，请检查文档权限");
        }
        
        String downloadUrl = urls.getString(0);
        log.info("下载 URL: {}", downloadUrl);
        
        // 获取 headers（包含认证信息）
        JSONObject headersObj = headerSignatureInfo.getJSONObject("headers");
        Map<String, String> headers = new HashMap<>();
        if (headersObj != null) {
            for (String key : headersObj.keySet()) {
                headers.put(key, headersObj.getString(key));
            }
            log.info("下载需要额外 headers: {}", headers.keySet());
        }
        
        // 步骤 2: 执行下载（带上 headers）
        HttpRequest.Builder downloadReqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(downloadUrl))
                .GET()
                .timeout(Duration.ofSeconds(60));
        
        // 添加 headers（如 Range、Authorization 等）
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            downloadReqBuilder.header(entry.getKey(), entry.getValue());
        }
        
        HttpRequest downloadReq = downloadReqBuilder.build();
        HttpResponse<byte[]> downloadRes = httpClient.send(downloadReq, HttpResponse.BodyHandlers.ofByteArray());
        
        if (downloadRes.statusCode() != 200) {
            log.error("文件下载失败，状态码: {}, 响应: {}", downloadRes.statusCode(), new String(downloadRes.body()));
            throw new RuntimeException("文件下载失败，HTTP 状态码: " + downloadRes.statusCode());
        }
        
        log.info("文件下载成功，大小: {} bytes", downloadRes.body().length);
        return downloadRes.body();
    }

    /**
     * 根据 dentryUuid 获取 spaceId
     * @param dentryUuid 文档条目UUID
     * @return spaceId 知识库空间ID
     */
    public String getSpaceIdByDentryUuid(String dentryUuid) throws Exception {
        String[] ids = getSpaceIdAndDentryId(dentryUuid);
        return ids[0];
    }
    
    /**
     * 内部方法：根据 dentryUuid 同时获取 spaceId 和 dentryId
     * @return [spaceId, dentryId]
     */
    private String[] getSpaceIdAndDentryId(String dentryUuid) throws Exception {
        String token = getAccessToken();
        String url = "https://api.dingtalk.com/v2.0/doc/dentries/" + dentryUuid + "/queryDentryId?operatorId=" + operatorId;
        
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("x-acs-dingtalk-access-token", token)
                .header("Content-Type", "application/json")
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();
        
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        log.info("根据dentryUuid查询信息响应: {}", res.body());
        
        JSONObject json = JSON.parseObject(res.body());
        
        // 检查是否有错误
        if (json.containsKey("code") || json.containsKey("errorCode")) {
            String errorMsg = json.getString("message") != null ? json.getString("message") : json.getString("errmsg");
            throw new RuntimeException("获取文档信息失败: " + errorMsg);
        }
        
        String spaceId = json.getString("spaceId");
        String dentryId = json.getString("dentryId");
        log.info("转换后的 spaceId: {}, dentryId: {}", spaceId, dentryId);
        
        return new String[]{spaceId, dentryId};
    }
}
