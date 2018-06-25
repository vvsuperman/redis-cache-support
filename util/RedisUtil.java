package cn.com.earnfish.rediscache.util;

import java.util.Date;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import cn.com.earnfish.autoconfigure.amqp.RabbitSender;
import cn.com.earnfish.base.constant.MQConstants;
import cn.com.earnfish.base.vo.RabbitMetaMessage;
import cn.com.earnfish.rediscache.po.RedisLogPO;
import cn.com.earnfish.utils.Identity;
import cn.com.earnfish.utils.JacksonUtil;

@Component
public class RedisUtil {
	
	@Autowired
	@Qualifier("customRedisTemplate")
	RedisTemplate customRedisTemplate;
	
	@Autowired
	@Qualifier("customRabbitTemplate") 
	RabbitTemplate rabbitTemplate;
	
	
	@Autowired
	RedisTemplate<String, String> redisTemplate;
	
	 
    /**
     * 发送redis的操作日志消息
     * @author fw
     * @param key 加入redis的主键
     * @param path 调用者  xxService.xxmethod
     * @param joinTime key加入时间
     * @param outTime key过期时间
     * @descript 对key的描述
     * @return
     */
	public  void sendRedisLogMessage(String key,String path,Date joinTime,   Date outTime,String descript ) {
	    	RedisLogPO redisLogPO = new RedisLogPO();
	    	redisLogPO.setId(Identity.syncUUID());
	    	redisLogPO.setKey(key);
	    	redisLogPO.setPath(path);
	    	redisLogPO.setJoinTime(joinTime); //扔redis时间
	    	redisLogPO.setOutTime(outTime); //失效时间
	    	
	    	RabbitMetaMessage message = new RabbitMetaMessage(redisLogPO, MQConstants.BUSINESS_EXCHANGE,
					 MQConstants.REDIS_LOG_ROUTING_KEY);
	    	
	    	RabbitSender rabbitSender = new RabbitSender(customRedisTemplate, rabbitTemplate);
	    	
	    	rabbitSender.send(message);
	    			    	
	 }
	
	//redis的get value,因为分布式redis全部以json字符串存储，如果需要对象的话必须手动将字符串转为对象
	public  Object getRedisObject(String key,Class clazz) {
		String value = redisTemplate.opsForValue().get(key);
		if( value == null ) {
			return null;
		}
		return JacksonUtil.jsonToObj(value, clazz);
	}
	
		    

	
	
}
