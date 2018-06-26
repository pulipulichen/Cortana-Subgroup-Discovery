if(!require(lme4)){install.packages("lme4")}
if(!require(lmerTest)){install.packages("lmerTest")}
if(!require(lsmeans)){install.packages("lsmeans")}
if(!require(multcompView)){install.packages("multcompView")}
if(!require(car)){install.packages("car")}
if(!require(pracma)){install.packages("pracma")}

Data <- read.table("D:/xampp/htdocs/[Java-Projects]/Cortana-Subgroup-Discovery/cortana論文_20180623_pair.csv", header = TRUE, sep = ",")

Data$diff <- Data$e_dv - Data$c_dv
m <- lm(Data$diff~Data$cov)  
#ancova.model <- aov(diff~as.factor(iv) + cov, data=Data) 

mydata <- read.table("D:/xampp/htdocs/[Java-Projects]/Cortana-Subgroup-Discovery/[document]/20180624/rm_anova.csv", header = TRUE, sep = ",")
am3 <- aov(dv ~ iv + Error(id/iv) + cov, data=mydata)
am3 <- Anova(am3, type=3);
summary(am3)


ancova.model.type3 <- Anova(m, type=3);

str(Data)

##############

library(lme4)

library(lmerTest)

model = lmer(dv ~ iv + cov  + (1|id),
            data=Data,
            REML=TRUE)

anova(model)

rand(model)

###############

library(car)

scatterplot(dv ~ cov | iv, data = Data, smooth=F, reg.line=F)

###############

library(multcompView)

library(lsmeans)

leastsquare = lsmeans(model,
                      pairwise ~ iv,
                      adjust="tukey")

CLD = cld(leastsquare,
    alpha=0.05,
    Letters=letters,      ### Use lower-case letters for .group
    adjust="tukey")       ### Tukey-adjusted comparisons

CLD
