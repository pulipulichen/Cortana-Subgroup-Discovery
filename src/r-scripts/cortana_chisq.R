cortana_chisq <- function (data, row_num) {
    col_num <- length(data) / row_num;
    tbl <-matrix(data, nrow = row_num);
    print(tbl);
    chisq.result <- chisq.test(tbl);
    adj.stdres <- chisq.result$stdres[1,1];
    print(chisq.result);

    expDat <- tbl;
    for (i in 1:row_num) {
        for (j in 1:col_num) {
            expDat[i,j] <- (sum(tbl[i,]) * sum(tbl[,j])) / sum(tbl);
        };
    };

    if (sum(data) < 20 && length(which(expDat <= 5))>0) {
        chisq.result <- fisher.test(tbl);
        print("fisher");
    };
    qualityMeasure <- chisq.result$p.value;
    paste(sprintf("%.5f", qualityMeasure), sprintf("%.5f", adj.stdres), sep=",");
};
print("data|script");
cortana_chisq(c(3, 1, 1, 3, 6, 6), 2);
# 3 1 6
# 1 3 6