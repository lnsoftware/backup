package com.royasoft.vwt.cag.vo;

import java.io.Serializable;

/**
 * 图文 内容条目
 * 
 * @author jxue
 * 
 */
public class NewsItem implements Serializable {

    private static final long serialVersionUID = -1773599454369201258L;

    private String id;

    /** 图文标题 */
    private String title;

    /** 图文说明 */
    private String description;

    /** 图片地址 */
    private String picUrl;

    /** 点击跳转地址 */
    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
