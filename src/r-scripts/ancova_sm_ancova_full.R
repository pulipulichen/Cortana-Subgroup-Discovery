input <- data.frame(
    iv = c('C','C','E','E','E','E','E'),cov = c(11.0,8.0,5.0,7.0,7.0,7.0,6.0),dv = c(11.0,3.0,5.0,5.0,5.0,4.0,7.0)
);

reg.model <- aov(dv~iv*cov,data=input);
reg.interaction <- summary(reg.model)[[1]][["Pr(>F)"]][3];

p.value <- 1;
pairwise.result <- c();

if (is.null(reg.interaction) || is.na(reg.interaction) ) {
    print('do nothing');
} else if (reg.interaction > 0.05) {
    ancova.model <- aov(dv~as.factor(iv)+cov,data=input);
    library(car);
    ancova.model.type3 <- Anova(ancova.model, type=3);
    p.value <- ancova.model.type3["Pr(>F)"][[1]][2];

    library(emmeans);
    ancova.compare.summary <- summary((emmeans(ancova.model, pairwise ~ iv, adjust = "none"))$contrasts);
    ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$estimate > 0, gsub(" - ", " > ", ancova.compare.summary$contrast), ifelse(ancova.compare.summary$estimate < 0, gsub(" - ", " < ", ancova.compare.summary$contrast), gsub(" - ", " = ", ancova.compare.summary$contrast)));
    ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$p.value < 0.05, paste0(ancova.compare.summary$contrast, "*"), ancova.compare.summary$contrast);
    pairwise.result <- ancova.compare.summary$contrast;
    
} else {
    
    library(gtools);
    library(plyr);
    iv.levels <- levels(factor(input[,"iv"]));
    iv.comb <- combinations(n=as.integer(summary(iv.levels)["Length"]), r=2, v=iv.levels, repeats.allowed=F);
    iv.comb.freq <- sum(count(iv.comb)$freq);
    iv.comb <- cbind(iv.comb, c(rep("=",iv.comb.freq)));
    iv.comb <- cbind(iv.comb, c(rep.int(NA,iv.comb.freq)));
    iv.comb <- cbind(iv.comb, c(rep("",iv.comb.freq)));

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
            iv.comb[i,4] <- ">"
        } else if (fitted.data1.mean < fitted.data2.mean) {
            iv.comb[i,4] <- "<"
        } else {
            iv.comb[i,4] <- "="
        };

        iv.comb[i,5] <- paste(iv.comb[i,1], iv.comb[i,4], iv.comb[i,2]);

        if (iv.comb[i,3] < 0.05) {
            iv.comb[i,5] <- paste0(iv.comb[i,5], "*")
        };
    };

    pairwise.result <- iv.comb[,5];
};
paste(sprintf("%.5f", p.value), paste(pairwise.result, collapse=";"), sep=",");