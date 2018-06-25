package cn.com.earnfish.rediscache.annotation;

  
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
 * 
 * @param values key的前缀
 * @param names 需要保存的字段，比如我们返回对象Person，person里面有name,gender,age,names="name,gender"，那么在redis中会选择性的存储name,gender字段
 * 
 */  
@Retention(RetentionPolicy.RUNTIME)  
@Target(ElementType.METHOD)  
@Documented  
public @interface RedisCacheAble {  
	String value() default "";   //key名称、前缀
	String names() default "";  //所需要包含的键值
	
}  