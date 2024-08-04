# JAVGO-UTILS

日常开发常用工具类：

* [批量递归修改文件名 - RenameFileUtil](src/main/java/cn/javgo/utils/file/RenameFileUtil.java)
* [分布式锁设计方案 - DistributedLock](src/main/java/cn/javgo/utils/distributed/DistributedLock.java)
  * [DistributedLock.java](src/main/java/cn/javgo/utils/distributed/DistributedLock.java)
  * [AbstractDistributedLock.java](src/main/java/cn/javgo/utils/distributed/impl/AbstractDistributedLock.java)
  * [RedisDistributedLockService.java](src/main/java/cn/javgo/utils/distributed/impl/RedisDistributedLockService.java)
  * [RedisKeyPrefix.java](src/main/java/cn/javgo/utils/distributed/constans/RedisKeyPrefix.java)
  * [RedisLock.java](src/main/java/cn/javgo/utils/distributed/anno/RedisLock.java)
  * [RedisLockSupport.java](src/main/java/cn/javgo/utils/distributed/aspect/RedisLockSupport.java)
* [通用 API 接口封装 - ApiResponse](src/main/java/cn/javgo/utils/common/ApiResponse.java)
* [全局异常处理 - GlobalExceptionHandler](src/main/java/cn/javgo/utils/exception/GlobalExceptionHandler.java)