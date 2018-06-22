@echo off
echo.
echo ##################################################
echo ####### CORTANA COMPILE SCRIPT FOR WINDOWS #######
echo ##################################################

set JAVAC=javac
set OPTIONS=-g
set CLASSPATH=.;..\bin;..\libs\*
set COMPILE=%JAVAC% %OPTIONS% -cp %CLASSPATH%

echo Using compile command:
echo %COMPILE%
echo.
echo Start compiling...
echo.

cd ..\src
%COMPILE% -d ..\bin nl\liacs\subdisc\*.java
%COMPILE% -d ..\bin nl\liacs\subdisc\cui\*.java
%COMPILE% -d ..\bin nl\liacs\subdisc\gui\*.java
xcopy /s /-y /y /i config ..\bin\config

REM pause

echo.
echo Compiling done...
echo.

echo checking for external files...
if not exist ..\bin\org\ goto EXTRACT

:EXTRACT
echo external files missing, extracting external jar...
cd ..\libs\
jar xvf jfreechart-1.0.14.jar
jar xvf jcommon-1.0.17.jar
jar xvf commons-math3-3.6.1.jar
jar xvf commons-csv-1.2.jar
jar xvf commons-lang3-3.4.jar
jar xvf datumbox-framework-applications-0.7.0.jar
jar xvf datumbox-framework-common-0.7.0.jar
jar xvf datumbox-framework-core-0.7.0.jar
jar xvf datumbox-framework-lib-0.7.0.jar
jar xvf ini4j-0.5.4.jar
jar xvf Jama-1.0.2.jar
jar xvf jdistlib-0.4.5-bin.jar
jar xvf libsvm-3.21.jar
jar xvf lpsolve-5.5.2.0.jar
jar xvf mapdb-1.0.9.jar
jar xvf slf4j-api-1.7.19.jar
jar xvf REngine.jar
jar xvf RserveEngine.jar
rmdir /s /q META-INF
if exist ..\bin\apache\ rmdir /s /q ..\bin\apache\
if exist ..\bin\com\ rmdir /s /q ..\bin\com\
if exist ..\bin\org\ rmdir /s /q ..\bin\org\
if exist ..\bin\Jama\ rmdir /s /q ..\bin\Jama\
if exist ..\bin\rosuda\ rmdir /s /q ..\bin\rosuda\
move apache ..\bin\
move org ..\bin\
move com ..\bin\
move Jama ..\bin\
move rosuda ..\bin\
echo extracting external jars done...
:NO_EXTRACT

REM pause

del ..\cortana.jar
echo creating cortana.jar
cd ..\bin\
jar.exe cvfm cortana.jar cortana.mf nl org com Jama config autorun.dtd cortana.jpg icon.jpg

echo creating jar done... Stored in Cortana folder (../)

move cortana.jar ..\
cd ..\batch
echo finished!
start "" "javaw" -jar ..\cortana.jar