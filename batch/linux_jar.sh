#!/bin/bash

echo "##################################################"
echo "####### CORTANA COMPILE SCRIPT FOR BASH    #######"
echo "##################################################"

SRC_DIR="../src/"
BIN_DIR="../bin/"
LIB_DIR="../libs/"
JFREECHART="jfreechart-1.0.14.jar"
JCOMMON="jcommon-1.0.17.jar"
JAMA="Jama-1.0.2.jar"

VERSION="6"
#use a specific path if your java installation is in an unusual place
#JAVAC="/usr/lib/jvm/java-$VERSION-openjdk-amd64/bin/javac"
JAVAC=javac

OPTIONS="-g"
CLASSPATH=$CLASSPATH:$BIN_DIR:$LIB_DIR*
COMPILE="$JAVAC $OPTIONS -cp $CLASSPATH -d $BIN_DIR"

echo Start compiling using compile command:
echo $COMPILE
echo

cd $SRC_DIR
$COMPILE nl/liacs/subdisc/*.java
$COMPILE nl/liacs/subdisc/cui/*.java
$COMPILE nl/liacs/subdisc/gui/*.java

echo
echo Compiling done...

# extraction of libs only needs to be done once/ for new external jars
echo extracting external jars...
cd $LIB_DIR
jar xvf $JFREECHART
jar xvf $JCOMMON
rm -rf META-INF
mv org/ com/ $BIN_DIR
cd $SRC_DIR
echo extracting external jars done...

echo Creating cortana.jar...
echo
cd $BIN_DIR
jar cvfm cortana.jar cortana.mf com org jama nl autorun.dtd cortana.jpg icon.jpg
mv cortana.jar ../
echo
echo Creating cortana.jar done...

cd ../batch/
echo Finished!

