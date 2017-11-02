package cn.com.spdbccc.hotelbank.rediscache.annotation;

  
import java.lang.annotation.Documented;  
import java.lang.annotation.ElementType;  
import java.lang.annotation.Retention;  
import java.lang.annotation.RetentionPolicy;  
import java.lang.annotation.Target;  
  
/** 
 * 描述：对插入数据库的结果进行的缓存 
 * @author fw 
 * 创建时间：2017年10月14日 下午10:30:00 
 * @version 1.2.0 
 */  
@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.METHOD)  
@Documented  
public @interface RedisCachePut {  
	String value() default "";  //类名
	String key() default "";   //所需要存的key名
	String names() default ""; //类中需要存的字段
}  