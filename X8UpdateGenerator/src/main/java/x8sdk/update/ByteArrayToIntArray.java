package x8sdk.update;

public class ByteArrayToIntArray {
    static int[] CRC32_Table = new int[256];
    private static boolean initTable;

    public static int CRC32Software(byte[] pData, int Length) {
        int tmp;
        int j;
        if (!initTable) {
            Table_Init();
            initTable = true;
        }
        int crc = -1;
        int offset = Length / 4;
        for (int i = 0; i < offset; i++) {
            tmp = bytesToInt(new byte[]{pData[i * 4], pData[(i * 4) + 1], pData[(i * 4) + 2], pData[(i * 4) + 3]}, 0);
            for (j = 3; j >= 0; j--) {
                crc = CRC32_Table[((crc >> 24) ^ ((tmp >> (j * 8)) & 255)) & 255] ^ (crc << 8);
            }
        }
        int k = Length % 4;
        if (k > 0) {
            byte[] t = new byte[4];
            for (j = 0; j < k; j++) {
                t[j] = pData[(offset * 4) + j];
            }
            tmp = bytesToInt(t, 0);
            for (j = 3; j >= 0; j--) {
                crc = CRC32_Table[((byte) ((crc >> 24) ^ ((tmp >> (j * 8)) & 255))) & 255] ^ (crc << 8);
            }
        }
        return crc ^ 0;
    }

    public static int bytesToInt(byte[] src, int offset) {
        return (((src[offset] & 255) | ((src[offset + 1] & 255) << 8)) | ((src[offset + 2] & 255) << 16)) | ((src[offset + 3] & 255) << 24);
    }

    public static void Table_Init() {
        for (int i32 = 0; i32 < 256; i32++) {
            int nData32 = i32 << 24;
            int CRC_Reg = 0;
            for (int j32 = 0; j32 < 8; j32++) {
                if (((nData32 ^ CRC_Reg) & Integer.MIN_VALUE) != 0) {
                    CRC_Reg = (CRC_Reg << 1) ^ 79764919;
                } else {
                    CRC_Reg <<= 1;
                }
                nData32 <<= 1;
            }
            CRC32_Table[i32] = CRC_Reg;
        }
    }
}
