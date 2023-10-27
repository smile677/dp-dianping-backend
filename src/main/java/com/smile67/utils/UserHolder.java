package com.smile67.utils;

import com.smile67.dto.UserDTO;

public class UserHolder {
    // 泛型使用UserDto隐藏用户敏感信息
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUser(UserDTO user) {
        tl.set(user);
    }

    public static UserDTO getUser() {
        return tl.get();
    }

    public static void removeUser() {
        tl.remove();
    }
}
