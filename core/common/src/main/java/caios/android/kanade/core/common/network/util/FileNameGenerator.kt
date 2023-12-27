package caios.android.kanade.core.common.network.util

import android.text.TextUtils
import org.apache.commons.lang3.ArrayUtils
import org.apache.commons.lang3.StringUtils
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

private val validChars = ("abcdefghijklmnopqrstuvwxyz"
        + "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        + "0123456789"
        + " _-").toCharArray()

const val MAX_FILENAME_LENGTH = 242 // limited by CircleCI
private const val MD5_HEX_LENGTH = 32

/**
 * This method will return a new string that doesn't contain any illegal
 * characters of the given string.
 */
fun generateFileName(input: String): String {
    var string = input
    string = StringUtils.stripAccents(string)
    val buf = StringBuilder()
    for (element in string) {
        if (Character.isSpaceChar(element)
            && (buf.isEmpty() || Character.isSpaceChar(buf[buf.length - 1]))
        ) {
            continue
        }
        if (ArrayUtils.contains(validChars, element)) {
            buf.append(element)
        }
    }
    val filename = buf.toString().trim { it <= ' ' }
    return if (TextUtils.isEmpty(filename)) {
        randomString(8)
    } else if (filename.length >= MAX_FILENAME_LENGTH) {
        filename.substring(
            0,
            MAX_FILENAME_LENGTH - MD5_HEX_LENGTH - 1
        ) + "_" + md5(filename)
    } else {
        filename
    }
}

private fun randomString(length: Int): String {
    val sb = java.lang.StringBuilder(length)
    for (i in 0..<length) {
        sb.append(validChars[(Math.random() * validChars.size).toInt()])
    }
    return sb.toString()
}

private fun md5(md5: String): String? {
    return try {
        val md = MessageDigest.getInstance("MD5")
        val array = md.digest(md5.toByteArray(charset("UTF-8")))
        val sb = java.lang.StringBuilder()
        for (b in array) {
            sb.append(Integer.toHexString(b.toInt() and 0xFF or 0x100).substring(1, 3))
        }
        sb.toString()
    } catch (e: NoSuchAlgorithmException) {
        null
    } catch (e: UnsupportedEncodingException) {
        null
    }
}
