csv_file_path = file.choose()
timestamp = format(Sys.time(), "%m%d-%H%M")

options(width=120)
options(repos = "https://cran.rstudio.com")
if(!require(methods)){install.packages("methods")}
if(!require(dplyr)){install.packages("dplyr")}
if(!require(FSA)){install.packages("FSA")}
if(!require(lattice)){install.packages("lattice")}
if(!require(DescTools)){install.packages("DescTools")}
if(!require(car)){install.packages("car")}
if(!require(rcompanion)){install.packages("rcompanion")}
if(!require(multcompView)){install.packages("multcompView")}
if(!require(userfriendlyscience)){install.packages("userfriendlyscience")}
if(!require(graphics)){install.packages("graphics")}
if(!require(lsr)){install.packages("lsr")}

csv_dir_path = dirname(csv_file_path)
csv_file_name = sub(pattern = "(.*)\\..*$", replacement = "\\1", basename(csv_file_path))
output_file_path = file.path(csv_dir_path, paste(csv_file_name, '_', timestamp, '_result.txt', sep=""))

Data = read.csv(csv_file_path, header=TRUE, sep=",")

cat("### File\n", csv_file_path, file=output_file_path, sep="\n", append=FALSE)

### Specify the order of factor levels

library(dplyr)   
Data = mutate(Data, group = factor(group, levels=unique(group)))

# Medians and descriptive statistics

library(FSA)

out <- capture.output(Summarize(value ~ group, data = Data))
cat("\n### Descriptive statistics\n", out, file=output_file_path, sep="\n", append=TRUE)

### Graphing the results ###
  
# Stacked histograms of values across groups

library(lattice)

png(file.path(csv_dir_path, paste(csv_file_name, '_', timestamp, '_histogram.png', sep="")))
histogram(~ value | group,  data=Data, layout=c(1,length(levels(Data$group))), 
   panel=function(x, ...) {                             
   panel.histogram(x, ...) 

   panel.mathdensity(dmath = dnorm, col = "black",
                                args = list(mean=mean(x),sd=sd(x)))          
                                        })
dev.off()

# Simple boxplots of values across groups

png(file.path(csv_dir_path, paste(csv_file_name, '_', timestamp, '_boxplot.png', sep="")))
boxplot(value ~ group,
        data = Data,
        ylab="Value",
        xlab="Group")
dev.off()

### Shapiro-Wilk normality test

cat("\n### Normality test\n", file=output_file_path, sep="\n", append=TRUE)

Data.levels = split(Data, Data$group)
for(i in seq(length(Data.levels))) {
    group.n = length(Data.levels[[i]]$group)
    group.name = Data.levels[[i]]$group[1]
    cat(paste("Group: ", group.name, sep=''), file=output_file_path, sep="", append=TRUE)
    if (group.n < 50) {
        shapiro.result = shapiro.test(Data.levels[[i]]$value)
        cat(", Shapiro-Wilk normality test W = ", shapiro.result$statistic, " p-value = ", shapiro.result$p.value, "\n" , file=output_file_path, sep="", append=TRUE)
    } else {
        ks.result = ks.test(Data.levels[[i]]$value, pnorm, mean(Data.levels[[i]]$value), sd(Data.levels[[i]]$value))
        cat(", Kolmogorov-Smirnov normality test D = ", ks.result$statistic, " p-value = ", ks.result$p.value, "\n" , file=output_file_path, sep="", append=TRUE)
    }
}

### Heteroscedasticity test

library(car)
leveneTest.result = leveneTest(value~group, Data, center= mean)
out <- capture.output(leveneTest.result)
cat("\n### Test for Homogeneity of Variance\n", out, file=output_file_path, sep="\n", append=TRUE)

is.heteroscedastic = (leveneTest.result$`Pr(>F)` <= 0.05)

if (isTRUE(is.heteroscedastic[1])) {
    cat("\nData are heteroscedastic. Excute Welch's anova.", file=output_file_path, sep="\n", append=TRUE)
} else {
    cat("\nData are homoscedastic. Excute ANOVA.", file=output_file_path, sep="\n", append=TRUE)
}

# ----------------

if (isTRUE(is.heteroscedastic[1])) {

    ### Welch's anova for unequal variances

    out <- capture.output(oneway.test(value ~ group, data=Data, var.equal=FALSE))
    cat('\n### Welch\'s anova for unequal variances', out, file=output_file_path, sep="\n", append=TRUE)

    ### Performing the Games-Howell Test

    library(userfriendlyscience)
    out <- capture.output(oneway(Data$group, y = Data$value, posthoc = 'games-howell'))
    cat("### Games-Howell Post-Hoc Test\n", out, file=output_file_path, sep="\n", append=TRUE)

} else {

    ### ANOVA

    library(graphics)

    # fit a linear model to the data
    model <- lm(value ~ group, data = Data)

    #run the ANOVA on the model
    anova.result = anova(model)

    out <- capture.output(anova.result)
    cat("\n### ANOVA for equal variances\n", out, file=output_file_path, sep="\n", append=TRUE)

    #cat(paste("Eta squared: ", kruskal.result$statistic / (length(Data$group) - 1), "\n", sep=''), file=output_file_path, sep="\n", append=TRUE)

    aov.result <- aov(value ~ group, data = Data)

    library(lsr)
    eta.squared.result = etaSquared( aov.result )
    cat("\nEta squared: ", eta.squared.result[[1]], '\n', file=output_file_path, sep="", append=TRUE)

    cat("\n### Post Hoc Tests", file=output_file_path, sep="\n", append=TRUE)

    #lsd.result <- capture.output(PostHocTest(aov.result, method="lsd"))
    #hsd.result <- capture.output(PostHocTest(aov.result, method="hsd"))
    scheffe.result <- capture.output(PostHocTest(aov.result, method="scheffe"))

    #cat(lsd.result, hsd.result, scheffe.result, file=output_file_path, sep="\n", append=TRUE)
    cat(scheffe.result, file=output_file_path, sep="\n", append=TRUE)

}

message('Finish')