#options(width=120);
options(repos = "https://cran.rstudio.com");
if(!require(methods)){install.packages("methods")};
if(!require(dplyr)){install.packages("dplyr")};
if(!require(FSA)){install.packages("FSA")};
if(!require(lattice)){install.packages("lattice")};
if(!require(DescTools)){install.packages("DescTools")};
if(!require(car)){install.packages("car")};
if(!require(rcompanion)){install.packages("rcompanion")};
if(!require(multcompView)){install.packages("multcompView")};
if(!require(userfriendlyscience)){install.packages("userfriendlyscience")};
if(!require(graphics)){install.packages("graphics")};
if(!require(lsr)){install.packages("lsr")};

cortana_anova <- function (Data) {

#Data <- data.frame(iv = c('C','C','C','C','C','C','C','C','F','F','E','E','E','E','E','E','E','E','F','F'),dv = c(21.0,23.0,25.0,23.0,23.0,24.0,24.0,20.0,22.0,24.0,21.0,24.0,21.0,20.0,23.0,24.0,23.0,21.0,25.0,24.0));

### Specify the order of factor levels

#library(dplyr)   
Data = mutate(Data, iv = factor(iv, levels=unique(iv)));

# Medians and descriptive statistics

#library(FSA)

#out <- capture.output(Summarize(dv ~ iv, data = Data))
#cat("\n### Descriptive statistics\n", out, file=output_file_path, sep="\n", append=TRUE)

### Graphing the results ###
  
# Stacked histograms of dvs across ivs

#library(lattice)

### Shapiro-Wilk normality test

#cat("\n### Normality test\n", file=output_file_path, sep="\n", append=TRUE)

Data.levels = split(Data, Data$iv);
for(i in seq(length(Data.levels))) {
    iv.n = length(Data.levels[[i]]$iv);
    iv.name = Data.levels[[i]]$iv[1];
    #cat(paste("Group: ", iv.name, sep=''), file=output_file_path, sep="", append=TRUE)
    if (iv.n < 50) {
        shapiro.result = shapiro.test(Data.levels[[i]]$dv);
        #cat(", Shapiro-Wilk normality test W = ", shapiro.result$statistic, " p-value = ", shapiro.result$p.dv, "\n" , file=output_file_path, sep="", append=TRUE)
    } else {
        ks.result = ks.test(Data.levels[[i]]$dv, pnorm, mean(Data.levels[[i]]$dv), sd(Data.levels[[i]]$dv));
        #cat(", Kolmogorov-Smirnov normality test D = ", ks.result$statistic, " p-value = ", ks.result$p.dv, "\n" , file=output_file_path, sep="", append=TRUE)
    };
};

### Heteroscedasticity test

#library(car)
leveneTest.result = leveneTest(dv~iv, Data, center= mean);
#out <- capture.output(leveneTest.result)
#cat("\n### Test for Homogeneity of Variance\n", out, file=output_file_path, sep="\n", append=TRUE)

is.heteroscedastic = (leveneTest.result$`Pr(>F)` <= 0.05);

if (isTRUE(is.heteroscedastic[1])) {
    #cat("\nData are heteroscedastic. Excute Welch's anova.", file=output_file_path, sep="\n", append=TRUE)
} else {
    #cat("\nData are homoscedastic. Excute ANOVA.", file=output_file_path, sep="\n", append=TRUE)
}

# ----------------

anova.method <- 'ANOVA';

if (isTRUE(is.heteroscedastic[1])) {
#if (TRUE) {

    ### Welch's anova for unequal variances
	anova.method <- 'Welch';

	welch.result <- oneway.test(dv ~ iv, data=Data, var.equal=FALSE);
	
	p.val <- welch.result$p.value;
    #out <- capture.output()
    #cat('\n### Welch\'s anova for unequal variances', out, file=output_file_path, sep="\n", append=TRUE)

    ### Performing the Games-Howell Test

    #library(userfriendlyscience)
    #out <- capture.output(oneway(Data$iv, y = Data$dv, posthoc = 'games-howell'))
    #cat("### Games-Howell Post-Hoc Test\n", out, file=output_file_path, sep="\n", append=TRUE)
	oneway.posthoc <- oneway(Data$iv, y = Data$dv, posthoc = 'games-howell');
	ancova.compare.summary <- oneway.posthoc$intermediate$posthocTGH$output$games.howell;
	
	ancova.compare.summary$contrast <- row.names(ancova.compare.summary);
	
	
	ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$diff > 0, gsub("-", " > ", ancova.compare.summary$contrast), ifelse(ancova.compare.summary$diff < 0, gsub("-", " < ", ancova.compare.summary$contrast), gsub("-", " = ", ancova.compare.summary$contrast)));
	ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$p < 0.05, paste0(ancova.compare.summary$contrast, "*"), ancova.compare.summary$contrast);
	
	pairwise.result <- ancova.compare.summary$contrast;

} else {

    ### ANOVA

    #library(graphics)

    # fit a linear model to the data
    model <- lm(dv ~ iv, data = Data);

    #run the ANOVA on the model
    anova.result = anova(model);

	p.val <- anova.result[["Pr(>F)"]][[1]][1];
    
    #cat("\n### ANOVA for equal variances\n", out, file=output_file_path, sep="\n", append=TRUE)

    #cat(paste("Eta squared: ", kruskal.result$statistic / (length(Data$iv) - 1), "\n", sep=''), file=output_file_path, sep="\n", append=TRUE)

    aov.result <- aov(dv ~ iv, data = Data);

    #library(lsr)
    eta.squared.result = etaSquared( aov.result );
    #cat("\nEta squared: ", eta.squared.result[[1]], '\n', file=output_file_path, sep="", append=TRUE)

    #cat("\n### Post Hoc Tests", file=output_file_path, sep="\n", append=TRUE)

    #lsd.result <- capture.output(PostHocTest(aov.result, method="lsd"))
    #hsd.result <- capture.output(PostHocTest(aov.result, method="hsd"))
    scheffe.result <- capture.output(PostHocTest(aov.result, method="scheffe"));
	
	ancova.compare.summary <- summary((emmeans(aov.result, pairwise ~ iv, adjust = "none"))$contrasts);
	ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$estimate > 0, gsub(" - ", " > ", ancova.compare.summary$contrast), ifelse(ancova.compare.summary$estimate < 0, gsub(" - ", " < ", ancova.compare.summary$contrast), gsub(" - ", " = ", ancova.compare.summary$contrast)));
	ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$p.value < 0.05, paste0(ancova.compare.summary$contrast, "*"), ancova.compare.summary$contrast);
	pairwise.result <- ancova.compare.summary$contrast;

    #cat(lsd.result, hsd.result, scheffe.result, file=output_file_path, sep="\n", append=TRUE)
    #cat(scheffe.result, file=output_file_path, sep="\n", append=TRUE)

};

paste(anova.method, sprintf("%.5f", p.val), paste(pairwise.result, collapse=";"), sep=",");

};

cortana_anova(data.frame(iv = c('C','C','C','C','C','C','C','C','C','C','E','E','E','E','E','E','E','E','E','E'),dv = c(21.0,23.0,25.0,23.0,23.0,24.0,24.0,20.0,22.0,24.0,21.0,24.0,21.0,20.0,23.0,24.0,23.0,21.0,25.0,24.0)));