package com.xiyun.xiyun.test.dao.data;

import lombok.Data;

import java.util.Date;

/**
 * @author zhaoyongqiang
 * @date 2020-03-07
 */
@Data
public class UserDO {

    /**
     * 主键
     */
    private String id;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * 创建时间
     */
    private Date crateTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 手机号
     */
    private String mobile;
}