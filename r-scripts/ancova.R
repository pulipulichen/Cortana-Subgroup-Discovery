input <- mtcars[,c("am","mpg","hp")]

result1 <- aov(mpg~hp*am,data=mtcars)
reg_interaction <- summary(result1)[[1]][["Pr(>F)"]][3]

if (reg_interaction > 0.05) {
    result2 <- aov(mpg~hp+am,data=mtcars)
    p_value <- summary(result2)[[1]][["Pr(>F)"]][2]
} else {
    input[,"group"] <- ifelse(input[,"hp"]>mean(input[,"hp"]),1,2)
    result3 <- aov(mpg ~ am + group + am:group, data = input)
    main_factor <- summary(result3)[[1]][["Pr(>F)"]][1]
    inter_factor <- summary(result3)[[1]][["Pr(>F)"]][3]
    p_value <- min(c(main_factor,inter_factor))
}
(1-p_value)