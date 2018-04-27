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
if(!require(lsmeans)){install.packages("lsmeans")}


#input <- read.table("D:/Desktop/20180413 測試cortana的貝氏網路/共變數分析/violate_reg.csv", header = TRUE, sep = ",");
input <- read.table("D:/Desktop/20180413 測試cortana的貝氏網路/共變數分析/homogeneity_reg.csv", header = TRUE, sep = ",");
input <- read.table("D:/Desktop/20180413 測試cortana的貝氏網路/共變數分析/violate_reg.csv", header = TRUE, sep = ",");
input <- read.table("D:/Desktop/20180413 測試cortana的貝氏網路/共變數分析/violate_reg2.csv", header = TRUE, sep = ",");

reg.model <- aov(dv~iv*cov,data=input);
reg.interaction <- summary(reg.model)[[1]][["Pr(>F)"]][3];
reg.interaction

if (reg.interaction > 0.05) {
    ancova.model <- aov(dv~as.factor(iv)+cov,data=input);
    p.value <- summary(ancova.model)[[1]][["Pr(>F)"]][2];

    library(emmeans);
    ancova.compare.summary <- summary((emmeans(ancova.model, pairwise ~ iv, adjust = "tukey"))$contrasts);
    ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$estimate > 0, gsub(" - ", " > ", ancova.compare.summary$contrast), ifelse(ancova.compare.summary$estimate < 0, gsub(" - ", " < ", ancova.compare.summary$contrast), gsub(" - ", " = ", ancova.compare.summary$contrast)));
    ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$p.value < 0.05, paste0(ancova.compare.summary$contrast, "*"), ancova.compare.summary$contrast);
    #pairwise.result <- paste(ancova.compare.summary$contrast, collapse = "\n");
    pairwise.result <- ancova.compare.summary$contrast;
    
} else {
    print("violate assumption");
    iv.levels <- levels(factor(input[,"iv"]));
    library(gtools);
    iv.comb <- combinations(n=as.integer(summary(iv.levels)["Length"]), r=2, v=iv.levels, repeats.allowed=F);
    iv.comb.freq <- sum(count(iv.comb)$freq);
    iv.comb <- cbind(iv.comb, c(rep("=",iv.comb.freq)));
    iv.comb <- cbind(iv.comb, c(rep.int(NA,iv.comb.freq)));
    iv.comb <- cbind(iv.comb, c(rep("",iv.comb.freq)));

    library(plyr);
    library(fANCOVA);
    p.value <- 1;
    for (i in 1:iv.comb.freq) {
        comb <- iv.comb[i,];
        input.comb <- input[input$iv %in% comb, ];
        input.comb <- input.comb[with(input.comb, order(iv)), ];
        Taov.result <- T.aov(input.comb[,"cov"], input.comb[,"dv"], input.comb[,"iv"], plot=TRUE, data.points=TRUE);

        loess.result <- loess.ancova(input.comb[,"cov"], input.comb[,"dv"], input.comb[,"iv"], plot=TRUE, data.points=TRUE);
        fitted.data <- data.frame(loess.result$smooth.fit$fitted, input.comb[,"iv"]);
        colnames(fitted.data)<- c("fitted","iv");

        fitted.data1 <- fitted.data[fitted.data$iv %in% comb[1], ];
        fitted.data1.mean <- mean(fitted.data1[,"fitted"]);

        fitted.data2 <- fitted.data[fitted.data$iv %in% comb[2], ];
        fitted.data2.mean <- mean(fitted.data2[,"fitted"]);

        iv.comb[i,3] <- Taov.result$p.value;
        p.value <- ifelse(Taov.result$p.value < p.value, Taov.result$p.value, p.value);
        
        if (fitted.data1.mean > fitted.data2.mean) {
            iv.comb[i,4] <- ">";
        } else if (fitted.data1.mean < fitted.data2.mean) {
            iv.comb[i,4] <- "<";
        } else {
            iv.comb[i,4] <- "=";
        }
        iv.comb[i,5] <- paste(iv.comb[i,1], iv.comb[i,4], iv.comb[i,2]);
        if (iv.comb[i,3] < 0.05) {
            iv.comb[i,5] <- paste0(iv.comb[i,5], "*");
        }
    }

    pairwise.result <- iv.comb[,5];
};