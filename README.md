# Cortana-Subgroup-Discovery
A clone from Cortana Subgroup Discovery: http://datamining.liacs.nl/cortana.html

https://pulipulichen.github.io/Cortana-Subgroup-Discovery/
https://pulipulichen.github.io/Cortana-Subgroup-Discovery/cortana.jar

https://pulipulichen.github.io/Cortana-Subgroup-Discovery/release/cortana1782.jar
https://pulipulichen.github.io/Cortana-Subgroup-Discovery/release/start_cortana.bat

# How to run Cortana with source code

1. Install Eclipse: https://www.eclipse.org/downloads/
2. Create "New > Project" with the path of project.
3. Revise codes.
4. Run > Run (Ctrl + F11)

# How to package jar

/batch/compile_jar.bat

# Resources
* Smile: https://haifengl.github.io/smile/

# Code snippet
````java
Log.logCommandLine("error: " + e.getMessage());
Log.logCommandLine("FileHandle showFileChooser() error: " + e.getMessage());
````

# TODO
- fastods/README.md at master · jferard/fastods https://github.com/jferard/fastods/blob/master/README.md
- outer 大約 小於
- Parametric and Non-parametric tests for comparing two or more groups | Health Knowledge https://www.healthknowledge.org.uk/public-health-textbook/research-methods/1b-statistical-methods/parametric-nonparametric-tests
- levene.test function | R Documentation https://www.rdocumentation.org/packages/lawstat/versions/3.2/topics/levene.test
- t檢定 - R的世界 - Use R for Statistics https://sites.google.com/site/rlearningsite/inference/ttest
- Unpaired Two-Samples T-test in R - Easy Guides - Wiki - STHDA 
http://www.sthda.com/english/wiki/unpaired-two-samples-t-test-in-r 

# Compute t-test 
res <- t.test(weight ~ group, data = my_data, var.equal = TRUE) 
res
- 兩組異質性時，改用獨立樣本t檢定，多組非常態分佈資料之差異檢定與事後比較：R的Kruskal–Wallis檢定與Welch's anova
http://blog.pulipuli.info/2018/01/rkruskalwalliswelchs-anova-non.html?m=1 

類別+數字

類別數量:
  2
     獨立樣本T檢定
       調整變異數

  3以上
     ANOVA
     調整變異數

還有無母數版本
- jsStatisticsKit/statisticsTools.js at master · mtwarog/jsStatisticsKit https://github.com/mtwarog/jsStatisticsKit/blob/master/statisticsTools.js