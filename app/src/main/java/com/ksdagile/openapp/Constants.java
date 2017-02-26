package com.ksdagile.openapp;

/**
 * Created by user on 15/12/2016.
 */

public class Constants {
    public static final String TAG = "OpenApp";
    public static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAi6PipyTJcokeNxXfdkqxE7yWak+rKJyzRTSbw8AXAtfqlr+mb/YYH0UMV8wEazy/lG6RTcdN/rYuGDov4h3VHHaJ0AL2PUUtSmC9w9gjoOFmo4pB5SvM0kHgTZg+CjpJ9qzpGz5k7ej6QBgYciTZVe0BSHKN+ozkAGgKjwUsLIaNPHto66ocZC5aIvhf48hIIEwOYPk50xkSNcWv442hssKG7W3c8966JTncwBpj4ep8ewTXix2yo2JykDBRLgefxPfP0J9KXI9a267MpEKFtJwmQMrpIv/IgJ0xtlDNI2HJPlJQxYCJcBdlB/ls4rkFW7V+ZsR+g5E5yTBMkqHK2QIDAQAB";
    public static final byte[] SALT = new byte[]{0x5c, (byte)0xc7, (byte)0xb2, (byte)0xf7, 0x01, 0x69, 0x41, 0x15, (byte)0x92, (byte)0xee, (byte)0xa1, 0x01, 0x52, 0x2a, 0x4e, (byte)0x84, 0x12, 0x27, (byte)0xee, (byte)0xce };
    public static final int LICENSE_NO_ANSWER = 0;
    public static final int LICENSE_ALLOWED = 1;
    public static final int LICENSE_REJECTED = -1;
}
