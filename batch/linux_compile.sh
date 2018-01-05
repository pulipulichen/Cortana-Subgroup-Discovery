#!/bin/bash

echo "##################################################"
echo "####### CORTANA COMPILE SCRIPT FOR BASH    #######"
echo "##################################################"

SRC_DIR="../src/"
BIN_DIR="../bin/"
LIB_DIR="../libs/"
JFREECHART="jfreechart-1.0.14.jar"
JCOMMON="jcommon-1.0.17.jar"

VERSION="6"
JAVAC="/usr/lib/jvm/java-$VERSION-openjdk-amd64/bin/javac"
OPTIONS="-Xlint -g"
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

cd ../batch/
echo Finished!

