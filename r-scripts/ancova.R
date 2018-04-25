input <- mtcars[,c("am","mpg","hp")];
input[,"am"] <- ifelse(input[,"am"]==1, "A", "B")

input[,"am"] <- ifelse(input[,"am"]="A", 0, 1)




result2 <- aov(mpg~as.factor(hp)+am,data=mtcars);

result1 <- aov(mpg~hp*am,data=mtcars);
reg_interaction <- summary(result1)[[1]][["Pr(>F)"]][3];

if (reg_interaction > 0.05) {
    result2 <- aov(mpg~hp+am,data=mtcars);
    p_value <- summary(result2)[[1]][["Pr(>F)"]][2];
} else {
    input[,"group"] <- ifelse(input[,"hp"]>mean(input[,"hp"]),1,2);
    result3 <- aov(mpg ~ am + group + am:group, data = input);
    main_factor <- summary(result3)[[1]][["Pr(>F)"]][1];
    inter_factor <- summary(result3)[[1]][["Pr(>F)"]][3];
    p_value <- min(c(main_factor,inter_factor));
};
(1-p_value)

LSD.test(mpg, hp, DFerror, MSerror)

TukeyHSD(result2)

input <- mtcars[,c("am","mpg","hp")];
result2 <- aov(mpg~hp+am,data=mtcars);
summary(result2)


input[,"am"] <- as.factor(input[,"am"])
viagraModel<-aov(mpg ~ hp+am, data = input) 
summary(viagraModel)
p_value <- summary(viagraModel)[[1]][["Pr(>F)"]][2];
glht(viagraModel, linfct = mcp(am = "Tukey"))

TukeyHSD(aov(mpg ~ hp+am, data = input))

pairwise.t.test(y,x,p.adj="none")

(LSD.test(viagraModel,"am", alpha = 0.05,group=TRUE,p.adj="non"))
summary(out)

input[,"am"] <- as.factor(input[,"am"])
viagraModel<-aov(mpg ~ am+hp, data = input) 
PostHocTest(viagraModel, method = "lsd")

library(lsmeans)
library(multcompView)


# --------------

input <- mtcars[,c("am","mpg","hp")];
input[,"am"] <- as.factor(input[,"am"])
ancova.model<-aov(mpg ~ am+hp, data = input) 
xyplot(mpg ~ hp, data=input, groups=am, aspect="iso", type=c("p","r"),auto.key=list(space="right", lines=TRUE, points=FALSE))

ancova.result<-lsmeans(ancova.model,pairwise ~ am, adjust = "lsd")
pval <- summary(ancova.result$contrasts)$p.value


# -----------
library(sm)
input <- mtcars[,c("am","mpg","hp")];
x <- input[,"hp"]
y <- input[,"mpg"]
g <- input[,"am"]
sm.ancova(x, y, g, h = 0.15, model = "equal")

# ----------------
data <- read.table("D:/Desktop/20180413 測試cortana的貝氏網路/共變數分析/英文學習成就測眼成績：控制組與實驗組前後測 - csv.csv", header = TRUE, sep = ",")

data <- read.table("D:/Desktop/20180413 測試cortana的貝氏網路/共變數分析/20180423 ANCOVA - data.csv", header = TRUE, sep = ",")
library(lattice)
xyplot(dv ~ cov, data=data, groups=iv, aspect="iso", type=c("p","r"),auto.key=list(space="right", lines=TRUE, points=FALSE))

loess.result <- loess.ancova(data[,"cov"], data[,"dv"], data[,"iv"], plot=TRUE, data.points=TRUE)

sm.model <- sm.ancova(data[,"cov"], data[,"dv"], data[,"iv"], model = "equal")
sm.aov.data <- rbind(sm.model$data$group, sm.model$eval.points)
ancova.result<-lsmeans(sm.model,pairwise ~ iv, adjust = "lsd")
summary(ancova.model<-aov(dv ~ iv+cov, data = data))

sm.model <- sm.ancova(data[,"cov"], data[,"dv"], data[,"iv"], model = "none", h = 1)
h.select(data[,"cov"], data[,"dv"], data[,"iv"], xlab=True, ylab=True)

sm.ancova(data[,"cov"], data[,"dv"], data[,"iv"], model = "equal", xlab=True, ylab=True)
sm.regression(data[,"cov"], data[,"dv"], model = "equal", group = data[,"iv"])


sig.trace(sm.ancova(data[,"cov"], data[,"dv"], data[,"iv"], model = "equal"),
hvec = seq(0.05, 0.3, length = 10))

sig.trace(sm.ancova(data[,"cov"], data[,"dv"], data[,"iv"], model = "equal"))

sig.trace(sm.ancova(onions[,"dens"], log(onions[,"yield"]), onions[,"location"], model="parallel"), hvec=seq(5,30,length=12))

 t1 <- T.aov(data[,"cov"], data[,"dv"], data[,"iv"])

 lsmeans(t1,pairwise ~ am, adjust = "lsd")
 t1
 plot(t1)

loess.result <- loess.ancova(data[,"cov"], data[,"dv"], data[,"iv"], plot=TRUE, data.points=TRUE)
aov(loess.result$smooth.fit$fitted ~ data[,"iv"]);

loess.result$smooth.fit$fitted ~ data[,"iv"]


mydata[,"iv"] <- as.factor(mydata[,"iv"])
input[,"am"] <- as.factor(input[,"am"])
taov <- aov(dv ~ iv, data=mydata)

pairwise.t.test(mydata$iv,mydata$dv,p.adjust.method = "none")


lsmeans(taov,pairwise ~ iv, adjust = "lsd")
Taov.result <- T.aov(data[,"cov"], data[,"dv"], data[,"iv"], plot=TRUE, data.points=TRUE)


data <- read.table("D:/Desktop/20180413 測試cortana的貝氏網路/共變數分析/20180423 ANCOVA - data - Copy.csv", header = TRUE, sep = ",")
Taov.result <- T.aov(data[,"cov"], data[,"dv"], data[,"iv"], plot=TRUE, data.points=TRUE)
Taov.result$p.value
Taov.result$fit

// --------------------------
#input <- read.table("D:/Desktop/20180413 測試cortana的貝氏網路/共變數分析/violate_reg.csv", header = TRUE, sep = ",");
input <- read.table("D:/Desktop/20180413 測試cortana的貝氏網路/共變數分析/homogeneity_reg.csv", header = TRUE, sep = ",");

reg.model <- aov(dv~iv*cov,data=input);
reg.interaction <- summary(reg.model)[[1]][["Pr(>F)"]][3];

if (reg.interaction > 0.05) {
    ancova.model <- aov(dv~as.factor(iv)+cov,data=input);
    p.value <- summary(ancova.model)[[1]][["Pr(>F)"]][2];

    library(lsmeans)
    
    ancova.compare.summary <- summary((lsmeans(ancova.model, pairwise ~ iv, adjust = "lsd"))$contrasts);
    ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$estimate > 0, gsub(" - ", " > ", ancova.compare.summary$contrast), ifelse(ancova.compare.summary$estimate < 0, gsub(" - ", " < ", ancova.compare.summary$contrast), gsub(" - ", " = ", ancova.compare.summary$contrast)));
    ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$p.value < 0.05, paste0(ancova.compare.summary$contrast, "*"));
    pairwise.result <- paste(ancova.compare.summary$contrast, collapse = "\n");
    
} else {

};