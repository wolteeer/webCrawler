package org.humanityx.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Hash utils.
 * sha1(hello)   = aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d
 * md5(hello)    = 5d41402abc4b2a76b9719d911017c592
 * md5b64(")     = XUFAKrxLKna5cZ2REBfFkg==
 * md5b64safe(") = XUFAKrxLKna5cZ2REBfFkg
 *
 * Given that MessageDigests are *not* thread safe, this class is @ProbablyThreadSafe
 * @author Arvid
 * @version 2-6-2015 - 20:38
 */
@ProbablyThreadSafe
public class Hash {

    public static String md5(String s){
        return DigestUtils.md5Hex(s);
    }

    public static String md5base64(String s){
        return Base64.encodeBase64String(DigestUtils.md5(s));
    }

    public static String md5base64safe(String s){
        return Base64.encodeBase64URLSafeString(DigestUtils.md5(s));
        // Alternatively...
        /*try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(s.getBytes("UTF-8"));
            return Base64.encodeBase64URLSafeString(digest);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;*/
    }


    public static void main(String[] args) {
        System.out.println("md5()           = " + md5("hello"));
        System.out.println("md5base64()     = " + md5base64("hello"));
        System.out.println("md5base64safe() = " + md5base64safe("hello"));
    }

}
