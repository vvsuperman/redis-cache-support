package cn.com.earnfish.rediscache.annotation;

  
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;  
import org.aspectj.lang.annotation.Aspect;  
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.json.JSONObject;
import org.slf4j.Logger;  
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import cn.com.earnfish.rediscache.annotation.RedisCacheAble;
import cn.com.earnfish.rediscache.config.RedisCacheConfig;
import cn.com.earnfish.rediscache.util.RedisUtil;
import cn.com.earnfish.utils.JacksonUtil;
import redis.clients.jedis.Jedis;  
  


/** 
 * 描述：加入查询结果的缓存 
 * @author fw 
 * 创建时间：2017年10月14日 下午10:30:00 
 * @version 1.0.0 
 */  
@Component // 注册到Spring容器，必须加入这个注解  
@ConditionalOnClass({ RedisCacheConfig.class })
@Aspect // 该注解标示该类为切面类，切面是由通知和切点组成的。  
//TODO 异常处理，依赖控制
public class ApiAspect {  
	Logger logger = LoggerFactory.getLogger(ApiAspect.class);
	
	@Autowired
	@Qualifier("jsonRedisTemplate")	
	private RedisTemplate<String, Object> jsonRedisTemplate;
		
	@Autowired
	private RedisUtil redisUtil;
	
    @Pointcut("@annotation(cn.com.earnfish.rediscache.annotation.RedisCachePut)")// 定义注解类型的切点，只要方法上有该注解，都会匹配  
    public void annotationPut(){          
    }  
    
    @Pointcut("@annotation(cn.com.earnfish.rediscache.annotation.RedisCacheAble)")// 定义注解类型的切点，只要方法上有该注解，都会匹配  
    public void annotationAble(){        
    }       
    
    @Around("annotationPut()&& @annotation(rd)")
    public Object redisCachePut(ProceedingJoinPoint joinPoint,RedisCachePut rd) throws Throwable {
    	
    	if(joinPoint.getArgs().length!=1 && joinPoint.getArgs()[0] == null) {
      	   logger.error("rediaCache args is null");    
     	}	
    	
    	//执行函数
        //如果返回值不是整数，那么就不是用于插入操作,返回
		Object returnObj = joinPoint.proceed();
		if(returnObj == null) {
			logger.error("redisCachePut不可使用于返回值为空");
			return 0;
		}   		
		String rtStr = returnObj.toString();
		if(!Character.isDigit(rtStr.charAt(0))) {
			logger.error("redisCachePut不可使用于返回值不为数字");
			return 0;
		}		
		int result = Integer.parseInt(rtStr);	
		//插入数据库失败
		if(result == 0) {
			return 0;
		}
    	
    	//获取调用的路径
		String path = getMethodPath(joinPoint);
		//根据values获取object里的值，并生成用于redis存储的对象
	    Object sourceObject = joinPoint.getArgs()[0];
		Class<? extends Object> cl = sourceObject.getClass();
    	//获取key在对象中的值
		String redisKey = getRedisKey(rd, sourceObject, cl);
    	//先淘汰缓存，再进行sql，防止数据不一致
        jsonRedisTemplate.delete(redisKey);	   		
		
		//插入数据库成功
		//如果values没有值，那么redis对应的value为输入对象；否则根据输入参数重新生成对象
    	if(rd.names() == null) {
            //存入目标对象,key=类名:keyvalue
    		jsonRedisTemplate.opsForValue().set(redisKey, sourceObject);
    	}else {   		
			Map jsonMap = new HashMap<String,Object>();
    		for(String name: rd.names().split(",")) {
    			 try {    
    				   //生成值到新的对象中
    		           String upChar = name.substring(0, 1).toUpperCase();   
    		           String getterStr = "get" + upChar + name.substring(1);  
    		           Method getMethod = cl.getMethod(getterStr, new Class[] {});    
    		           Object objValue = getMethod.invoke(sourceObject, new Object[] {});  
    		           
    		           jsonMap.put(name, objValue);
    		       } catch (Exception e) {    
    		    	   logger.error(e.getMessage(),e);    
    		       }        		
        	}  		
            //存入目标对象,key=类名:keyvalue
    		jsonRedisTemplate.opsForValue().set(redisKey, jsonMap);

    	}
    	return result;
}
    
    
	@Around("annotationAble()&& @annotation(rd)")
	public Object redisCacheAble(ProceedingJoinPoint joinPoint, RedisCacheAble rd) throws Throwable {
		String preKey = rd.value();
		String arg0 = joinPoint.getArgs()[0].toString();
        
        Class returnClassType = ((MethodSignature)joinPoint.getSignature()).getMethod().getReturnType();
								
		//TODO arg0判断
		String key = preKey + ":" +arg0;
		//如果redis中已經有值，直接返回
		String rtObject = (String) jsonRedisTemplate.opsForValue().get(key);
		
		if (rtObject != null) {
			return JacksonUtil.jsonToObj(rtObject, returnClassType);
		}
		
		// 执行函数,如果返回值為空,返回
		Object sourceObject = joinPoint.proceed();
		if (sourceObject == null) {
			return null;
		}
		
        String path = getMethodPath(joinPoint);
		// 根据values获取object里的值，并生成用于redis存储的对象
		Class<? extends Object> cl = sourceObject.getClass();

		// 插入数据库成功
		// 如果values没有值，那么redis对应的value为输入对象；否则根据输入参数重新生成对象
		if (rd.names() == null) {
			// 存入目标对象
			jsonRedisTemplate.opsForValue().set(key, sourceObject);

		} else {
			// 将目标对象特定字段存入redis
			// TODO 不用反射，而用jsonobject；并且只存储需要的字段，而不存整个对象
			//Object targetObject = cl.newInstance();
			Map jsonMap = new HashMap<String,Object>();
			for (String name : rd.names().split(",")) {
				try {
					// 生成值到新的对象中
					String upChar = name.substring(0, 1).toUpperCase();
					String getterStr = "get" + upChar + name.substring(1);
					Method getMethod = cl.getMethod(getterStr, new Class[] {});
					Object objValue = getMethod.invoke(sourceObject, new Object[] {});
					jsonMap.put(name, objValue);
					
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
			// 存入目标对象,key=类名:keyvalue
    		// 纯jsonobject，所占空间更小
    		jsonRedisTemplate.opsForValue().set(key, jsonMap);

		}
		return sourceObject;
	}  
	
	
	private String getRedisKey(RedisCachePut rd, Object sourceObject, Class<? extends Object> cl)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		String key = rd.key();
		key = key.substring(1); //去掉开头的#号，与spring原生注解保持一致
		
		String firstLetter = key.substring(0, 1).toUpperCase();   
        String getter = "get" + firstLetter + key.substring(1);  
        Method method = cl.getMethod(getter, new Class[] {});    
        Object keyValue = method.invoke(sourceObject, new Object[] {});  
        
        String value = rd.value();
      	String redisKey = value+":"+keyValue.toString();
		return redisKey;
	}

	private String getMethodPath(ProceedingJoinPoint joinPoint) {
		String methodName = ((MethodSignature)joinPoint.getSignature()).getMethod().getName();
	    String classFullName =  ((MethodSignature)joinPoint.getSignature()).getMethod().getDeclaringClass().getName();
	    String path = classFullName.substring(classFullName.lastIndexOf(".")+1) + "." + methodName;
		return path;
	}
} 