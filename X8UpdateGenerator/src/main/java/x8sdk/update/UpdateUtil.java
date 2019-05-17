package x8sdk.update;

import com.google.gson.GsonBuilder;
import util.SimpleLogger;
import x8sdk.update.fwpack.ByteHexHelper;
import x8sdk.update.fwpack.FirmwareType;
import x8sdk.update.fwpack.FwInfo;

import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class UpdateUtil {

    public static final String fimi_api_url_de = "https://fimiapp-server-frankfurt.mi-ae.com.de/v3/firmware/getFirmwareDetail";

    public static List<UpfirewareDto> filterX8sFirmware(List<UpfirewareDto> list) {
        List<UpfirewareDto> upfirewareDtoList = new ArrayList();
        for (UpfirewareDto dto : list) {
            if (isX8sFirmware(dto)) {
                boolean normalUpdate = true;//localFwEntity.getLogicVersion() < dto.getLogicVersion() && "0".equals(dto.getForceSign());
                boolean forceUpdate = false;//localFwEntity.getLogicVersion() < dto.getLogicVersion() && "2".equals(dto.getForceSign());
                boolean ingoreUpdate = false;//localFwEntity.getLogicVersion() != dto.getLogicVersion() && "1".equals(dto.getForceSign());
                boolean isUpdateZone = true;//dto.getEndVersion() == 0 || (localFwEntity.getLogicVersion() <= ((long) dto.getEndVersion()) && localFwEntity.getLogicVersion() >= ((long) dto.getStartVersion()));
                if ((normalUpdate || forceUpdate || ingoreUpdate) && isUpdateZone) {
                    upfirewareDtoList.add(dto);
                }
            }
        }
        return upfirewareDtoList;
    }

    public static List<FwInfo> toFwInfo(List<UpfirewareDto> dtos) {
        List<FwInfo> fws = new ArrayList();
        for (UpfirewareDto upfirewareDto : dtos) {
            FwInfo fwInfo = new FwInfo();
            fwInfo.setModelId((byte) upfirewareDto.getModel());
            fwInfo.setTypeId((byte) upfirewareDto.getType());
            fwInfo.setForceType(Byte.parseByte(upfirewareDto.getForceSign()));
            fwInfo.setFilename(Paths.get(upfirewareDto.getSysName()));
            fwInfo.setSoftwareVer((short) ((int) upfirewareDto.getLogicVersion()));
            fwInfo.setDownloadUrl(upfirewareDto.getFileUrl());
            fwInfo.setChecksumMD5(upfirewareDto.getFileEncode());
            int dateIndex = upfirewareDto.getFileUrl().indexOf("app");
            if(dateIndex != -1){
                try {
                    Date date = new Date();
                    date.setTime(Long.valueOf(upfirewareDto.getFileUrl().substring(dateIndex + 3, dateIndex + 16)));
                    fwInfo.setReleaseDate(date);
                } catch (Exception x){
                    x.printStackTrace();
                }
            }
            fws.add(fwInfo);
        }
        return fws;
    }

    public static Path getFirmwareJsonFromServer(Path folder) throws Exception {
        InputStream in = new URL(fimi_api_url_de).openStream();
        Path filename = Paths.get(folder.toString() + "/getFirmwareDetail.json");
        Files.copy(in, filename, StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    public static Path getFirmwareImageFromServer(FwInfo fwInfo){
        try{
            SimpleLogger.log(SimpleLogger.LogType.INFO, "Downloading " + fwInfo.getFilename().getFileName());
            InputStream in = new URL(fwInfo.getDownloadUrl()).openStream();
            Files.copy(in, fwInfo.getFilename(), StandardCopyOption.REPLACE_EXISTING);
            return fwInfo.getFilename();
        }catch (Exception x){
            SimpleLogger.log(SimpleLogger.LogType.ERROR, "Download from" + fwInfo.getDownloadUrl()+ " failed.");
            x.printStackTrace();
        }
        return null;
    }

    public static void getFirmwareImageFromServer( Path fwfolder, List<FwInfo> fwInfos){
        for (FwInfo fwInfo : fwInfos) {
            if (Arrays.asList(FirmwareType.values()).contains(fwInfo.getFirmwareType())) {
                if(fwInfo.getDownloadUrl().startsWith("http")) {
                    fwInfo.setFilename(Paths.get(fwfolder.toString(), fwInfo.getFilename().toString()));
                    fwInfo.setFilename(UpdateUtil.getFirmwareImageFromServer(fwInfo));
                }
            }
        }
    }

    public static List<UpfirewareDto> UpfirewareDtosFromJSON(Path jsonfile) throws Exception{
        UpfirewareDto[] upfirewareDtosarr = new GsonBuilder().create().fromJson(new FileReader(jsonfile.toFile()), FimiAPIResponse.class).data;
        return Arrays.asList(upfirewareDtosarr);
    }

    public static boolean checkMD5(FwInfo fwInfo){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(Files.readAllBytes(fwInfo.getFilename()));
            byte[] digest = md.digest();
            String calculated = ByteHexHelper.bytesToHexString(digest).replace(" ", "");
            return fwInfo.getChecksumMD5().equalsIgnoreCase(calculated);
        } catch (Exception e){}
        return false;
    }

    public static boolean isX8sFirmware(UpfirewareDto dto) {
        if (dto.getType() == 0 && dto.getModel() == 3) {
            return true;
        }
        if (dto.getType() == 1 && dto.getModel() == 3) {
            return true;
        }
        if (dto.getType() == 9 && dto.getModel() == 1) {
            return true;
        }
        if (dto.getType() == 11 && dto.getModel() == 3) {
            return true;
        }
        if (dto.getType() == 12 && dto.getModel() == 3) {
            return true;
        }
        if (dto.getType() == 14 && dto.getModel() == 0) {
            return true;
        }
        if (dto.getType() == 3 && dto.getModel() == 6) {
            return true;
        }
        if (dto.getType() == 5 && dto.getModel() == 3) {
            return true;
        }
        if (dto.getType() == 10 && dto.getModel() == 3) {
            return true;
        }
        if (dto.getType() == 4 && dto.getModel() == 2) {
            return true;
        }
        if (dto.getType() == 13 && dto.getModel() == 1) {
            return true;
        }
        return false;
    }
}