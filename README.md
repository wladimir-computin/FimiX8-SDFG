# Fimi X8 SE SDFG
### Generate sd-card-flashable fr_firmware.bin images for Fimi X8 SE

## How FIMI implemented the update procedure
*While updating the drone via the App, this is what actually happens in the background:*

 0. App querys firmware information from the drone.
 1. App querys firmware information from Fimi's backend api.
 2. App downloads all updated firmware images.
 3. App packs all updaded firmwares into one big ``àll_chips.bin`` file, together with some header (memory layout) and CRC32 checksum information.
 4. App sends ``all_chips.bin`` + a CRC32 checksum as a second file via USB to the RC.
 5. RC forwards firmware via FimiLink4 (propritary network protocol) in realtime to the drone.
 6. Either the drone (FC-Relay, OpenWRT system) or the camera (Ambarella A12, Linux + RTOS system) (I don't know which of both, here's some missing information) gets the firmware and passes it ultimately to the camera.
 7. Camera writes the firmware to the root of the sd-card as ``fr_firmware.bin``.
 8. Drone restarts.
 9. Camera reads the firmware file during the next boot.
 10. Camera checks if ``fr_firmware.bin`` is correctly formatted and if the CRC32 checksum is correct.
 11. Camera unpacks all firmware images into the ``/fr_update`` subdirectory.
 12. Camera runs code to flash each part of the firmware individually. This takes a while and the drone is blinking and beeping wildly. Depending on the firmware type (Gimbal, FC, FC-Relay) etc. the firmware is first transfered to another component of the drone (like FC-Relay) and the flashed by it.
 13. Camera deletes ``fr_firmware.bin`` file and the ``/fr_update`` directory.
 14. Progress is written into ``fr_update.log`` file, which can be read after the drone restarted.

## How sd-card flashing works
 1. Download firmware JSON file. (automatic, if flashing latest available version)
 2. Download needed firmware images from directly FIMIs server or from my github repo. (automatic, if flashing latest available version)
 3. Generate the update package with this tool. 
 4. Place the update package as ``fr_firmware.bin`` and restart the drone. From now on it's like we updating from the App, but skipping steps 0 to 8.
 
## General Information
TODO


The current firmware files are listed by the backend API here:
https://fimiapp-server-frankfurt.mi-ae.com.de/v3/firmware/getFirmwareDetail

## Usage
### Arguments
```
-h, --help             show this help message and exit
-f <path/to/folder>    Path to the firmware directory.
		       Omit: download firmware files automatically from the urls provided by the firmware
		       JSON file.
                       
-i <path/to/file.json> Path to the firmware JSON file.
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

## Installation
Not needed, you just need the Java (8 or greater) runtime installed. After cloning the repo or downloading the jar file, run it from the terminal like this:

``java -jar X8UpdateGenerator-1.0.jar``

### Build
* **Linux/Unix**

``./gradlew build``

* **Windows**

``gradle.bat build``
