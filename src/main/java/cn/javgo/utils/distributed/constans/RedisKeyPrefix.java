package cn.javgo.utils.distributed.constans;

/**
 * Desc: Redis 前缀常量
 *
 * @author javgo
 * @create 2024-08-04 18:02
 */
public interface RedisKeyPrefix {

    /**
     * 基本前缀
     */
    String BASE_PREFIX = "javgo_cn:";

    /**
     * sessionId 前缀
     */
    String SESSION_KEY = BASE_PREFIX + "sessionId:";

    /**
     * 数据权限 前缀
     */
    String DATA_ROLE_KEY = BASE_PREFIX + "data_role:";

    /**
     * 数据权限 授权 前缀
     */
    String DATA_ROLE_AUTHORIZED_KEY = BASE_PREFIX + "data_role_authorized:";

    /**
     * 锁 前缀
     */
    String LOCK_KEY = BASE_PREFIX + "lock:";

    /**
     * 后台权限 前缀
     */
    String BACKEND_PERMISSION = BASE_PREFIX + "backendPermission:";

    /**
     * 后台权限 url-role 映射 前缀
     */
    String BACKEND_PERMISSION_URL_ROLE_MAP = BASE_PREFIX + "backendPermissionUrlRoleMap:";

    /**
     * 前台权限 前缀
     */
    String ALL_FRONTEND_PERMISSION = BASE_PREFIX + "allFrontEndPermission:";

    /**
     * 前台权限 id-dto 映射 前缀
     */
    String ALL_PERMISSION_ID2DTO_MAP = BASE_PREFIX + "allPermissionId2DTOMap:";

    /**
     * 前台权限 字段权限 前缀
     */
    String ALL_FIELD_PERMISSION = BASE_PREFIX + "allFieldPermission:";

    // ......
}
