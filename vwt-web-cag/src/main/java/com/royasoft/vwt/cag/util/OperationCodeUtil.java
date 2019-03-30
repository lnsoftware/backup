/************************************************
 *  Copyright © 2002-2015 上海若雅软件系统有限公司 *
 ************************************************/
package com.royasoft.vwt.cag.util;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志编码转换工具类
 *
 * @Author:ZHOUKQ
 * @Since:2016年3月1日
 */
public class OperationCodeUtil {

    private static Map<String, Map<String, String>> operationMap = new HashMap<String, Map<String, String>>();
    static {
        operationMap.put("1201", createOperationInfo("工作圈业务处理", "C0017", "获取工作圈说说列表", "C0017001"));
        operationMap.put("1202", createOperationInfo("工作圈业务处理", "C0017", "获取工作圈个人说说列表", "C0017002"));
        operationMap.put("1203", createOperationInfo("工作圈业务处理", "C0017", "发表工作圈说说", "C0017003"));
        operationMap.put("1204", createOperationInfo("工作圈业务处理", "C0017", "删除工作圈说说", "C0017004"));
        operationMap.put("1205", createOperationInfo("工作圈业务处理", "C0017", "删除我的工作圈说说", "C0017004"));
        operationMap.put("1206", createOperationInfo("工作圈业务处理", "C0017", "评论工作圈说说", "C0017005"));
        operationMap.put("1207", createOperationInfo("工作圈业务处理", "C0017", "赞工作圈说说", "C0017006"));
        operationMap.put("1208", createOperationInfo("工作圈业务处理", "C0017", "取消赞工作圈说说", "C0017007"));
        operationMap.put("1209", createOperationInfo("工作圈业务处理", "C0017", "获取工作圈说说详情信息", "C0017008"));

        operationMap.put("1301", createOperationInfo("任务业务处理", "C0011", "创建任务", "C0011001"));
        operationMap.put("1302", createOperationInfo("任务业务处理", "C0011", "获取任务列表", "C0011002"));
        operationMap.put("1303", createOperationInfo("任务业务处理", "C0011", "编辑任务", "C0011003"));
        operationMap.put("1304", createOperationInfo("任务业务处理", "C0011", "结束任务", "C0011004"));
        operationMap.put("1305", createOperationInfo("任务业务处理", "C0011", "删除任务", "C0011005"));
        operationMap.put("1306", createOperationInfo("任务业务处理", "C0011", "发送文本回执", "C0011006"));
        operationMap.put("1307", createOperationInfo("任务业务处理", "C0011", "发送阅读回执", "C0011007"));
        operationMap.put("1308", createOperationInfo("任务业务处理", "C0011", "查询回执列表", "C0011008"));
    }

    /**
     * 组装原操作日志信息
     * 
     * @param moduleName 模块名称
     * @param moduleCode 模块编码
     * @param operationName 操作名
     * @param operationCode 操作编码
     * @return
     * @Description:
     */
    private static Map<String, String> createOperationInfo(String moduleName, String moduleCode, String operationName, String operationCode) {
        Map<String, String> operationInfo = new HashMap<String, String>();
        operationInfo.put("moduleName", moduleName);
        operationInfo.put("moduleCode", moduleCode);
        operationInfo.put("operationName", operationName);
        operationInfo.put("operationCode", operationCode);
        return operationInfo;
    }

    /**
     * 根据操作id获取操作日志信息
     * 
     * @param functionId
     * @return
     * @Description:
     */
    public static Map<String, String> getOperationInfo(String functionId) {
        return operationMap.get(functionId);
    }

    /**
     * 获取请求IP地址
     * 
     * @param request
     * @return
     */
    public static String getIpAddr(Channel ctx, HttpRequest request) {
        String clientIP = request.headers().get("X-Forwarded-For");
        if (clientIP == null) {
            InetSocketAddress insocket = (InetSocketAddress) ctx.remoteAddress();
            clientIP = insocket.getAddress().getHostAddress();
        }
        return clientIP;
    }

}
