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
        compare.list <- strsplit(levels(ancova.compare.summary$contrast), " - ");
        compare.list <- data.frame(compare.list, row.names = c("iv1", "iv2"));
        ancova.compare.summary$iv1 <- unlist(compare.list[1,]);
        ancova.compare.summary$iv2 <- unlist(compare.list[2,]);
        ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$estimate > 0, paste(ancova.compare.summary$iv1, ">", ancova.compare.summary$iv2), ifelse(ancova.compare.summary$estimate < 0, paste(ancova.compare.summary$iv2, ">", ancova.compare.summary$iv1), paste(ancova.compare.summary$iv1, "=", ancova.compare.summary$iv2)));
        ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$p.value < 0.05, paste0(ancova.compare.summary$contrast, "*"), ancova.compare.summary$contrast);
        pairwise.result <- ancova.compare.summary$contrast;

    } else {
        is.parametric.ancova <- FALSE;
        iv.levels <- levels(factor(input[,"iv"]));
        iv.comb <- combinations(n=as.integer(summary(iv.levels)["Length"]), r=2, v=iv.levels, repeats.allowed=F);
        iv.comb.freq <- length((as.list(iv.comb)))/2;
        iv.comb <- cbind(iv.comb, c(rep("=",iv.comb.freq)));
        iv.comb <- cbind(iv.comb, c(rep.int(NA,iv.comb.freq)));
        iv.comb <- cbind(iv.comb, c(rep("",iv.comb.freq)));
        p.value <- 1;
        for (i in 1:iv.comb.freq) {

            comb <- iv.comb[i,];
            input.comb <- input[input$iv %in% comb, ];
            input.comb <- input.comb[with(input.comb, order(iv)), ];
            input.comb$iv.number <- unlist(lapply(input.comb$iv, function(ch) grep(ch, as.list(iv.levels))));

            Taov.result <- NULL;
            loess.result <- NULL;
            retry.count <- 0;
            while (is.null(Taov.result) && retry.count < 10) {
                retry.count <- retry.count + 1;
                input.comb$cov.jitter <- jitter(input.comb$cov, factor=0.2);
                input.comb$dv.jitter <- jitter(input.comb$dv, factor=0.2);

                tryCatch({
                        Taov.result <- T.aov(input.comb[,"cov"], input.comb[,"dv"], input.comb[,"iv.number"], plot=FALSE, data.points=TRUE);
                        print("Taov.result ok");
                    },
                    warning = function(w) {}, 
                    error=function(e){
                });

                if (is.null(Taov.result)) {
                    tryCatch({
                            Taov.result <- T.aov(input.comb$cov.jitter, input.comb$dv.jitter, input.comb$iv.number, plot=FALSE, data.points=TRUE);
                            print("Taov.result 2 ok");
                        },
                        warning = function(w2) {}, 
                        error=function(e2){
                    });
                };

                tryCatch({
                        loess.result <- loess.ancova(input.comb$cov, input.comb$dv, input.comb$iv.number, plot=FALSE, data.points=TRUE);
                        print("loess.result ok");
                    }, 
                    warning = function(w) {}, 
                    error=function(e){
                });

                if (is.null(loess.result)) {
                    tryCatch({
                            loess.result <- loess.ancova(input.comb$cov.jitter, input.comb$dv.jitter, input.comb$iv.number, plot=FALSE, data.points=TRUE);
                            print("loess.result 2 ok");
                        },
                        warning = function(w2) {}, 
                        error=function(e2){
                    });
                };
            };
            
            if (is.null(Taov.result) || is.null(loess.result)) {
                next;
            };

            fitted.data <- data.frame(loess.result$smooth.fit$fitted, input.comb$iv);
            colnames(fitted.data)<- c("fitted","iv");

            fitted.data1 <- fitted.data[fitted.data$iv %in% comb[1], ];
            fitted.data1.mean <- mean(fitted.data1[,"fitted"]);

            fitted.data2 <- fitted.data[fitted.data$iv %in% comb[2], ];
            fitted.data2.mean <- mean(fitted.data2[,"fitted"]);

            iv.comb[i,3] <- Taov.result$p.value;
            p.value <- ifelse(Taov.result$p.value < p.value, Taov.result$p.value, p.value);

            if (fitted.data1.mean > fitted.data2.mean) {
                iv.comb[i,5] <- paste(iv.comb[i,1], ">", iv.comb[i,2]);
            } else if (fitted.data1.mean < fitted.data2.mean) {
                iv.comb[i,5] <- paste(iv.comb[i,2], ">", iv.comb[i,1]);
            } else {
                iv.comb[i,5] <- paste(iv.comb[i,1], "=", iv.comb[i,2]);
            };

            if (iv.comb[i,3] < 0.05) {
                iv.comb[i,5] <- paste0(iv.comb[i,5], "*");
            };
        };

        if (iv.comb[,5] != "") {
            pairwise.result <- iv.comb[,5];
        };
    };
    paste(is.parametric.ancova, sprintf("%.5f", p.value), paste(pairwise.result, collapse=";"), sep=",");
};
print("script|data");
cortana_ancova(data.frame(iv = c('控制組','控制組','控制組','控制組','控制組','控制組','控制組','控制組','控制組','控制組','實驗組','實驗組','實驗組','實驗組','實驗組','實驗組','實驗組','實驗組','實驗組','實驗組'),cov = c(11.0,12.0,19.0,13.0,17.0,15.0,17.0,14.0,13.0,16.0,11.0,14.0,10.0,12.0,12.0,13.0,10.0,15.0,14.0,11.0),dv = c(21.0,23.0,25.0,23.0,23.0,24.0,24.0,20.0,22.0,24.0,21.0,24.0,21.0,20.0,23.0,24.0,23.0,21.0,25.0,24.0)));