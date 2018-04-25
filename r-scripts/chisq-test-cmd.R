data <- c(3, 1, 1, 3);

tbl <-matrix(data, nrow = 2);
expDat <- tbl;
for (i in 1:2) {
  expDat[i,1] <- (sum(tbl[i,]) * sum(tbl[,1])) / sum(tbl);
  expDat[i,2] <- (sum(tbl[i,]) * sum(tbl[,2])) / sum(tbl);
};

chisq.result <- chisq.test(tbl);
#adjusted standardized residual 
adj.stdres <- chisq.result$stdres[1,1];

if (sum(data) <= 20 && length(which(expDat <= 5))>0) {
    result <- fisher.test(tbl);
} else {
    result 
};
qualityMeasure <- (1-result$p.value);


