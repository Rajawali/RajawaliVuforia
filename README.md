## Rajawali + Vuforia Integration

## ATTENTION
As of 03/17/2016, this module/example is fully integrated to the examples application which has been moved to the main Rajawali repository: https://github.com/Rajawali/Rajawali. This repository will remain for historical reference but **it is effectively abandoned and no responses from the maintainers should be expected.**

[Vuforia SDK](https://www.vuforia.com/) is a product of Qualcomm Austria Research Center GmbH.
is a product of Qualcomm Austria Research Center GmbH.

This project integrates the (Rajawali 3D framework)[https://github.com/MasDennis/Rajawali] and the Vuforia Augmented Reality SDK.
It is currently using Vuforia 3.0.9.

Features:
* Frame Markers
* Image Targets
* Cloud Recognition Targets
* Cylinder Targets
* Multi-Targets

[http://www.youtube.com/watch?v=rjLa4K9Ffuo](http://www.youtube.com/watch?v=rjLa4K9Ffuo)

#How To Run The Example

You need a local build of the latest RajawaliVuforia Framework. Currently RajawaliVuforia is not available on maven so a local build must be created. To build a local release of RajawaliVuforia simply perform a checkout of the library then run the gradle tasks ```clean assembleRelease uploadArchives```.

## Linux
```
git clone https://github.com/Rajawali/RajawaliVuforia.git
./RajawaliVuforia/RajawaliVuforia/gradlew clean assembleRelease uploadArchives
```

## Windows
```
git clone https://github.com/Rajawali/RajawaliVuforia.git
./RajawaliVuforia/RajawaliVuforia/gradlew.bat clean assembleRelease uploadArchives
```

Make sure to copy the library (.so) files to app/src/main/jniLibs.

![Rajawali + Vuforia](http://www.rozengain.com/files/rajawali/rajawali-vuforia-001.jpg)
![Rajawali + Vuforia](http://www.rozengain.com/files/rajawali/rajawali-vuforia-002.jpg)
![Rajawali + Vuforia](http://www.rozengain.com/files/rajawali/rajawali-vuforia-003.jpg)
![Rajawali + Vuforia](http://www.rozengain.com/files/rajawali/rajawali-vuforia-004.jpg)
![Rajawali + Vuforia](http://www.rozengain.com/files/rajawali/rajawali-vuforia-005.jpg)
![Rajawali + Vuforia](http://www.rozengain.com/files/rajawali/rajawali-vuforia-006.jpg)
![Rajawali + Vuforia](http://www.rozengain.com/files/rajawali/rajawali-vuforia-007.jpg)
![Rajawali + Vuforia, Cylinder Target](http://www.rozengain.com/files/RajawaliWiki/rajawali-cylinder-target.png)
![Rajawali + Vuforia, Multi Target](http://www.rozengain.com/files/RajawaliWiki/rajawali-vuforia-multi-targets.png)

