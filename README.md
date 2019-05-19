# Fimi X8 SE SDFG
### Generate sd-card-flashable fr_firmware.bin images for Fimi X8 SE
 
## General Information

### About this tool
I got my shiny new Fimi X8 SE and wanted to update it before the first flight. During the update the connection got lost and my drone got bricked: After boot, the app was showing the "System is preparing" error message and I couldn't fly or update again.
So I reverse engineered the firmware files and Fimi Navi APK, found how to flash firmware using the sd-card and unbricked my drone with it (I'm still suprised and thankful that it actually worked). Now I want to share my knowledge with you :)

### Update / Downgrade
It is possible to update or downgrade every part of the drone and remote control to any firmware version available.
There is a flag (forceSign=1 in the JSON file) which indicates the software to install the firmware even if the version is lower than the one installed. As it seems, the drone installs lower versions without this flag, too.

**Attention:** Since the drone has many components with individual firmware for each of them, some firmware versions might not play nice with each other. In the worst case, this could even lead to a bricked drone, be warned! You are on the save side if you install the firmware versions which are known working together. See FIMI X8 FW UP-DATE spreadsheet by aiolosimport: https://docs.google.com/spreadsheets/d/1MghIcdNIom1Fj6nHkqPca1OvwRelI0lQ1QDUz1jvz4E

The current firmware files are listed by the backend API here:
https://fimiapp-server-frankfurt.mi-ae.com.de/v3/firmware/getFirmwareDetail

### Flashing remote control
Flashing the remote control (RC and RC-Relay) works exactly the same.
Just download the desired firmware files, pack them with this tool and place them on the sd card of the drone.
Restart the drone and turn on the RC (with smartphone attached).
The drone will connect to the RC and flash it. This takes a while, make sure the RC's battery is charged.
After everything is finished, restart the RC and drone.

### Low Battery
The battery must be at least 35% charged or the drone will refuse to flash. Better don't flash if you have less than 50% battery.

### Update log
The drone logs the update process, so you can see if everything went fine. The log can be found on the root of the sd-card after flashing was finished.

### Flashing multiple firmware at once
FIMI designed the updater so it can flash multiple firmware at once if they are packed into one ``fr_firmware.bin`` file. You can also flash every firmware one by one. I don't know if there are cases when the update will fail because we do it one by one and in the wrong order. I flashed my firmware one by one, so if it fails I know why. The order was: FC, Gimbal, ESC, FC-Relay, OTA, RC-Relay, RC, Camera. But some of the firmware was already updated, so I don't know if this approach is optimal in every case. The tool can generate a fr_firmware.bin file containing multiple firmware, so you have the choice :)

### How FIMI implemented the update procedure
*While updating the drone via the App, this is what actually happens in the background:*

 0. App querys firmware information from the drone.
 1. App querys firmware information from Fimi's backend api.
 2. App downloads all updated firmware images.
 3. App packs all updated firmwares into one big ``àll_chips.bin`` file, together with some header (memory layout) and CRC32 checksum information.
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

### How sd-card flashing works
 1. Download firmware JSON file. (automatic, if flashing latest available version)
 2. Download needed firmware images from directly FIMIs server or from my github repo. (automatic, if flashing latest available version)
 3. Generate the update package with this tool. 
 4. Place the update package as ``fr_firmware.bin`` and restart the drone. From now on it's like updating from the App, but skipping steps 0 to 8.
 
 ### Flashing without sd-card (FC_UPGRADE partition)
There are some firmware components which can be flashed without the sd-card. When you connect the drone via USB, it mounts a storage named ``FC_UPGRADE``. The flashing method differs from the sd-card method: For example, to flash the FC firmware it needs to be named ``FC.INI`` and you have to use the firmware image downloaded from FIMI directly, not the ``fr_firmware.bin`` file generated by this tool. Then just restart the drone. We can upgrade and downgrade firmware independent of the camera that way, but there are some limitations:

The ``FC_UPGRADE`` partition is only about 3mb in size and the only firmware I tried to flash with it was the FC firmware. I don't know which components can be flashed with it and how they must be named. For sure, you can't flash RC, RC-Relay, FC-Relay and Camera with this method because of their image size. I'm not sure about the the other firmwares.

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
