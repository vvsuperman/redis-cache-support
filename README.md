# rediscachesupport
## 一 解决的问题
解决redis在分布式使用中的2个问题：
1. redis中的数据是供微服务中的所有服务使用，所以不能直接将对象进行序列化否则即使同一个对象，在不同的服务中由于包不同，无法转出。所以，把对象转为字符串的形式存入数据库，如此以来就任何服务都能够将使用redis中的数据
2. 缩减redis的存储空间。假设一个对象有30+字段，实际上并用不到这么多，如果全部扔到redis中必然会造成空间的浪费，若只存需要的字段，必然会大大的缩小redis的存储空间

## 二 使用方式
以例子来说明

	@RedisCacheAble(value="xxoo",names = "name,age" )
	public Person testAble(String key) { 
	    //  exec sql query
	}

1 首先在redis中根据key=xxxoo:key查询，如果有值，根据key所对应的json数组生成Person对象，并返回
2 执行查询语句   
3 根据Person中的name，age字段生成json数组，并存到redis中


	@RedisCachePut(value="xxoo",key="#name",names= "name,age")
		public int testPut(Person person) { 
	    //  exec sql insert
		}
1 执行插入操作
2 根据person中的name，age生成json数组，并根据key=xxoo:person.name存入数据库

## 三 如何引入
直接引入即可，记得要扫描包哦
