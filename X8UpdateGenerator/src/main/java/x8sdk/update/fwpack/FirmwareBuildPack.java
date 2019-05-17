package x8sdk.update.fwpack;

import util.SimpleLogger;
import x8sdk.update.ByteArrayToIntArray;
import x8sdk.update.FileUtil;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;

public class FirmwareBuildPack {
    public static final int BUFSIZE = 8192;
    public static final int ERROR = 1;
    public static final Path WORKINGDIR = Paths.get("tmp");
    public static final Path PKG_CRC = Paths.get(WORKINGDIR.toString(), "pgk_crc");
    public static final Path PKG_HEADER_FILE = Paths.get(WORKINGDIR.toString(),"update_file_header");
    public static final Path PKG_NOCRC = Paths.get(WORKINGDIR.toString(),"pgk_no_crc");
    public static final Path PKG_UPDATE_FILE = Paths.get(WORKINGDIR.toString(),"fr_firmware.bin"); //was all_chips.bin
    public static final Path PKG_UPDATE_OUTFILE = Paths.get(WORKINGDIR.toString(),"update_fileData");
    public static final int SUCCESS = 0;

    private List<FwInfo> fws;

    public FirmwareBuildPack(List<FwInfo> fws) {
        this.fws = fws;
        try {
            deleteTempDir();
            FileUtil.createFileAndPaperFile(WORKINGDIR);
        } catch (Exception x){
            SimpleLogger.log(SimpleLogger.LogType.ERROR, "Failed to create temp directory.");
        }
    }

    public void createUpdatePkg(Path filename, Path fwfolder){
        try {

            SimpleLogger.log(SimpleLogger.LogType.INFO, "Copying firmware files");
            mergFwDataFile(PKG_UPDATE_OUTFILE);

            SimpleLogger.log(SimpleLogger.LogType.INFO, "Generating header");
            FileUtil.createFile(PKG_HEADER_FILE);
            FileUtil.addFileContent(PKG_HEADER_FILE, getfwPackInfo());
            FileUtil.addFileContent(PKG_HEADER_FILE, getFwInfo());
            FileUtil.meragerFiles(PKG_NOCRC, new Path[]{PKG_HEADER_FILE, PKG_UPDATE_OUTFILE});

            SimpleLogger.log(SimpleLogger.LogType.INFO, "Calculating CRC32 checksum");
            byte[] crc = getPackCRC(PKG_NOCRC);
            FileUtil.createFile(PKG_CRC);
            FileUtil.addFileContent(PKG_CRC, crc);

            SimpleLogger.log(SimpleLogger.LogType.INFO, "Merging everything together");
            FileUtil.meragerFiles(PKG_UPDATE_FILE, new Path[]{PKG_CRC, PKG_NOCRC});

            SimpleLogger.log(SimpleLogger.LogType.INFO, "Cleaning up");
            String updateFileName;
            if(filename == null || filename.toString().isEmpty()) {
                updateFileName = "fr_firmware";
                for (FwInfo fw : fws) {
                    updateFileName += "_" + fw.getFirmwareType().name().toLowerCase();
                }
                updateFileName += ".bin";
            } else {
                updateFileName = filename.toString();
            }
            Files.copy(PKG_UPDATE_FILE, Paths.get(updateFileName), StandardCopyOption.REPLACE_EXISTING);
            deleteTempDir();

            SimpleLogger.log(SimpleLogger.LogType.INFO, "Generated firmware: " + updateFileName);


        } catch (Exception x){
            SimpleLogger.log(SimpleLogger.LogType.ERROR, "Well, that didn't worked so well.");
            x.printStackTrace();
        }
    }

    public byte[] getfwPackInfo() {
        byte[] packInfo = new byte[124];
        System.arraycopy(ByteHexHelper.shortToBytes((short) 0), 0, packInfo, 0, 2);
        System.arraycopy(ByteHexHelper.shortToBytes((short) 0), 0, packInfo, 2, 2);
        packInfo[4] = (byte) this.fws.size();
        System.arraycopy(ByteHexHelper.intToFourHexBytes(0), 0, packInfo, 5, 4);
        return packInfo;
    }

    public byte[] getPackCRC(Path fileName) throws Exception{
        if(fileName.toFile().exists()) {
            byte[] fileBytes = FileUtil.getFileBytes(fileName);
            return ByteHexHelper.intToFourHexBytes(ByteArrayToIntArray.CRC32Software(fileBytes, fileBytes.length));
        } else {
            SimpleLogger.log(SimpleLogger.LogType.ERROR, "File " + fileName + " not found!");
            throw new FileNotFoundException(fileName.toString());
        }
    }

    public byte[] getFwInfo() {
        int count = this.fws.size();
        byte[] fwInfos = new byte[(count * 64)];
        int header = fwInfos.length + 128;
        int fwLen = 0;
        for (int i = 0; i < count; i++) {
            byte[] oneFw = getOneFwInfo((FwInfo) this.fws.get(i));
            if (i > 0) {
                fwLen = (int) (FileUtil.getFileLenght(((FwInfo) this.fws.get(i - 1)).getFilename()) + ((long) fwLen));
            }
            byte[] addr = ByteHexHelper.intToFourHexBytes(fwLen + header);
            System.arraycopy(addr, 0, oneFw, 0, 4);
            System.arraycopy(oneFw, 0, fwInfos, i * 64, 64);
        }
        return fwInfos;
    }

    public byte[] getOneFwInfo(FwInfo fw) {
        byte[] iByte = new byte[64];
        System.arraycopy(ByteHexHelper.intToFourHexBytes((int) FileUtil.getFileLenght(fw.getFilename())), 0, iByte, 4, 4);
        iByte[8] = fw.getModelId();
        iByte[9] = fw.getTypeId();
        iByte[10] = fw.getForceType(); //isForceSign
        System.arraycopy(ByteHexHelper.shortToBytes(fw.getSoftwareVer()), 0, iByte, 11, 2);
        iByte[13] = fw.getStepVer(); //no idea what that's for
        return iByte;
    }

    public void mergFwDataFile(Path outFile) throws Exception{
        FileChannel outChannel = null;
        try {
            outChannel = new FileOutputStream(outFile.toFile()).getChannel();
            for (FwInfo fw : this.fws) {
                SimpleLogger.log(SimpleLogger.LogType.DEBUG, "Copying " + fw.getFilename());
                if(fw.getFilename().toFile().exists()) {
                    FileChannel fc = new FileInputStream(fw.getFilename().toFile()).getChannel();
                    ByteBuffer bb = ByteBuffer.allocate(8192);
                    while (fc.read(bb) != -1) {
                        bb.flip();
                        outChannel.write(bb);
                        bb.clear();
                    }
                    fc.close();
                } else {
                    SimpleLogger.log(SimpleLogger.LogType.ERROR, "File " + fw.getFilename() + " not found!");
                    throw new FileNotFoundException(fw.getFilename().toString());
                }
            }
        } catch (Exception x) {
            throw x;

        } finally {
            if (outChannel != null) {
                try {
                    outChannel.close();
                } catch (IOException e2) {
                }
            }
        }
    }

    private void deleteTempDir(){
        try {
            Files.walk(WORKINGDIR)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (Exception x){}
    }
}
