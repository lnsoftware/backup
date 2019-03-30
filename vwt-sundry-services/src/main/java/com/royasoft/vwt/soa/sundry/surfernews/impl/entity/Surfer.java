package com.royasoft.vwt.soa.sundry.surfernews.impl.entity;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 冲浪新闻 实体
 * 
 * @author daizl
 *
 */
@Entity
@Table(name = "surfernews")
public class Surfer {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "time")
    private Timestamp time;

    @Column(name = "message")
    private String message;

    @Column(name = "source")
    private String source;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
