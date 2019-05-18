import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import util.SimpleLogger;
import x8sdk.update.UpdateUtil;
import x8sdk.update.UpfirewareDto;
import x8sdk.update.fwpack.FirmwareBuildPack;
import x8sdk.update.fwpack.FirmwareType;
import x8sdk.update.fwpack.FwInfo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Main {
    private static String[] choices = Arrays.stream(FirmwareType.values()).map(name -> {return name.name().toLowerCase();}).filter(name -> {return !name.equals("unknown");} ).toArray(String[]::new);

    private static Namespace doArgParserstuff(String[] args){
        ArgumentParser parser = ArgumentParsers.newFor("Main")
                .locale(Locale.ENGLISH)
                .build()
                .description("Generate fr_firmware.bin images for the Fimi X8 SE");

        parser.addArgument("-f")
                .dest("fwfolder")
                .required(false)
                .type(String.class)
                .metavar("<path/to/folder>")
                .setDefault("fw-download")
                .help("Path to the firmware directory.\nOmit: download firmware files automatically from the urls provided by the firmware JSON file.");

        parser.addArgument("-i")
                .dest("jsonfile")
                .required(false)
                .type(String.class)
                .metavar("<path/to/file.json>")
                .setDefault("")
                .help("Path to the firmware JSON file.\nPass \"auto\": download it automatically from FIMIs firmware server.\nOmit: search for *.json in <firmware folder>.");

        parser.addArgument("-u")
                .dest("firmwaretypes")
                .required(false)
                .type(String.class)
                .nargs("+")
                .choices(choices)
                .help("The firmware types to include in fr_firmware.bin.\nOne or more types separated with space.");

        parser.addArgument("-c")
                .dest("md5ignore")
                .required(false)
                .type(String.class)
                .setDefault("")
                .help("Don't check MD5 checksum between firmware file and JSON file. Make sure you know what you are doing.");

        parser.addArgument("-o")
                .dest("outputfile")
                .required(false)
                .type(String.class)
                .setDefault("")
                .metavar("<fr_firmware.bin>")
                .help("Path to outputfile. Omit to create firmware in the working directory.");

        try {
            return parser.parseArgsOrFail(args);
        } catch (Exception x){
            System.exit(0);
        }
        return null;
    }

    public static void main(String[] args) {

        Namespace res = doArgParserstuff(args);

        System.out.println();
        System.out.println(">>>>FIMI X8 SE firmware file generator<<<<");
        System.out.println();

        Path inputfile = Paths.get(res.getString("jsonfile"));
        Path fwfolder = Paths.get(res.getString("fwfolder"));
        List<String> updatetypes = res.get("firmwaretypes");
        Path outputfile = Paths.get(res.getString("outputfile"));
        boolean md5ignore = res.getString("md5ignore").equalsIgnoreCase("MD5IGNORE");


        if(!Files.isDirectory(fwfolder)){
            try {
                Files.createDirectory(fwfolder);
            } catch (Exception x){
                SimpleLogger.log(SimpleLogger.LogType.ERROR, "Could not find or create firmware folder" + fwfolder.toString() + "!");
                System.exit(1);
            }
        }

        if(inputfile.toString().isEmpty()){
            SimpleLogger.log(SimpleLogger.LogType.INFO, "No firmare JSON file specified, will try to find one in: " + fwfolder);
            try (Stream<Path> files = Files.walk(fwfolder)) {
                List<Path> jsons = files
                        .filter(f -> f.getFileName().toString().endsWith(".json"))
                        .collect(Collectors.toList());
                if(jsons.size() == 1){
                    inputfile = jsons.get(0);
                } else {
                    SimpleLogger.log(SimpleLogger.LogType.ERROR, "Could not find firmware JSON file.");
                    System.exit(1);
                }
            } catch (Exception x){
                x.printStackTrace();
                System.exit(1);
            }
        }
        else if(inputfile.toString().equals("auto")) {
            SimpleLogger.log(SimpleLogger.LogType.INFO, "No firmare JSON file specified, will try to get one from: " + UpdateUtil.fimi_api_url_de);
            try {
                inputfile = UpdateUtil.getFirmwareJsonFromServer(fwfolder);
                SimpleLogger.log(SimpleLogger.LogType.DEBUG, "Done!");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        else if(!Files.exists(inputfile)){
            SimpleLogger.log(SimpleLogger.LogType.ERROR, "File " + inputfile.toString() + " not found!");
            System.exit(1);
        }

        List<FirmwareType> firmwareTypes = new ArrayList<>();
        if(updatetypes == null || updatetypes.isEmpty()){
            SimpleLogger.log(SimpleLogger.LogType.INFO, "No firmware type specified, will only parse the firmware JSON file and try to download all missing FIMI X8 SE firmware images");
        }
        else {
            for(String choice : updatetypes) {
                FirmwareType type = FirmwareType.fromString(choice);
                if (type != FirmwareType.UNKNOWN) {
                    firmwareTypes.add(type);
                } else {
                    SimpleLogger.log(SimpleLogger.LogType.ERROR, "Specified unknown firmware type: " + choice);
                    System.exit(1);
                }
            }
        }


        try {
            List<UpfirewareDto> upfirewareDtos = UpdateUtil.UpfirewareDtosFromJSON(inputfile);
            SimpleLogger.log(SimpleLogger.LogType.DEBUG, "Got " + upfirewareDtos.size() + " firmwares.");

            upfirewareDtos = UpdateUtil.filterX8sFirmware(upfirewareDtos);
            SimpleLogger.log(SimpleLogger.LogType.DEBUG, upfirewareDtos.size() + " are FIMI X8 SE firmwares.");

            List<FwInfo> fwInfos = UpdateUtil.toFwInfo(upfirewareDtos);

            if(firmwareTypes.size() > 0) {
                List<FwInfo> filteredFwInfos = new ArrayList<>();

                for (FwInfo fwInfo : fwInfos) {
                    if (firmwareTypes.contains(fwInfo.getFirmwareType())) {
                        fwInfo.setFilename(Paths.get(fwfolder.toString(), fwInfo.getFilename().toString()));
                        if (!fwInfo.getFilename().toFile().exists()) {
                            fwInfo.setFilename(UpdateUtil.getFirmwareImageFromServer(fwInfo));
                        }
                        if (fwInfo.getFilename() != null) {
                            if (!md5ignore) {
                                if (UpdateUtil.checkMD5(fwInfo)) {
                                    SimpleLogger.log(SimpleLogger.LogType.DEBUG, fwInfo.getFilename().getFileName() + " passed MD5 check..");
                                } else {
                                    SimpleLogger.log(SimpleLogger.LogType.ERROR, fwInfo.getFilename().getFileName() + " failed MD5 check.");
                                    System.exit(1);
                                }
                            }
                            filteredFwInfos.add(fwInfo);
                        } else {
                            SimpleLogger.log(SimpleLogger.LogType.ERROR, "Failed to get " + fwInfo.getFirmwareType() + " firmware.");
                            System.exit(1);
                        }
                    }
                }

                try {
                    new FirmwareBuildPack(filteredFwInfos).createUpdatePkg(outputfile, fwfolder);
                } catch (Exception x) {
                    SimpleLogger.log(SimpleLogger.LogType.ERROR, "Failed to generate firmware");
                    x.printStackTrace();
                    System.exit(1);
                }
            } else {
                SimpleLogger.log(SimpleLogger.LogType.INFO, "Downloading all firmware images.");
                UpdateUtil.getFirmwareImageFromServer(fwfolder, fwInfos);
            }

        } catch (Exception x) {
            SimpleLogger.log(SimpleLogger.LogType.ERROR, "Failed to parse firmware JSON file");
            x.printStackTrace();
            System.exit(1);
        }

    }
}
