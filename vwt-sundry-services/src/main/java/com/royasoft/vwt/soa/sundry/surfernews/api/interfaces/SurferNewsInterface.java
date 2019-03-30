package com.royasoft.vwt.soa.sundry.surfernews.api.interfaces;

import java.sql.Timestamp;

public interface SurferNewsInterface {

    /**
     * 保存冲浪新闻信息
     * 
     * @param uuid
     * @param t
     * @param content
     * @param source
     */
    public void saveSurferInfo(String uuid, Timestamp t, String content, String source);

}
