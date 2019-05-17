package x8sdk.update.fwpack;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.regex.Pattern;

public class ByteHexHelper {
    private static boolean D = false;

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return "";
        }
        for (byte b : src) {
            String hv = Integer.toHexString(b & 255);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
            stringBuilder.append(" ");
        }
        return stringBuilder.toString().toUpperCase();
    }

    public static String byteToHexString(byte src) {
        StringBuilder stringBuilder = new StringBuilder("");
        String hv = Integer.toHexString(src & 255);
        if (hv.length() < 2) {
            stringBuilder.append(0);
        }
        stringBuilder.append(hv);
        return stringBuilder.toString();
    }

    public static int byteToInt(byte src) {
        return src & 255;
    }

    public static byte[] intToHexBytes(int id) {
        String hexString = Integer.toHexString(id);
        int len = hexString.length();
        while (len < 2) {
            hexString = "0" + hexString;
            len = hexString.length();
        }
        return hexStringToBytes(hexString);
    }

    public static byte[] intToTwoHexBytes(int id) {
        String hexString = Integer.toHexString(id);
        int len = hexString.length();
        while (len < 4) {
            hexString = "0" + hexString;
            len = hexString.length();
        }
        return hexStringToBytes(hexString);
    }

    public static byte[] intToFourHexBytes(int n) {
        return new byte[]{(byte) (n & 255), (byte) ((n >> 8) & 255), (byte) ((n >> 16) & 255), (byte) ((n >> 24) & 255)};
    }

    public static byte[] intToFourHexBytesTwo(int id) {
        String hexString = Integer.toHexString(id);
        int len = hexString.length();
        if (len < 2) {
            hexString = "0" + hexString;
            len = hexString.length();
        }
        while (len < 8) {
            hexString = hexString + "0";
            len = hexString.length();
        }
        return hexStringToBytes(hexString);
    }

    public static byte intToHexByte(int id) {
        String hexString = Integer.toHexString(id);
        int len = hexString.length();
        while (len < 2) {
            hexString = "0" + hexString;
            len = hexString.length();
        }
        return hexStringToByte(hexString);
    }

    public static byte[] hexStringToBytes2(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return new byte[0];
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) ((charToByte(hexChars[pos]) << 4) | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    public static byte hexStringToByte(String hexString) {
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) ((charToByte(hexChars[pos]) << 4) | charToByte(hexChars[pos + 1]));
        }
        return d[0];
    }

    public static byte[] hexStringToBytes(String str) {
        String str1 = str.replace(" ", "");
        byte[] d = new byte[(str1.length() / 2)];
        int i = 0;
        while (i < str1.length()) {
            int c;
            int i2;
            int tmp = str1.substring(i, i + 1).getBytes()[0];
            if (tmp > 96) {
                c = ((tmp - 97) + 10) * 16;
            } else if (tmp > 64) {
                c = ((tmp - 65) + 10) * 16;
            } else {
                c = (tmp - 48) * 16;
            }
            i++;
            tmp = str1.substring(i, i + 1).getBytes()[0];
            if (tmp > 96) {
                i2 = (tmp - 97) + 10;
            } else if (tmp > 64) {
                i2 = (tmp - 65) + 10;
            } else {
                i2 = tmp - 48;
            }
            d[i / 2] = (byte) (c + i2);
            i++;
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String XOR(String hex) {
        byte bytes = (byte) 0;
        if (hex.length() > 0) {
            for (int i = 0; i < hex.length() / 2; i++) {
                bytes = (byte) (hexStringToByte(hex.substring(i * 2, (i * 2) + 2)) ^ bytes);
            }
        }
        return bytesToHexString(new byte[]{bytes});
    }

    public static String currentData() {
        StringBuffer stringBuffer = new StringBuffer();
        DecimalFormat decimalFormat = new DecimalFormat("00");
        Calendar calendar = Calendar.getInstance();
        String year = decimalFormat.format((long) calendar.get(1));
        String month = decimalFormat.format((long) (calendar.get(2) + 1));
        String day = decimalFormat.format((long) calendar.get(5));
        String hour = decimalFormat.format((long) calendar.get(11));
        String minute = decimalFormat.format((long) calendar.get(12));
        String second = decimalFormat.format((long) calendar.get(13));
        stringBuffer.append(year.substring(2, year.length())).append(month).append(day).append(hour).append(minute).append(second).append(decimalFormat.format((long) (calendar.get(7) - 1)));
        System.out.println(stringBuffer.toString());
        return stringBuffer.toString();
    }

    public static String RandomMethod() {
        String hexString = Integer.toHexString((int) (Math.random() * 100.0d));
        int len = hexString.length();
        while (len < 2) {
            hexString = "0" + hexString;
            len = hexString.length();
        }
        return hexString;
    }

    public static String packLength(String str) {
        String hexLength = Integer.toHexString(str.length() / 2);
        int len = hexLength.length();
        while (len < 4) {
            hexLength = "0" + hexLength;
            len = hexLength.length();
        }
        return hexLength;
    }

    public static String checkedSite(int site) {
        String hexLength = Integer.toHexString(site);
        int len = hexLength.length();
        while (len < 2) {
            hexLength = "0" + hexLength;
            len = hexLength.length();
        }
        return hexLength;
    }

    public static String packLength(int dataLen) {
        String hexLength = Integer.toHexString(dataLen);
        int len = hexLength.length();
        while (len < 4) {
            hexLength = "0" + hexLength;
            len = hexLength.length();
        }
        return hexLength;
    }

    public static int intPackLength(String str) {
        return Integer.valueOf(str, 16).intValue();
    }

    public static int intPackLength(byte[] str) {
        return Integer.valueOf(bytesToHexString(str), 16).intValue();
    }

    public static String packVerify(String target, String source, String packLengths, String counter, String commandWord, String dataArea) {
        return XOR(target + source + packLengths + counter + commandWord + dataArea);
    }

    public static String dpuString(String str) {
        String buffer = "";
        if (str == null || str.length() <= 0) {
            return buffer;
        }
        String result = bytesToHexString((str + "\u0000").getBytes());
        buffer = packLength(result) + result;
        System.out.println("resultLength==" + buffer);
        return buffer;
    }

    public static String binaryString2hexString(String bString) {
        if (bString == null || bString.equals("")) {
            return "";
        }
        int i;
        if (bString.length() % 8 != 0) {
            int addLen = 8 - (bString.length() % 8);
            for (i = 0; i < addLen; i++) {
                bString = bString + "0";
            }
            System.out.println("choiceItem = " + bString);
        }
        StringBuffer tmp = new StringBuffer();
        for (i = 0; i < bString.length(); i += 4) {
            int iTmp = 0;
            for (int j = 0; j < 4; j++) {
                iTmp += Integer.parseInt(bString.substring(i + j, (i + j) + 1)) << ((4 - j) - 1);
            }
            tmp.append(Integer.toHexString(iTmp));
        }
        System.out.println("tmp.toString() = " + tmp.toString());
        return tmp.toString();
    }

    public static String hexString2binaryString(String hexString) {
        if (hexString == null || hexString.length() % 2 != 0) {
            return null;
        }
        String bString = "";
        for (int i = 0; i < hexString.length(); i++) {
            String tmp = "0000" + Integer.toBinaryString(Integer.parseInt(hexString.substring(i, i + 1), 16));
            bString = bString + tmp.substring(tmp.length() - 4);
        }
        return bString;
    }

    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            dest = Pattern.compile("\t|\r|\n").matcher(str).replaceAll("");
        }
        return dest.trim();
    }

    public static ArrayList<String> toStringArray(byte[] data) {
        if (data != null) {
            int total_bytes = data.length;
            if (total_bytes >= 3) {
                int walkthrough = 0;
                ArrayList<String> arrayList = new ArrayList();
                while (walkthrough < total_bytes - 1) {
                    int temp_len = (data[walkthrough] << 8) | data[walkthrough + 1];
                    byte[] str_bytes = new byte[(temp_len - 1)];
                    System.arraycopy(data, walkthrough + 2, str_bytes, 0, temp_len - 1);
                    arrayList.add(new String(str_bytes));
                    walkthrough += temp_len + 2;
                }
                return arrayList;
            }
        }
        return null;
    }

    public static byte[] appendByteArray(byte[] src, byte[] data) {
        if (src.length <= 0 || data.length <= 0) {
            throw new IllegalArgumentException("字节数组参数错误");
        }
        byte[] ret = new byte[(src.length + data.length)];
        System.arraycopy(src, 0, ret, 0, src.length);
        System.arraycopy(data, 0, ret, src.length, data.length);
        return ret;
    }

    public static String calculateSingleFileMD5sum(File file) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        FileInputStream fis = new FileInputStream(file);
        byte[] buff = new byte[256];
        while (true) {
            int readLen = fis.read(buff);
            if (readLen == -1) {
                break;
            }
            md5.update(buff, 0, readLen);
        }
        fis.close();
        StringBuilder sb = new StringBuilder();
        for (byte b : md5.digest()) {
            sb.append(new Formatter().format("%02x", new Object[]{Byte.valueOf(b)}));
        }
        return sb.toString();
    }

    public static String parseAscii(String str) {
        StringBuilder sb = new StringBuilder();
        byte[] bs = str.getBytes();
        for (byte toHex : bs) {
            sb.append(toHex(toHex));
        }
        return sb.toString();
    }

    public static String toHex(int n) {
        StringBuilder sb = new StringBuilder();
        if (n / 16 == 0) {
            return toHexUtil(n);
        }
        sb.append(toHex(n / 16)).append(toHexUtil(n % 16));
        return sb.toString();
    }

    private static String toHexUtil(int n) {
        String rt = "";
        switch (n) {
            case 10:
                return rt + "A";
            case 11:
                return rt + "B";
            case 12:
                return rt + "C";
            case 13:
                return rt + "D";
            case 14:
                return rt + "E";
            case 15:
                return rt + "F";
            default:
                return rt + n;
        }
    }

    public static byte[] getBooleanArray(byte b) {
        byte[] array = new byte[8];
        for (int i = 7; i >= 0; i--) {
            array[i] = (byte) (b & 1);
            b = (byte) (b >> 1);
        }
        return array;
    }

    public static String byteToBit(byte b) {
        return "" + ((byte) ((b >> 7) & 1)) + ((byte) ((b >> 6) & 1)) + ((byte) ((b >> 5) & 1)) + ((byte) ((b >> 4) & 1)) + ((byte) ((b >> 3) & 1)) + ((byte) ((b >> 2) & 1)) + ((byte) ((b >> 1) & 1)) + ((byte) ((b >> 0) & 1));
    }

    public static byte[] shortToBytes(short sNum) {
        return new byte[]{(byte) ((sNum >> 8) & 255), (byte) (sNum & 255)};
    }
}
