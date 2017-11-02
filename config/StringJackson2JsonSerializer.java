package cn.com.spdbccc.hotelbank.rediscache.config;

import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import com.fasterxml.jackson.databind.ObjectMapper;

import cn.com.spdbccc.hotelbank.utils.JacksonUtil;



/** 
 * 描述：redis序列化、反序列化，会将对象转为Json String然后再序列化，方便跨服务
 * @author fw 
 * 创建时间：2017年10月14日 下午10:30:00 
 * @version 1.2.0 
 */  
public class StringJackson2JsonSerializer<T> extends Jackson2JsonRedisSerializer<T> {

	 
	
	
	private ObjectMapper objectMapper = new ObjectMapper();

	public StringJackson2JsonSerializer(Class<T> type) {
		super(type);
		// TODO Auto-generated constructor stub
	}
	
	
	
	public byte[] serialize(Object t) throws SerializationException {

		if (t == null) {
			return  new byte[0];
		}
		try {
			//将对象转为Json String然后再序列化，方便跨服务
			return this.objectMapper.writeValueAsBytes(JacksonUtil.objToJson(t));
		} catch (Exception ex) {
			throw new SerializationException("Could not write JSON: " + ex.getMessage(), ex);
		}
	}

}
