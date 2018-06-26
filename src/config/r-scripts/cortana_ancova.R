if(!require(car)){install.packages("car")};
if(!require(emmeans)){install.packages("emmeans")};
if(!require(gtools)){install.packages("gtools")};
if(!require(plyr)){install.packages("plyr")};
if(!require(fANCOVA)){install.packages("fANCOVA")};

cortana_ancova <- function (input) {

    p.value <- 1;
    pairwise.result <- c('null');
    is.parametric.ancova <- TRUE;
    ancova.model <- aov(dv~as.factor(iv)+cov,data=input);
    ancova.model.type3 <- Anova(ancova.model, type=3);
    p.value <- ancova.model.type3["Pr(>F)"][[1]][2];
    ancova.compare.summary <- summary((emmeans(ancova.model, pairwise ~ iv, adjust = "none"))$contrasts);
    compare.list <- strsplit(levels(ancova.compare.summary$contrast), " - ");
    compare.list <- data.frame(compare.list, row.names = c("iv1", "iv2"));
    ancova.compare.summary$iv1 <- unlist(compare.list[1,]);
    ancova.compare.summary$iv2 <- unlist(compare.list[2,]);
    ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$estimate > 0, paste(ancova.compare.summary$iv1, ">", ancova.compare.summary$iv2), ifelse(ancova.compare.summary$estimate < 0, paste(ancova.compare.summary$iv2, ">", ancova.compare.summary$iv1), paste(ancova.compare.summary$iv1, "=", ancova.compare.summary$iv2)));
    ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$p.value < 0.05, paste0(ancova.compare.summary$contrast, "*"), ancova.compare.summary$contrast);
    pairwise.result <- ancova.compare.summary$contrast;

    paste(is.parametric.ancova, sprintf("%.5f", p.value), paste(pairwise.result, collapse=";"), sep=",");
};
print("script|data");
cortana_ancova(data.frame(iv = c('控制組','控制組','控制組','控制組','控制組','控制組','控制組','控制組','控制組','控制組','實驗組','實驗組','實驗組','實驗組','實驗組','實驗組','實驗組','實驗組','實驗組','實驗組'),cov = c(11.0,12.0,19.0,13.0,17.0,15.0,17.0,14.0,13.0,16.0,11.0,14.0,10.0,12.0,12.0,13.0,10.0,15.0,14.0,11.0),dv = c(21.0,23.0,25.0,23.0,23.0,24.0,24.0,20.0,22.0,24.0,21.0,24.0,21.0,20.0,23.0,24.0,23.0,21.0,25.0,24.0)));