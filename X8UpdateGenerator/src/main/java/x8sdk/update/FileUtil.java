package x8sdk.update;

import java.io.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

public class FileUtil {
    public static final int BUFSIZE = 8192;

    /* JADX WARNING: Removed duplicated region for block: B:33:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0025 A:{SYNTHETIC, Splitter:B:15:0x0025} */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0031 A:{SYNTHETIC, Splitter:B:21:0x0031} */
    public static void addFileContent(Path filename, byte[] content) throws Exception {
        //restored manually from code below
        RandomAccessFile r2 = new RandomAccessFile(filename.toFile(), "rw");
        r2.seek(r2.length());
        r2.write(content);
        r2.close();

        /*
        r1 = 0;
        r2 = new java.io.RandomAccessFile;	 Catch:{ IOException -> 0x001f }
        r3 = "rw";
        r2.<init>(r6, r3);	 Catch:{ IOException -> 0x001f }
        r4 = r2.length();	 Catch:{ IOException -> 0x003d, all -> 0x003a }
        r2.seek(r4);	 Catch:{ IOException -> 0x003d, all -> 0x003a }
        r2.write(r7);	 Catch:{ IOException -> 0x003d, all -> 0x003a }
        if (r2 == 0) goto L_0x0040;
    L_0x0014:
        r2.close();	 Catch:{ IOException -> 0x0019 }
        r1 = r2;
    L_0x0018:
        return;
    L_0x0019:
        r0 = move-exception;
        r0.printStackTrace();
        r1 = r2;
        goto L_0x0018;
    L_0x001f:
        r0 = move-exception;
    L_0x0020:
        r0.printStackTrace();	 Catch:{ all -> 0x002e }
        if (r1 == 0) goto L_0x0018;
    L_0x0025:
        r1.close();	 Catch:{ IOException -> 0x0029 }
        goto L_0x0018;
    L_0x0029:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x0018;
    L_0x002e:
        r3 = move-exception;
    L_0x002f:
        if (r1 == 0) goto L_0x0034;
    L_0x0031:
        r1.close();	 Catch:{ IOException -> 0x0035 }
    L_0x0034:
        throw r3;
    L_0x0035:
        r0 = move-exception;
        r0.printStackTrace();
        goto L_0x0034;
    L_0x003a:
        r3 = move-exception;
        r1 = r2;
        goto L_0x002f;
    L_0x003d:
        r0 = move-exception;
        r1 = r2;
        goto L_0x0020;
    L_0x0040:
        r1 = r2;
        goto L_0x0018;
        */
        //throw new UnsupportedOperationException("Method not decompiled: com.fimi.kernel.utils.FileUtil.addFileContent(java.lang.String, byte[]):void");
    }

    public static byte[] getFileBytes(Path filePath) throws Exception{
        byte[] buffer = null;
        FileInputStream fis = new FileInputStream(filePath.toFile());
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
        byte[] b = new byte[1000];
        while (true) {
            int n = fis.read(b);
            if (n != -1) {
                bos.write(b, 0, n);
            } else {
                fis.close();
                bos.close();
                return bos.toByteArray();
            }
        }
    }

    public static long getFileLenght(Path path) {
        return path.toFile().length();
    }

    public static void meragerFiles(Path outFileName, Path[] fileNames) throws Exception {
        FileChannel outChannel = null;
        try {
            outChannel = new FileOutputStream(outFileName.toString()).getChannel();
            for (Path fw : fileNames) {
                FileChannel fc = new FileInputStream(fw.toFile()).getChannel();
                ByteBuffer bb = ByteBuffer.allocate(BUFSIZE);
                while (fc.read(bb) != -1) {
                    ((Buffer)bb).flip();
                    outChannel.write(bb);
                    ((Buffer)bb).clear();
                }
                fc.close();
            }
            try {
                outChannel.close();
            } catch (IOException e) {
            }
        } catch (Exception x) {
            if (outChannel != null) {
                try {
                    outChannel.close();
                } catch (IOException e2) {
                }
            }
            throw x;
        }
    }

    public static void createFile(Path pathName) throws Exception {
        File f = pathName.toFile();
        if (f.exists()) {
            f.delete();
        }
        f.createNewFile();
    }

    public static void createFileAndPaperFile(Path fileName) throws Exception{
        File file = fileName.toFile();
        if (fileName.toString().indexOf(".") != -1) {
            file.createNewFile();
        } else if (!file.exists()) {
            file.mkdir();
        }
    }
}
