package com.admitgenius.model;

/**
 * Defines the roles a user can have within the system.
 */
public enum UserRole {
    USER, // 普通注册用户 (Regular registered user)
    EXPERT, // 专家用户 (Expert user)
    SCHOOL_ASSISTANT, // 择校助手 (School Assistant)
    ADMIN // 系统管理员 (System administrator)
}