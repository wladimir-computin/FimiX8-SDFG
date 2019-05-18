# Fimi X8 SE SDFG
### Generate sd-card-flashable fr_firmware.bin images for Fimi X8 SE

## About SD-Card flashing
TODO

## Tutorial
TODO

## Usage
### Arguments
```
  -h, --help             show this help message and exit
  -f <path/to/folder>    Path to the firmware directory.
			 Omit: download firmware files automatically from the urls provided by the firmware
	                 JSON file.
  -i <path/to/file.json>
                         Path to the firmware JSON file.
                         Pass "auto": download it automatically from FIMIs firmware server.
                         Omit: search for *.json in <firmware folder>.
  -u {rc,rc_relay,fc,fc_relay,esc,gimbal,camera,nfz,ota} [...]
                         The firmware types to include in fr_firmware.bin.
                         One or more types separated with space.
  -c MD5IGNORE           Don't check MD5 checksum between firmware file and JSON file.
			 Make sure you know what you are doing.
  -o <fr_firmware.bin>   Path to outputfile. Omit to create firmware in the working directory.

```
### Examples
 * Download JSON firmware file, parse it and download all current firmware images for the Fimi X8 SE.
Stuff will be placed in fw-download folder by default, no firmware is generated:
``java -jar X8UpdateGenerator-1.0.jar -i auto``

* Download JSON firmware file, parse it and download the current Gimbal firmware image. Then generate the fr_firmware.bin file for the Gimbal:
``java -jar X8UpdateGenerator-1.0.jar -i auto -u gimbal``

* Use JSON firmware file from firmware folder (fw-download by default), download the current Gimbal firmware image (if not already downloaded). Then generate the fr_firmware.bin file for the Gimba and place it in the current working directory:
``java -jar X8UpdateGenerator-1.0.jar -u gimbal``

* Same as above, but creates a multipart fr_firmware.bin (Gimbal, ESC, and FC) which will flash multiple firmware parts at once:
``java -jar X8UpdateGenerator-1.0.jar -u gimbal esc fc``

The current firmware files are listed by the backend API here:
https://fimiapp-server-frankfurt.mi-ae.com.de/v3/firmware/getFirmwareDetail
