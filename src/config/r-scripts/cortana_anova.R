options(repos = "https://cran.rstudio.com");
if(!require(emmeans)){install.packages("emmeans")};
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

Data = mutate(Data, iv = factor(iv, levels=unique(iv)));

Data.levels = split(Data, Data$iv);
for(i in seq(length(Data.levels))) {
    iv.n = length(Data.levels[[i]]$iv);
    iv.name = Data.levels[[i]]$iv[1];
    if (iv.n < 50) {
        shapiro.result = shapiro.test(Data.levels[[i]]$dv);
    } else {
        ks.result = ks.test(Data.levels[[i]]$dv, pnorm, mean(Data.levels[[i]]$dv), sd(Data.levels[[i]]$dv));
    };
};

leveneTest.result = leveneTest(dv~iv, Data, center= mean);
is.heteroscedastic = (leveneTest.result$`Pr(>F)` <= 0.05);

anova.method <- 'ANOVA';
pairwise.result <- c('null');
p.val <- 1;

if (isTRUE(is.heteroscedastic[1])) {
	anova.method <- 'Welch';
	welch.result <- oneway.test(dv ~ iv, data=Data, var.equal=FALSE);
	p.val <- welch.result$p.value;
	oneway.posthoc <- oneway(Data$iv, y = Data$dv, posthoc = 'games-howell');
	ancova.compare.summary <- oneway.posthoc$intermediate$posthocTGH$output$games.howell;
	
	ancova.compare.summary$contrast <- row.names(ancova.compare.summary);
	
	
	ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$diff > 0, gsub("-", " > ", ancova.compare.summary$contrast), ifelse(ancova.compare.summary$diff < 0, gsub("-", " < ", ancova.compare.summary$contrast), gsub("-", " = ", ancova.compare.summary$contrast)));
	ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$p < 0.05, paste0(ancova.compare.summary$contrast, "*"), ancova.compare.summary$contrast);
	
	pairwise.result <- ancova.compare.summary$contrast;

} else {
    model <- lm(dv ~ iv, data = Data);
    anova.result = anova(model);

	p.val <- anova.result[["Pr(>F)"]][[1]][1];
    aov.result <- aov(dv ~ iv, data = Data);
    eta.squared.result = etaSquared( aov.result );
    scheffe.result <- capture.output(PostHocTest(aov.result, method="scheffe"));
	
	ancova.compare.summary <- summary((emmeans(aov.result, pairwise ~ iv, adjust = "none"))$contrasts);
	ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$estimate > 0, gsub(" - ", " > ", ancova.compare.summary$contrast), ifelse(ancova.compare.summary$estimate < 0, gsub(" - ", " < ", ancova.compare.summary$contrast), gsub(" - ", " = ", ancova.compare.summary$contrast)));
	ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$p.value < 0.05, paste0(ancova.compare.summary$contrast, "*"), ancova.compare.summary$contrast);
	pairwise.result <- ancova.compare.summary$contrast;
};

paste(anova.method, sprintf("%.5f", p.val), paste(pairwise.result, collapse=";"), sep=",");

};
print("script|data");
cortana_ancova(data.frame(iv = c('E','E','E','E','E','E','E','E','E','E','E','E','C','C','C','C','C','C','C','C','C','C'),cov = c(1.0,1.0,1.0,1.0,2.0,2.0,2.0,2.0,2.0,2.0,2.0,2.0,2.0,2.0,2.0,2.0,2.0,1.0,1.0,1.0,1.0,1.0),dv = c(19.0,39.0,36.0,23.0,5.0,11.0,6.0,5.0,3.0,5.0,10.0,44.0,22.0,4.0,20.0,11.0,7.0,10.0,17.0,6.0,5.0,14.0)));