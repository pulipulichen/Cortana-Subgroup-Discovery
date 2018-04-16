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
cd ..\batch

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
jar xvf Jama-1.0.2.jar
rmdir /s /q META-INF
if exist ..\bin\com\ rmdir /s /q ..\bin\com\
if exist ..\bin\org\ rmdir /s /q ..\bin\org\
if exist ..\bin\Jama\ rmdir /s /q ..\bin\Jama\
move org ..\bin\
move com ..\bin\
move Jama ..\bin\
echo extracting external jars done...
:NO_EXTRACT
REM pause

del ..\cortana.jar
echo creating cortana.jar
cd ..\bin\
jar.exe cvfm cortana.jar cortana.mf nl org com Jama autorun.dtd cortana.jpg icon.jpg

echo creating jar done... Stored in Cortana folder (../)

move cortana.jar ..\
cd ..\batch
echo finished!
start "" "javaw" -jar ..\cortana.jar