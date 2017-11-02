package cn.com.spdbccc.hotelbank.rediscache.po;


import java.util.Date;

import cn.com.spdbccc.hotelbank.base.entity.BaseEntity;


/**
 * REDIS操作实体类
 * @author fw
 * 2017年5月26日
 */
public class RedisLogPO extends BaseEntity<String>{
	 
	private static final long serialVersionUID = -7898559147420705558L;
	
	/**KEY**/
    private String key;
    /**加入时间**/
    private Date joinTime;
    /**过期时间**/
    private Date outTime;    
	/**IP地址**/
    private String ip;
    /**刷新路径**/
    private String path;
    /**描述**/
    private String descript;
    
    public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Date getJoinTime() {
		return joinTime;
	}
	public void setJoinTime(Date joinTime) {
		this.joinTime = joinTime;
	}
	public Date getOutTime() {
		return outTime;
	}
	public void setOutTime(Date outTime) {
		this.outTime = outTime;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getDescript() {
		return descript;
	}
	public void setDescript(String descript) {
		this.descript = descript;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
}