if(!require(car)){install.packages("car")};
if(!require(emmeans)){install.packages("emmeans")};
if(!require(gtools)){install.packages("gtools")};
if(!require(plyr)){install.packages("plyr")};
if(!require(fANCOVA)){install.packages("fANCOVA")};

cortana_ancova <- function (input) {
    reg.model <- aov(dv~iv*cov,data=input);
    reg.interaction <- summary(reg.model)[[1]][["Pr(>F)"]][3];

    p.value <- 1;
    pairwise.result <- c('null');
    is.parametric.ancova <- TRUE;

    if (is.null(reg.interaction) || is.na(reg.interaction) ) {
        print('do nothing');
    } else if (reg.interaction > 0.05) {
        ancova.model <- aov(dv~as.factor(iv)+cov,data=input);
        ancova.model.type3 <- Anova(ancova.model, type=3);
        p.value <- ancova.model.type3["Pr(>F)"][[1]][2];
        ancova.compare.summary <- summary((emmeans(ancova.model, pairwise ~ iv, adjust = "none"))$contrasts);
        ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$estimate > 0, gsub(" - ", " > ", ancova.compare.summary$contrast), ifelse(ancova.compare.summary$estimate < 0, gsub(" - ", " < ", ancova.compare.summary$contrast), gsub(" - ", " = ", ancova.compare.summary$contrast)));
        ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$p.value < 0.05, paste0(ancova.compare.summary$contrast, "*"), ancova.compare.summary$contrast);
        pairwise.result <- ancova.compare.summary$contrast;
    } else {
        is.parametric.ancova <- FALSE;
        iv.levels <- levels(factor(input[,"iv"]));
        iv.comb <- combinations(n=as.integer(summary(iv.levels)["Length"]), r=2, v=iv.levels, repeats.allowed=F);
        iv.comb.freq <- sum(count(iv.comb)$freq);
        iv.comb <- cbind(iv.comb, c(rep("=",iv.comb.freq)));
        iv.comb <- cbind(iv.comb, c(rep.int(NA,iv.comb.freq)));
        iv.comb <- cbind(iv.comb, c(rep("",iv.comb.freq)));

        p.value <- 1;
        for (i in 1:iv.comb.freq) {

            comb <- iv.comb[i,];
            input.comb <- input[input$iv %in% comb, ];
            input.comb <- input.comb[with(input.comb, order(iv)), ];

            tryCatch({
                Taov.result <- T.aov(input.comb[,"cov"], input.comb[,"dv"], input.comb[,"iv"], plot=FALSE, data.points=TRUE);

                loess.result <- loess.ancova(input.comb[,"cov"], input.comb[,"dv"], input.comb[,"iv"], plot=FALSE, data.points=TRUE);
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
            }, 
            warning = function(w) {}, 
            error=function(e){});
        };

        if (iv.comb[,5] != "") {
            pairwise.result <- iv.comb[,5];
        };
    };
    paste(is.parametric.ancova, sprintf("%.5f", p.value), paste(pairwise.result, collapse=";"), sep=",");
};
print("data|script");
cortana_ancova(data.frame(iv = c('C','C','C','C','C','C','C','C','C','C','E','E','E','E','E','E','E','E','E','E'),cov = c(21.0,23.0,25.0,23.0,23.0,24.0,24.0,20.0,22.0,24.0,21.0,24.0,21.0,20.0,23.0,24.0,23.0,21.0,25.0,24.0),dv = c(21.0,23.0,25.0,23.0,23.0,24.0,24.0,20.0,22.0,24.0,21.0,24.0,21.0,20.0,23.0,24.0,23.0,21.0,25.0,24.0)));
