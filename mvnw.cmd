@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM ----------------------------------------------------------------------------

@echo off
set ERROR_CODE=0

@REM ==== START VALIDATION ====
if NOT "%JAVA_HOME%"=="" goto OkJHome

echo.
echo ERROR: JAVA_HOME not found in your environment.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:OkJHome
if exist "%JAVA_HOME%\bin\java.exe" goto init

echo.
echo ERROR: JAVA_HOME is set to an invalid directory.
echo JAVA_HOME = "%JAVA_HOME%"
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

@REM ==== INITIALIZE ====
:init
set MAVEN_CMD_LINE_ARGS=%MAVEN_CONFIG% %*

@REM Find Maven project base directory
for %%i in (%~dp0) do set "BASEDIR=%%~fi"

@REM ==== START MAVEN ====
call "%JAVA_HOME%\bin\java.exe" ^
  -classpath "%BASEDIR%\boot\plexus-classworlds-2.7.0.jar" ^
  "-Dclassworlds.conf=%BASEDIR%\bin\m2.conf" ^
  "-Dmaven.home=%BASEDIR%" ^
  "-Dmaven.multiModuleProjectDirectory=%BASEDIR%" ^
  org.codehaus.plexus.classworlds.launcher.Launcher %MAVEN_CMD_LINE_ARGS%

if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@endlocal & set ERROR_CODE=%ERROR_CODE%

cmd /C exit /B %ERROR_CODE%
