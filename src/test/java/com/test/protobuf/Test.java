package com.test.protobuf;

import com.google.common.base.Charsets;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.TextFormat;

import java.io.UnsupportedEncodingException;

/**
 * @author xh
 * @date 2024-06-24
 * @Description:
 */
public class Test {
    public static byte[] encode(ProtoTest2.Student student) {
        return student.toByteArray();
    }

    public static ProtoTest2.Student decode(byte[] bytes) throws InvalidProtocolBufferException {
        return ProtoTest2.Student.parseFrom(bytes);
    }

    public static ProtoTest2.Student createStudent() {
        ProtoTest2.Student.Builder builder = ProtoTest2.Student.newBuilder();
        // 当项目编码为 GBK 时，需要这样设置
        String xm = new String("小明".getBytes(), Charsets.UTF_8);
        builder.setName(ByteString.copyFromUtf8(xm));
        builder.setAge(18);
        // 设置学校
        ProtoTest2.School.Builder schoolBuilder = ProtoTest2.School.newBuilder();
        schoolBuilder.setName("宇宙第一中学");
        schoolBuilder.setNumber(1);

        builder.setSchool(schoolBuilder.build());
        return builder.build();
    }

    public static void main(String[] args) {

        ProtoTest2.Student student = createStudent();
        byte[] bytes = encode(student);
        try {
            ProtoTest2.Student student1 = decode(bytes);
            // 打印 byte 数组
            System.out.println(student1.getName().toStringUtf8());
            // 打印字符串
            System.out.println(ByteString.copyFrom(student1.getSchool().getName().getBytes()).toStringUtf8());
            System.out.println(student1);
            System.out.println("Assert equal : --> " + student.equals(student1));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
