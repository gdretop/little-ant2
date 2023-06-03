package com.ant.little.common.util;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.security.NoSuchAlgorithmException;

/**
 * @author: little-ant
 * 描述:
 * @date: 2023/2/27
 * @Version 1.0
 **/
public class Md5Util {
    public static String md5(String input) throws NoSuchAlgorithmException {
        String md5str = Hex.encodeHexString(DigestUtils.getMd5Digest().digest(input.getBytes()));
        return md5str;
    }
}
