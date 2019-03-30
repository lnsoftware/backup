package com.royasoft.vwt.controller.util;

/**
 * 分页工具类
 * 
 * @author
 *
 */
public class PageUtils {

    /**
     * 计算获取总页数
     * 
     * @param recordCount 总记录数
     * @param pageSize 每页条数
     * @return 总页数
     */
    public static int getPageCount(int recordCount, int pageSize) {
        // 总条数/每页显示的条数=总页数
        int size = recordCount / pageSize;
        // 最后一页的条数
        int mod = recordCount % pageSize;
        if (mod != 0)
            size++;
        return recordCount == 0 ? 1 : size;
    }
}
