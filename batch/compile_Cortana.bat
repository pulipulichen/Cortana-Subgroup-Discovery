cd ..\src
set CLASSPATH=.;..\bin;..\libs\jfreechart-1.0.14.jar;..\libs\jcommon-1.0.17.jar;..\libs\Jama-1.0.2.jar;..\libs\weka.jar;;..\libs\smile-math-1.5.1.jar

javac -d ..\bin nl\liacs\subdisc\*.java
javac -d ..\bin nl\liacs\subdisc\cui\*.java
javac -d ..\bin nl\liacs\subdisc\gui\*.java

pause
