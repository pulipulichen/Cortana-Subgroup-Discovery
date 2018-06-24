package nl.liacs.subdisc.gui;

import java.awt.*;

import java.io.*;
import java.util.*;
import java.util.List;

import nl.liacs.subdisc.*;
import nl.liacs.subdisc.FileHandler.Action;
import nl.liacs.subdisc.gui.MiningWindow.*;

//import org.apache.commons.math3.stat.inference.*;
//import com.datumbox.framework.common.dataobjects.FlatDataCollection;
//import com.datumbox.framework.common.dataobjects.TransposeDataCollection;
//import com.datumbox.framework.core.statistics.parametrics.independentsamples.*;

public class SubDisc
{
	/*
	 * External jars required for correct execution.
	 * KNIME related jars are not required, when used as a KNIME plugin,
	 * KNIME loads is own jfreechart, jcommon and KNIME related jars.
	 */
	/*
	 * jfreechart, jcommon licence:
	 * Like JFreeChart, JCommon is also licensed under the terms of the GNU
	 * Lesser General Public Licence.
	 */
	/* 
	 * Jama licence:
	 * Copyright Notice This software is a cooperative product of
	 * The MathWorks and the National Institute of Standards and Technology
	 * (NIST) which has been released to the public domain.
	 * Neither The MathWorks nor NIST assumes any responsibility whatsoever
	 * for its use by other parties, and makes no guarantees, expressed or
	 * implied, about its quality, reliability, or any other characteristic.
	 */
	private static final String[] JARS = {
		// for drawing
		"jfreechart-1.0.14.jar",
		"jcommon-1.0.17.jar",
		// for propensity score, Rob
		"weka.jar",
		// for Cook's distance only
		"Jama-1.0.2.jar",
		// for KNIME
//		"knime-core.jar",
//		"org.eclipse.osgi_3.6.1.R36x_v20100806.jar",
//		"knime-base.jar",
//		"org.eclipse.core.runtime_3.6.0.v20100505.jar",
//		"org.knime.core.util_4.1.1.0034734.jar",
	};
	
	
	
	public static void main(String[] args)
	{
		//Log.logCommandLine(RserveUtil.run("print(\"1550\");if(!require(car)){install.packages(\"car\")};if(!require(emmeans)){install.packages(\"emmeans\")};if(!require(gtools)){install.packages(\"gtools\")};if(!require(plyr)){install.packages(\"plyr\")};if(!require(fANCOVA)){install.packages(\"fANCOVA\")};cortana_ancova <- function (input) {    reg.model <- aov(dv~iv*cov,data=input);    reg.interaction <- summary(reg.model)[[1]][[\"Pr(>F)\"]][3];    p.value <- 1;    pairwise.result <- c('null');    is.parametric.ancova <- TRUE;    if (is.null(reg.interaction) || is.na(reg.interaction) ) {        print('do nothing');    } else if (reg.interaction > 0.05) {        ancova.model <- aov(dv~as.factor(iv)+cov,data=input);        ancova.model.type3 <- Anova(ancova.model, type=3);        p.value <- ancova.model.type3[\"Pr(>F)\"][[1]][2];        ancova.compare.summary <- summary((emmeans(ancova.model, pairwise ~ iv, adjust = \"none\"))$contrasts);        compare.list <- strsplit(levels(ancova.compare.summary$contrast), \" - \");        compare.list <- data.frame(compare.list, row.names = c(\"iv1\", \"iv2\"));        ancova.compare.summary$iv1 <- unlist(compare.list[1,]);        ancova.compare.summary$iv2 <- unlist(compare.list[2,]);        ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$estimate > 0, paste(ancova.compare.summary$iv1, \">\", ancova.compare.summary$iv2), ifelse(ancova.compare.summary$estimate < 0, paste(ancova.compare.summary$iv2, \">\", ancova.compare.summary$iv1), paste(ancova.compare.summary$iv1, \"=\", ancova.compare.summary$iv2)));        ancova.compare.summary$contrast <- ifelse(ancova.compare.summary$p.value < 0.05, paste0(ancova.compare.summary$contrast, \"*\"), ancova.compare.summary$contrast);        pairwise.result <- ancova.compare.summary$contrast;    } else {        is.parametric.ancova <- FALSE;        iv.levels <- levels(factor(input[,\"iv\"]));        iv.comb <- combinations(n=as.integer(summary(iv.levels)[\"Length\"]), r=2, v=iv.levels, repeats.allowed=F);        iv.comb.freq <- length((as.list(iv.comb)))/2;        iv.comb <- cbind(iv.comb, c(rep(\"=\",iv.comb.freq)));        iv.comb <- cbind(iv.comb, c(rep.int(NA,iv.comb.freq)));        iv.comb <- cbind(iv.comb, c(rep(\"\",iv.comb.freq)));        p.value <- 1;        for (i in 1:iv.comb.freq) {            comb <- iv.comb[i,];            input.comb <- input[input$iv %in% comb, ];            input.comb <- input.comb[with(input.comb, order(iv)), ];            input.comb$iv.number <- unlist(lapply(input.comb$iv, function(ch) grep(ch, as.list(iv.levels))));            Taov.result <- NULL;            loess.result <- NULL;            retry.count <- 0;            while (is.null(Taov.result) && retry.count < 10) {                retry.count <- retry.count + 1;                input.comb$cov.jitter <- jitter(input.comb$cov, factor=0.2);                input.comb$dv.jitter <- jitter(input.comb$dv, factor=0.2);                tryCatch({                        Taov.result <- T.aov(input.comb[,\"cov\"], input.comb[,\"dv\"], input.comb[,\"iv.number\"], plot=FALSE, data.points=TRUE);                        print(\"Taov.result ok\");                    },                    warning = function(w) {},                     error=function(e){                });                if (is.null(Taov.result)) {                    tryCatch({                            Taov.result <- T.aov(input.comb$cov.jitter, input.comb$dv.jitter, input.comb$iv.number, plot=FALSE, data.points=TRUE);                            print(\"Taov.result 2 ok\");                        },                        warning = function(w2) {},                         error=function(e2){                    });                };                tryCatch({                        loess.result <- loess.ancova(input.comb$cov, input.comb$dv, input.comb$iv.number, plot=FALSE, data.points=TRUE);                        print(\"loess.result ok\");                    },                     warning = function(w) {},                     error=function(e){                });                if (is.null(loess.result)) {                    tryCatch({                            loess.result <- loess.ancova(input.comb$cov.jitter, input.comb$dv.jitter, input.comb$iv.number, plot=FALSE, data.points=TRUE);                            print(\"loess.result 2 ok\");                        },                        warning = function(w2) {},                         error=function(e2){                    });                };            };                        if (is.null(Taov.result) || is.null(loess.result)) {                next;            };            fitted.data <- data.frame(loess.result$smooth.fit$fitted, input.comb$iv);            colnames(fitted.data)<- c(\"fitted\",\"iv\");            fitted.data1 <- fitted.data[fitted.data$iv %in% comb[1], ];            fitted.data1.mean <- mean(fitted.data1[,\"fitted\"]);            fitted.data2 <- fitted.data[fitted.data$iv %in% comb[2], ];            fitted.data2.mean <- mean(fitted.data2[,\"fitted\"]);            iv.comb[i,3] <- Taov.result$p.value;            p.value <- ifelse(Taov.result$p.value < p.value, Taov.result$p.value, p.value);            if (fitted.data1.mean > fitted.data2.mean) {                iv.comb[i,5] <- paste(iv.comb[i,1], \">\", iv.comb[i,2]);            } else if (fitted.data1.mean < fitted.data2.mean) {                iv.comb[i,5] <- paste(iv.comb[i,2], \">\", iv.comb[i,1]);            } else {                iv.comb[i,5] <- paste(iv.comb[i,1], \"=\", iv.comb[i,2]);            };            if (iv.comb[i,3] < 0.05) {                iv.comb[i,5] <- paste0(iv.comb[i,5], \"*\");            };        };        if (iv.comb[,5] != \"\") {            pairwise.result <- iv.comb[,5];        };    };    paste(is.parametric.ancova, sprintf(\"%.5f\", p.value), paste(pairwise.result, collapse=\";\"), sep=\",\");};"));
		//Log.logCommandLine(RserveUtil.run("cortana_ancova(data.frame(iv = c(\"控制組\",\"控制組\",\"控制組\",\"控制組\",\"控制組\",\"控制組\",\"控制組\",\"控制組\",\"控制組\",\"控制組\",\"實驗組\",\"實驗組\",\"實驗組\",\"實驗組\",\"實驗組\",\"實驗組\",\"實驗組\",\"實驗組\",\"實驗組\",\"實驗組\"),cov = c(11.0,12.0,19.0,13.0,17.0,15.0,17.0,14.0,13.0,16.0,11.0,14.0,10.0,12.0,12.0,13.0,10.0,15.0,14.0,11.0),dv = c(21.0,23.0,25.0,23.0,23.0,24.0,24.0,20.0,22.0,24.0,21.0,24.0,21.0,20.0,23.0,24.0,23.0,21.0,25.0,24.0)));"));
		
		
		//Log.logCommandLine("start cortana");
		
		checkLibs();
		
		//Log.logCommandLine("start cortana 1");
		
		if (!GraphicsEnvironment.isHeadless() && (SplashScreen.getSplashScreen() != null))
		{
			// assume it is an XML-autorun experiment
			if (args.length > 0)
				SplashScreen.getSplashScreen().close();
			else
			{
				try { Thread.sleep(3000); }
				catch (InterruptedException e) {};
				SplashScreen.getSplashScreen().close();
			}
		}
		
		//Log.logCommandLine("start cortana 2");

		if (XMLAutoRun.autoRunSetting(args)) {
			return;
		}
		
		
		//Log.logCommandLine("start cortana 3");
		
		TestUtil.start();
		
		//Log.logCommandLine("start cortana 4");

		//Log.logCommandLine("error: " + JARTextFileLoader.load("/r-scripts/ancova_sm_ancova_full.R"));
		/*
		String input = "Sl\"o,pe";
		if (input.indexOf("\"") > -1 || input.indexOf(",") > -1) {
			input = input.replaceAll("\"", "\\\\\"");
			input = "\"" + input + "\""; 
		}
		Log.logCommandLine("save: " + input + input.indexOf("\""));
		*/
		
		
		/*
		String [] iv = {"C","C","C","C","C","C","C","E","E","E","E","E","E"};
		float[] cov = {11,12,19,13,15,17,13,11,14,10,12,12,11};
		float[] dv = { 21,23,25,23,24,24,22,21,24,21,20,23,24 };
		AncovaMeasure p = new AncovaMeasure(null, iv, cov, dv) ;
		Log.logCommandLine("AncovaMeasure: " + p.getFstatPvalInvert());
		*/
		
		//RserveUtil.startup();
		if (ConfigIni.getBoolean("rserve", "TaskkillWhenStartup")) {
			RserveUtil.taskkill();
		}
		
		
		// 20180414 Open the main window directly without open a file.
		try {
			//ConfigIni.set("search strategy", "RPath2", "ccd");
			
			MiningWindow w = new MiningWindow();
			
			//Log.logCommandLine("start cortana 5");
			
			// 20180616 開啟Cortana並且讀取檔案
			w.actionPerformed("Open File");
			
			//Log.logCommandLine("start cortana 6");
			
			if (ConfigIni.get("global", "DefaultLoadFile") != null
					&& ConfigIni.getBoolean("global", "AutoStartSubgroupDiscovery")) {
				w.actionPerformed("Subgroup Discovery");
			}
			if (ConfigIni.getBoolean("global", "AutoExplore")) {
				w.actionPerformed("Explore...");
			}
			if (ConfigIni.getBoolean("global", "AutoMetaData")) {
				w.actionPerformed("Meta Data...");
			}
			if (ConfigIni.getBoolean("global", "AutoBrowse")) {
				w.actionPerformed("Browse...");
			}
			if (ConfigIni.getBoolean("global", "AutoDistribution")) {
				w.actionPerformed("Distribution...");
			}
			if (ConfigIni.getBoolean("global", "AutoExit")) {
				w.actionPerformed("Exit");
			}
		}
		catch (Exception e) {
			Log.logCommandLine("MiningWindow error:" + e.getMessage());
		}
			
		
		//Log.logCommandLine("start cortana 7");
		
		//Log.logCommandLine("" + ConfigIni.getBoolean("global", "SubgroupDiscoveryAutoStart"));
		
		/*
		FileHandler aLoader = new FileHandler(Action.OPEN_FILE);
		Table aTable = aLoader.getTable();
		SearchParameters aSearchParameters = aLoader.getSearchParameters();

		if (aTable == null) {
			//new MiningWindow();
			// do nothing
		}
		else if (aSearchParameters == null) {
			//new MiningWindow(aTable);
			w.setup(aTable);
		}
		else {
			//new MiningWindow(aTable, aSearchParameters);
			w.setup(aTable, aSearchParameters);
		}
		*/
	}

	// may move to a separate class
	private static void checkLibs() {
/*
		// spaces in paths may give problems, leave code in just in case
		String path = SubDisc.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath;
		try {
			decodedPath = URLDecoder.decode(path, "UTF-8");
			path = new File(path).getParentFile().getPath();
		} catch (UnsupportedEncodingException e) {}
*/
		File dir = new File(new File("").getAbsolutePath());
		File libs = null;
		for (File f : dir.listFiles()) {
			if ("libs".equals(f.getName())) {
				libs = f;
				break;
			}
		}
		System.out.println("Starting Cortana from:");
		System.out.println("\t" + dir.getAbsolutePath());
		System.out.println("Looking for /libs/ directory...");
		System.out.format("/libs/ directory %sfound:%n", libs == null ? "not " : "");
		System.out.println("\t" + (libs == null ? "" : libs.getAbsolutePath()));

		if (libs != null) {
			System.out.format("Looking for required jars (%d)...%n",
						JARS.length);
			checkJars(libs);
		} else {
			System.out.println("Most drawing functionality will not work.");
		}
	}

	private static void checkJars(File libsDir) {
		final String[] files = libsDir.list();
		List<String> notFound = new ArrayList<String>();
		OUTER: for (String jar : JARS) {
				for (String file : files) {
					if (jar.equals(file)) {
						System.out.format("\tFound: '%s'.%n", jar);
						continue OUTER;
					}
				}
				notFound.add(jar);
			}

		if (!notFound.isEmpty())
			tryHarder(notFound, files);
	}

	/*
	 * If another version is found it might work.
	 * Newer version may have removed deprecated methods, older version may
	 * not have implemented some of the required methods.
	 * 
	 * TODO cortana.mf's Class-Path attribute defines the required jars,
	 * other libraries will not be loaded automatically, could be done here.
	 */
	private static void tryHarder(List<String> jars, String[] files) {
		OUTER: for (String jar : jars) {
				String base = jar.substring(0, jar.indexOf("-"));
				for (String file : files) {
					if (file.startsWith(base)) {
						System.out.format("\tFound: '%s', ('%s' expected).%n",
									file,
									jar);
						continue OUTER;
					}
				}
				System.out.format("\t'%s' not found, some functions will not work.%n", jar);
			}
	}
}
