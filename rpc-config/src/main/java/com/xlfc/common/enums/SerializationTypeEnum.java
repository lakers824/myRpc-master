package com.xlfc.common.enums;

public enum SerializationTypeEnum {

    //枚举成员（可以有多个，逗号间隔）
    KRYO((byte) 0x01, "kryo");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        //当定义一个枚举类型时，每一个枚举类型成员都可以看作是 Enum 类的实例
        //values返回所有成员，这里是遍历所有成员
        for (SerializationTypeEnum c : SerializationTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }

    SerializationTypeEnum(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    public byte getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
