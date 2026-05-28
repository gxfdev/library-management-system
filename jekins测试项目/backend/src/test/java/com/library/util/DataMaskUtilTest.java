package com.library.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("数据脱敏工具测试")
class DataMaskUtilTest {

    @Test
    @DisplayName("手机号脱敏")
    void maskPhone() {
        assertEquals("138****8000", DataMaskUtil.maskPhone("13800138000"));
        assertEquals("139****9999", DataMaskUtil.maskPhone("13912349999"));
        assertNull(DataMaskUtil.maskPhone(null));
        assertEquals("", DataMaskUtil.maskPhone(""));
        assertEquals("123", DataMaskUtil.maskPhone("123"));
    }

    @Test
    @DisplayName("邮箱脱敏")
    void maskEmail() {
        assertEquals("a***n@example.com", DataMaskUtil.maskEmail("admin@example.com"));
        assertEquals("t***t@test.cn", DataMaskUtil.maskEmail("test@test.cn"));
        assertEquals("a***@x.com", DataMaskUtil.maskEmail("ab@x.com"));
        assertNull(DataMaskUtil.maskEmail(null));
        assertEquals("invalid", DataMaskUtil.maskEmail("invalid"));
    }

    @Test
    @DisplayName("身份证脱敏")
    void maskIdCard() {
        assertEquals("1101**********2234", DataMaskUtil.maskIdCard("110101199001012234"));
        assertNull(DataMaskUtil.maskIdCard(null));
        assertEquals("1234567", DataMaskUtil.maskIdCard("1234567"));
    }

    @Test
    @DisplayName("姓名脱敏")
    void maskName() {
        assertEquals("张*", DataMaskUtil.maskName("张三"));
        assertEquals("张*丰", DataMaskUtil.maskName("张三丰"));
        assertEquals("李*华", DataMaskUtil.maskName("李晓华"));
        assertEquals("王", DataMaskUtil.maskName("王"));
        assertNull(DataMaskUtil.maskName(null));
    }

    @Test
    @DisplayName("银行卡脱敏")
    void maskBankCard() {
        assertEquals("**** **** **** 5678", DataMaskUtil.maskBankCard("62220212345678905678"));
        assertNull(DataMaskUtil.maskBankCard(null));
        assertEquals("1234567", DataMaskUtil.maskBankCard("1234567"));
    }
}
