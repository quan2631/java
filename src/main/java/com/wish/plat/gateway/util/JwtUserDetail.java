package com.wish.plat.gateway.util;



/**
 * @author bifeng
 * @version 2019-06-29
 * 优化为一个get\set
 */

public class JwtUserDetail {

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getCityId() {
		return cityId;
	}

	public void setCityId(String cityId) {
		this.cityId = cityId;
	}

	public String getOperCode() {
		return operCode;
	}

	public void setOperCode(String operCode) {
		this.operCode = operCode;
	}

	public String getOperRoleCode() {
		return operRoleCode;
	}

	public void setOperRoleCode(String operRoleCode) {
		this.operRoleCode = operRoleCode;
	}

	public String getOperChannel() {
		return operChannel;
	}

	public void setOperChannel(String operChannel) {
		this.operChannel = operChannel;
	}

	public String getOperOrgCode() {
		return operOrgCode;
	}

	public void setOperOrgCode(String operOrgCode) {
		this.operOrgCode = operOrgCode;
	}

	/**
     * 城市代码
     */

    private String cityId;
	/**
     * 用户ID
     */

    private String userId;
    /**
     * 用户编码
     */
    private String operCode;
    /**
     * 用户角色
     */
    private String operRoleCode;
    /**
     * 用户渠道
     */
    private String operChannel;
    /**
     * 所属机构编码
     */
    private String operOrgCode;
    private String sessionId;

    public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public JwtUserDetail(String cityID,String userId, String operCode, String operRoleCode, String operChannel, String operOrgCode,String sessionId) {
        this.cityId = cityID;
        this.operCode = operCode;
        this.operRoleCode= operRoleCode;
        this.operChannel = operChannel;
        this.operOrgCode = operOrgCode;
        this.sessionId=sessionId;
        this.userId=userId;
    }
}

