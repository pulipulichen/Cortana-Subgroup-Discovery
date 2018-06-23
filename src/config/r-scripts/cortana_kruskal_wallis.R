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

cortana_kruskal_wallis <- function (Data) {

Data = mutate(Data, iv = factor(iv, levels=unique(iv)));

leveneTest.result = leveneTest(dv~iv, Data, center= mean);
is.heteroscedastic = (leveneTest.result$`Pr(>F)` <= 0.05);

anova.method <- 'K-W';

pairwise.result <- c();

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
    kruskal.result <- kruskal.test(dv ~ iv, data = Data);

    Data$iv <- factor(Data$iv, levels=levels(Data$iv));
	
	if (length(unique(Data$iv)) > 2) {
		ancova.compare.summary <- dunnTest(dv ~ iv, data=Data, method="bh");
		
		ancova.compare.summary <- ancova.compare.summary$res;
		ancova.compare.summary$contrast <- ancova.compare.summary$Comparison;
		ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$Z > 0, gsub("-", " > ", ancova.compare.summary$contrast), ifelse(ancova.compare.summary$Z < 0, gsub("-", " < ", ancova.compare.summary$contrast), gsub("-", " = ", ancova.compare.summary$contrast)));
		ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$P.adj < 0.05, paste0(ancova.compare.summary$contrast, "*"), ancova.compare.summary$contrast);
		
		pairwise.result <- ancova.compare.summary$contrast;
	}
	else {
		ancova.compare.summary <- NemenyiTest(x = Data$dv, g = Data$iv, dist="tukey"); 
		
		ancova.compare.summary <- ancova.compare.summary[[1]];
		contrast <- row.names(ancova.compare.summary);
		contrast <- ifelse(ancova.compare.summary[1,1] > 0, gsub("-", " > ", contrast), ifelse(ancova.compare.summary[1,1] < 0, gsub("-", " < ", contrast), gsub("-", " = ", contrast)));
		contrast <- ifelse(ancova.compare.summary[1,2] < 0.05, paste0(contrast, "*"), contrast);
		
		pairwise.result <- contrast;
	};
	
	
};

paste(anova.method, sprintf("%.5f", p.val), paste(pairwise.result, collapse=";"), sep=",");

};
print("script|data");
cortana_kruskal_wallis(data.frame(iv = c('E','E','E','E','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C','C'),dv = c(11.0,31.0,9.0,3.0,4.0,10.0,20.0,11.0,7.0,13.0,7.0,10.0,10.0,13.0,17.0,12.0,3.0,6.0,14.0,39.0,21.0)));
