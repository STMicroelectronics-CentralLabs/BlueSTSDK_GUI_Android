# BlueSTSDK Gui

This library is built on top of the [BlueSTSDK](https://github.com/STMicroelectronicswq/BlueSTSDK_Android) library.

It extends the BlueSTSDK providing firmware update over bluetooth and a set of helper classes to build a consistent user interface among apps based on the BlueSTSDK library.

## How to include it

This library has some part in Kotlin, so add the Kotlin support to your project:
in the project build.gradle add:
```groovy
buildscript {
    
    ext.kotlin_version = '1.3.41' 

    dependencies {
        ...
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
}
```

To avoid version conflict with the dependency used by the application or other libraries, app all the dependency version must be defined into the project build.gradle.

To build the BlueSTSDK_GUI library you need to add:
```groovy
ext {
    // Sdk and tools
    targetSdkVersion = 28
    compileSdkVersion = 29
    buildToolsVersion = '29.0.1'

    // App dependencies
    androidx_appCompatVersion='1.0.2'
    androidx_materialVersion='1.0.0'
    androidx_constraintLayoutVersion = '1.1.3'
    androidx_recycleViewVersion = '1.0.0'
    androidx_cardViewVersion = '1.0.0'
    androidx_recycleViewVersion = '1.0.0'
    androidx_lifecycleExtVersion = '2.0.0'

    // Test dependecy
    androidx_runnerVersion = '1.2.0'
    androidx_rulesVersion = '1.2.0'
    androidx_espressoVersion = '3.2.0'

    junitVersion = '4.12'
    mockitoVersion = '1.9.5'
}

```



## License

Copyright (c) 2017  STMicroelectronics – All rights reserved
The STMicroelectronics corporate logo is a trademark of STMicroelectronics

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

- Redistributions of source code must retain the above copyright notice, this list of conditions
and the following disclaimer.

- Redistributions in binary form must reproduce the above copyright notice, this list of
conditions and the following disclaimer in the documentation and/or other materials provided
with the distribution.

- Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
STMicroelectronics company nor the names of its contributors may be used to endorse or
promote products derived from this software without specific prior written permission.

- All of the icons, pictures, logos and other images that are provided with the source code
in a directory whose title begins with st_images may only be used for internal purposes and
shall not be redistributed to any third party or modified in any way.

- Any redistributions in binary form shall not include the capability to display any of the
icons, pictures, logos and other images that are provided with the source code in a directory
whose title begins with st_images.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
OF SUCH DAMAGE.
