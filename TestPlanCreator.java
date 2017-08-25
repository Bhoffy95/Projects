import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;

import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.assertions.SizeAssertion;
import org.apache.jmeter.assertions.gui.AssertionGui;
import org.apache.jmeter.assertions.gui.SizeAssertionGui;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.CSVDataSet;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.TransactionController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.control.gui.TransactionControllerGui;
import org.apache.jmeter.extractor.BeanShellPostProcessor;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.extractor.gui.RegexExtractorGui;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.sampler.TestAction;
import org.apache.jmeter.sampler.gui.TestActionGui;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.protocol.java.control.gui.BeanShellSamplerGui;
import org.apache.jmeter.protocol.java.sampler.BeanShellSampler;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.SummaryReport;
import org.apache.jmeter.visualizers.backend.BackendListener;
import org.apache.jmeter.visualizers.backend.BackendListenerGui;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;

import com.blazemeter.jmeter.threads.concurrency.ConcurrencyThreadGroup;
import com.blazemeter.jmeter.threads.concurrency.ConcurrencyThreadGroupGui;

import kg.apc.jmeter.listener.GraphsGeneratorListener;




public class TestPlanCreator {
		
	public static int homePageLoops = 1;
	public static int loginPageLoops = 1;
	public static int selectionPageLoops = 5;
	public static int recommendationPageLoops = 10;
	public static int detailsPageLoops = 0;
	public static int userLoops = 7;
	public static int numUsers = 7;
	public static int rampUpTime = 70;
	public static int targetRunTime = 90; //each iteration's run time
	public static String jmeterPath = "C:\\JMeter\\apache-jmeter-3.2\\bin";
	
	public TestPlanCreator(){
		
	}
	
	public void makeHTTPRequest(HTTPSamplerProxy sampler, String name, String method, String Path, String[] arguments){
		sampler.setName(name);
		sampler.setMethod(method);
		sampler.setPath(Path);
		sampler.setDomain("");
		if(arguments != null){
		for(int i=0; i < arguments.length; i += 2){
			sampler.addArgument(arguments[i], arguments[i+1]);
		  }
		}
		sampler.setFollowRedirects(true);
		sampler.setUseKeepAlive(true);
		//following lines allow the testElement to be saved into a test plan. They are in every "make" method
		sampler.setProperty(TestElement.TEST_CLASS, HTTPSamplerProxy.class.getName());
		sampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
	}
	
	public void makeHeaderManager(HeaderManager manager, String name, String[] headerNames, String[] headerValues){
		manager.setName(name);
		for(int i=0; i<headerNames.length; i++){
			Header header = new Header();
			header.setName(headerNames[i]);
			header.setValue(headerValues[i]);
			manager.add(header);
		}
		manager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
		manager.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());
	}
	
	
	public void makeLoopController(LoopController controller, String name, int loops){
		controller.setName(name);
		controller.setLoops(loops);
		controller.setFirst(true);
		controller.setProperty(TestElement.TEST_CLASS, LoopController.class.getName());
		controller.setProperty(TestElement.GUI_CLASS, LoopControlPanel.class.getName());
		controller.initialize();
	}
	
	
	public void makeTransactionController(TransactionController controller, String name){
		
		controller.setName(name);
		controller.setIncludeTimers(false);
		controller.setGenerateParentSample(false);
		controller.setProperty(TestElement.TEST_CLASS, TransactionController.class.getName());
		controller.setProperty(TestElement.GUI_CLASS, TransactionControllerGui.class.getName());
	}
	
	public void makeResponseAssertion(ResponseAssertion assertion, String name, boolean not, String testString){
		
		assertion.setName(name);
		assertion.addTestString(testString);
		assertion.setToSubstringType();
		assertion.setTestFieldResponseData();
		if(not){
			assertion.setToNotType();
		}
		assertion.setProperty(TestElement.TEST_CLASS, ResponseAssertion.class.getName());
		assertion.setProperty(TestElement.GUI_CLASS, AssertionGui.class.getName());
	}
	
	
	public void makeSizeAssertion(SizeAssertion assertion, String name, int size){
		assertion.setName(name);
		assertion.setAllowedSize(size);
		assertion.setCompOper(1);
		assertion.setTestFieldNetworkSize();
		assertion.setProperty(TestElement.TEST_CLASS, SizeAssertion.class.getName());
		assertion.setProperty(TestElement.GUI_CLASS, SizeAssertionGui.class.getName());
	}
	
	public void makeListener(ResultCollector listener, String name, String fileName){
		SummaryReport sumRep = new SummaryReport();
		listener.setName(name);
		listener.setFilename(fileName);
		listener.setListener(sumRep);
		listener.setProperty(TestElement.TEST_CLASS, ResultCollector.class.getName());
		listener.setProperty(TestElement.GUI_CLASS, SummaryReport.class.getName());
	}
	
	public void makeThreadGroup(ThreadGroup group, String name, int threads, int rampUp, LoopController loopController){
		group.setName(name);
		group.setNumThreads(threads);
		group.setRampUp(rampUp);
		group.setSamplerController(loopController);
		group.setProperty(TestElement.TEST_CLASS, ThreadGroup.class.getName());
		group.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());
	}
	
	public void makeTestPlan(TestPlan plan, String name){
		plan.setName(name);
		plan.setProperty(TestElement.TEST_CLASS, TestPlan.class.getName());
		plan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
		plan.setUserDefinedVariables((Arguments) new ArgumentsPanel().createTestElement());
	}
	
	public void makeRegExExtractor(RegexExtractor regEx, String name, String expression, String matchNum, String template, String variable){
		
		regEx.setName(name);
		regEx.setRegex(expression);
		regEx.setMatchNumber(matchNum);
		regEx.setTemplate(template);
		regEx.setRefName(variable);
		regEx.setProperty(TestElement.TEST_CLASS, RegexExtractor.class.getName());
		regEx.setProperty(TestElement.GUI_CLASS, RegexExtractorGui.class.getName());
	}
	
	public void makeBeanShellPostProcessor(BeanShellPostProcessor beanShell, String script, String name){
		
		beanShell.setName(name);
		beanShell.setProperty(BeanShellSampler.SCRIPT, script);
		beanShell.setProperty(TestElement.TEST_CLASS, BeanShellPostProcessor.class.getName());
		beanShell.setProperty(TestElement.GUI_CLASS, BeanShellSamplerGui.class.getName());
		
	}
	
	public void makeBeanShellSampler(BeanShellSampler sampler, String script, String name){
		
		sampler.setName(name);
		sampler.setProperty(BeanShellSampler.SCRIPT, script);
		sampler.setProperty(TestElement.TEST_CLASS, BeanShellSampler.class.getName());
		sampler.setProperty(TestElement.GUI_CLASS, BeanShellSamplerGui.class.getName());
		
	}
	
	public void makeCSVDataSet(CSVDataSet set, String name, String fileName, boolean recycle){
		
		set.setName(name);
		set.setProperty("delimiter", ",");
		set.setProperty("filename", fileName);
		set.setProperty("recycle", recycle);
		set.setProperty("variableNames", "storeID,zipCodeFile");
		set.setProperty("ignoreFirstLine", true);
		set.setProperty(TestElement.TEST_CLASS, CSVDataSet.class.getName());
		set.setProperty(TestElement.GUI_CLASS, TestBeanGUI.class.getName());
		
	}
	
	public void makeTestAction(TestAction action, String name, String duration){
		
		action.setName(name);
		action.setDuration(duration);
		action.setAction(1);
		action.setProperty(TestElement.TEST_CLASS, TestAction.class.getName());
		action.setProperty(TestElement.GUI_CLASS, TestActionGui.class.getName());
		
	}
	public void makeGraphsGen(GraphsGeneratorListener listener, String name, String prefix, String outputFolder, String offset){
		listener.setName(name);
		listener.setFilePrefix(prefix);
		listener.setOutputBaseFolder(outputFolder);
		listener.setStartOffset(offset);
		listener.setEndOffset(offset);
		listener.setExportMode(2);
		listener.setGranulation("30000");
		listener.setProperty(TestElement.TEST_CLASS, GraphsGeneratorListener.class.getName());
		listener.setProperty(TestElement.GUI_CLASS, TestBeanGUI.class.getName());
	}
	
	public void makeBackendListener(BackendListener listener, String name,String host, String port, String regex){
		Arguments args = new Arguments();
		args.addArgument("graphiteMetricsSender", "org.apache.jmeter.visualizers.backend.graphite.TextGraphiteMetricsSender");
		args.addArgument("graphiteHost", host);
		args.addArgument("graphitePort", port);
		args.addArgument("rootMetricsPrefix", "PDL.");
		args.addArgument("summaryOnly", "false");
		args.addArgument("samplersList", regex);
		args.addArgument("useRegexpForSamplersList", "true");
		args.addArgument("percentiles", "90;95;99");
		listener.setClassname("org.apache.jmeter.visualizers.backend.graphite.GraphiteBackendListenerClient");
		listener.setArguments(args);
		listener.setQueueSize("5000");
		listener.setName(name);
		listener.setProperty(TestElement.TEST_CLASS, BackendListener.class.getName());
		listener.setProperty(TestElement.GUI_CLASS, BackendListenerGui.class.getName());
		
	}
	
	public ListedHashTree constructHomePage(){
		
		TestPlanCreator homeCreator = new TestPlanCreator();
		
		HTTPSamplerProxy rootConnect = new HTTPSamplerProxy();
		homeCreator.makeHTTPRequest(rootConnect, "1-1_Root_Connect", "GET", "/", null);
		rootConnect.setImageParser(true);
		rootConnect.setEmbeddedUrlRE("");
		HTTPSamplerProxy homePageFont = new HTTPSamplerProxy();
		homeCreator.makeHTTPRequest(homePageFont, "1-2_Get_Font", "GET", "", null);
		HTTPSamplerProxy homePageWeather = new HTTPSamplerProxy();
		homeCreator.makeHTTPRequest(homePageWeather, "1-3_Get_Weather", "GET", "", null);
		HTTPSamplerProxy homePageBGTire = new HTTPSamplerProxy();
		homeCreator.makeHTTPRequest(homePageBGTire, "1-4_Get_Png", "GET", "", null);
		
		//a forced wait that will not add time to the transaction. Use these for Think Time.
		TestAction homePause = new TestAction();
		homeCreator.makeTestAction(homePause, "Home Page Pause", "1500");
		
		//Create assertions. Use "Not" assertions to assume something will not be present.
		ResponseAssertion rootAssertion = new ResponseAssertion();
		homeCreator.makeResponseAssertion(rootAssertion, "Root Response Asseriton", false, "https://www.google-analytics.com/analytics.js");
		rootAssertion.setToContainsType();
		SizeAssertion homeFontAssertion = new SizeAssertion();
		homeCreator.makeSizeAssertion(homeFontAssertion, "Home Page Font Size Assertion", 17302);
		ResponseAssertion homeWeatherAssertion = new ResponseAssertion();
		homeCreator.makeResponseAssertion(homeWeatherAssertion, "Home Weather Fail Assertion", false, "Unable to find weather information");
		SizeAssertion homeTireBGAssertion = new SizeAssertion();
		homeCreator.makeSizeAssertion(homeTireBGAssertion, "Home Tire-BG PNG", 31355);
		
		String[] rootNames = {"Accept-Language", "Accept-Encoding", "User-Agent", "Accept"};
		String[] rootValues = {"en-US,en;q=0.5", "gzip, deflate", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"};
		HeaderManager rootHM = new HeaderManager();
		homeCreator.makeHeaderManager(rootHM, "Root Connect Header Manager", rootNames, rootValues);
		HeaderManager homeFontHM = new HeaderManager();
		String[] homeFontNames = {"Referer", "Accept-Language", "Accept-Encoding", "User-Agent", "Accept"};
		String[] homeFontValues = {"", "en-US,en;q=0.5", "identity", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0", "application/font-woff2;q=1.0,application/font-woff;q=0.9,*/*;q=0.8"};
		homeCreator.makeHeaderManager(homeFontHM, "Home Page Fonts Header Manager", homeFontNames, homeFontValues);
		HeaderManager homeWeatherHM = new HeaderManager();
		String[] homeWeatherNames = {"Referer", "Accept-Language", "Accept-Encoding", "User-Agent", "Accept"};
		String[] homeWeatherValues = {"", "en-US,en;q=0.5", "gzip, deflate", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0", "application/json, text/plain, */*"};
		homeCreator.makeHeaderManager(homeWeatherHM, "Home Weather Header Manager", homeWeatherNames, homeWeatherValues);
		String[] dynatraceName = {"x-dynaTrace"};
		String[] dynatraceVal = {"01_HomePage_Jmeter"};
		HeaderManager dynatraceHM = new HeaderManager();
		homeCreator.makeHeaderManager(dynatraceHM, "Home Dynatrace HM", dynatraceName, dynatraceVal);
		
		TransactionController homeController = new TransactionController();
		homeCreator.makeTransactionController(homeController, "01_Home_Page");
		
		LoopController homeLoop = new LoopController();
		homeCreator.makeLoopController(homeLoop, "Home Page Loop Controller", homePageLoops);
		
		ListedHashTree homeGroup = new ListedHashTree();
		HashTree homeLoopTree = homeGroup.add(homeLoop);
		HashTree homeTree = homeLoopTree.add(homeController);
		HashTree homeRootTree = homeTree.add(rootConnect);
		homeRootTree.add(rootHM);
		homeRootTree.add(rootAssertion);
		HashTree homeFontTree = homeTree.add(homePageFont);
		homeFontTree.add(homeFontAssertion);
		homeFontTree.add(homeFontHM);
		HashTree homeWeatherTree = homeTree.add(homePageWeather);
		homeWeatherTree.add(homeWeatherHM);
		homeWeatherTree.add(homeWeatherAssertion);
		HashTree homeTireBGTree = homeTree.add(homePageBGTire);
		homeTireBGTree.add(homeTireBGAssertion);
		homeTree.add(homePause);
		homeTree.add(dynatraceHM);
		
		
		return homeGroup;
		
	}
	
	
	public ListedHashTree constructLoginPage(){
		
		TestPlanCreator creator = new TestPlanCreator();
		
		HTTPSamplerProxy loginStore1 = new HTTPSamplerProxy();
		String[] loginArgs = {"site", "${storeID}"};
		creator.makeHTTPRequest(loginStore1, "2-1_Login_Store1", "GET", "", loginArgs);
		HTTPSamplerProxy loginStore2 = new HTTPSamplerProxy();
		creator.makeHTTPRequest(loginStore2, "2-2_Login_Store2", "GET", "", loginArgs);
		HTTPSamplerProxy loginYears = new HTTPSamplerProxy();
		creator.makeHTTPRequest(loginYears, "2-3_Login_Years", "GET", "", null);
		HTTPSamplerProxy loginWeather = new HTTPSamplerProxy();
		String[] weatherArgs = {"zipCode", "${zipCode}"};
		creator.makeHTTPRequest(loginWeather, "2-4_Login_Weather", "GET", "", weatherArgs);
		HTTPSamplerProxy loginFont = new HTTPSamplerProxy();
		creator.makeHTTPRequest(loginFont, "2-5_Login_Weather", "GET", "", null);
		HTTPSamplerProxy loginArrowPng = new HTTPSamplerProxy();
		creator.makeHTTPRequest(loginArrowPng, "2-6_Login_ArrowPng", "GET", "", null);
		HTTPSamplerProxy loginPointPng = new HTTPSamplerProxy();
		creator.makeHTTPRequest(loginPointPng, "2-7_Login_PointPng", "GET", "", null);
		HTTPSamplerProxy loginMoveDotsPng = new HTTPSamplerProxy();
		creator.makeHTTPRequest(loginMoveDotsPng, "2-8_Login_DotsPng", "GET", "", null);
		
		TestAction loginPause = new TestAction();
		creator.makeTestAction(loginPause, "Login Pause", "1500");
		
		ResponseAssertion loginStore1_1Assertion = new ResponseAssertion();
		creator.makeResponseAssertion(loginStore1_1Assertion, "Login Store 1 Assertion 2", false, "\"message\":\"success\"");
		ResponseAssertion loginStore2Assertion = new ResponseAssertion();
		creator.makeResponseAssertion(loginStore2Assertion, "Login Store 2 Assertion 2", false, "\"message\":\"success\"");
		ResponseAssertion loginYearsAssertion = new ResponseAssertion();
		creator.makeResponseAssertion(loginYearsAssertion, "Login Years Assertion", false, "\"message\":\"success\"");
		ResponseAssertion loginWeatherAssertion = new ResponseAssertion();
		creator.makeResponseAssertion(loginWeatherAssertion, "Verify Zip Assertion", false, "${zipCodeFile}");
		creator.makeResponseAssertion(loginWeatherAssertion, "Login Weather Assertion", true, "Unable to find weather information");
		SizeAssertion loginFontAssertion = new SizeAssertion();
		creator.makeSizeAssertion(loginFontAssertion, "Login Font Assertion", 17358);
		SizeAssertion loginArrowAssertion = new SizeAssertion();
		creator.makeSizeAssertion(loginArrowAssertion, "Login Arrow Png Size Assertion", 487);
		SizeAssertion loginPointAssertion = new SizeAssertion();
		creator.makeSizeAssertion(loginPointAssertion, "Login Point Png Size Assertion", 1074);
		SizeAssertion loginMoveDotsAssertion = new SizeAssertion();
		creator.makeSizeAssertion(loginMoveDotsAssertion, "Login Move Dots Size Asseriton", 471);
		
		HeaderManager loginStore1HM = new HeaderManager();
		String[] store1Names = {"Referer", "Accept-Language", "Accept-Encoding", "User-Agent", "Accept"};
		String[] store1Values = {"", "en-US,en;q=0.5", "gzip, deflate", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0", "application/json, text/plain, */*"};
		creator.makeHeaderManager(loginStore1HM, "Login Store 1 Header Manager", store1Names, store1Values);
		HeaderManager loginStore2HM = new HeaderManager();
		creator.makeHeaderManager(loginStore2HM, "Login Store 2 Header Manager", store1Names, store1Values);
		HeaderManager loginYearsHM = new HeaderManager();
		String[] loginNames = {"Referer", "Accept-Language", "Accept-Encoding", "User-Agent", "Accept"};
		String[] loginYearsValues = {"http://stg.tirefinder.trtc.com/", "en-US,en;q=0.5", "gzip, deflate", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0", "application/json, text/plain, */*"};	
		creator.makeHeaderManager(loginYearsHM, "Login Years Header Manager", loginNames, loginYearsValues);
		HeaderManager loginWeatherHM = new HeaderManager();
		creator.makeHeaderManager(loginWeatherHM, "Login Weather Header Manager", loginNames, loginYearsValues);
		HeaderManager loginFontsHM = new HeaderManager();
		String[] loginFontValues = {"http://stg.tirefinder.trtc.com/dist/main.css", "en-US,en;q=0.5", "identity", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0", "application/font-woff2;q=1.0,application/font-woff;q=0.9,*/*;q=0.8"};
		creator.makeHeaderManager(loginFontsHM, "Login Font Header Manager", loginNames, loginFontValues);
		String[] dynatraceName = {"x-dynaTrace"};
		String[] dynatraceVal = {"NA=PDL_02_LoginPage_Jmeter"};
		HeaderManager dynatraceHM = new HeaderManager();
		creator.makeHeaderManager(dynatraceHM, "Login Dynatrace HM", dynatraceName, dynatraceVal);
		
		RegexExtractor siteRegex = new RegexExtractor();
		creator.makeRegExExtractor(siteRegex, "Site Number Extractor", "\"siteNumber\":([0-9]*)", "1", "$1$", "siteNumber");
		RegexExtractor yearsRegex = new RegexExtractor();
		creator.makeRegExExtractor(yearsRegex, "Random Year Extractor", "\"(20[0-9]{2})\"", "0", "$1$", "year");
		RegexExtractor zipRegex = new RegexExtractor();
		creator.makeRegExExtractor(zipRegex, "Zip Code Extractor", "zipCode\":\"([0-9]*)", "1", "$1$", "zipCode");
		
		LoopController loopControl = new LoopController();
		creator.makeLoopController(loopControl, "Login Loop Controller", loginPageLoops);
		
		TransactionController loginGroupControl = new TransactionController();
		creator.makeTransactionController(loginGroupControl, "PDL_02_Login_Page");
		
		ListedHashTree loginGroup = new ListedHashTree();
		HashTree loginLoopTree = loginGroup.add(loopControl);
		HashTree loginTree = loginLoopTree.add(loginGroupControl);
		HashTree loginStore1Tree = loginTree.add(loginStore1);
		loginStore1Tree.add(loginStore1HM);
		loginStore1Tree.add(loginStore1_1Assertion);
		loginStore1Tree.add(siteRegex);
		loginStore1Tree.add(zipRegex);
		HashTree loginStore2Tree = loginTree.add(loginStore2);
		loginStore2Tree.add(loginStore2Assertion);
		loginStore2Tree.add(loginStore2HM);
		HashTree loginArrowTree = loginTree.add(loginArrowPng);
		loginArrowTree.add(loginArrowAssertion);
		HashTree loginPointTree = loginTree.add(loginPointPng);
		loginPointTree.add(loginPointAssertion);
		HashTree loginMoveDots = loginTree.add(loginMoveDotsPng);
		loginMoveDots.add(loginMoveDotsAssertion);
		HashTree loginYearsTree = loginTree.add(loginYears);
		loginYearsTree.add(loginYearsHM);
		loginYearsTree.add(loginYearsAssertion);
		loginYearsTree.add(yearsRegex);
		HashTree loginWeatherTree = loginTree.add(loginWeather);
		loginWeatherTree.add(loginWeatherHM);
		loginWeatherTree.add(loginWeatherAssertion);
		HashTree loginFontTree = loginTree.add(loginFont);
		loginFontTree.add(loginFontsHM);
		loginFontTree.add(loginFontAssertion);
		loginTree.add(loginPause);
		loginTree.add(dynatraceHM);
		
		return loginGroup;
		
	}
	
	public ListedHashTree constructSelectPage(){
		
		TestPlanCreator creator = new TestPlanCreator();
		
		HTTPSamplerProxy selectYears = new HTTPSamplerProxy();
		String[] argsMakeModel = {"year", "${year}"};
		creator.makeHTTPRequest(selectYears, "PDL_3-1_Select_Years", "GET", "/api/vehicle/makeModels", argsMakeModel);
		HTTPSamplerProxy selectMakeModel = new HTTPSamplerProxy();
		String[] argsTrims = {"year", "${year}", "make", "${make}", "model", "${model}"};
		creator.makeHTTPRequest(selectMakeModel, "PDL_4-1_Select_MakeModel", "GET", "/api/vehicle/trims", argsTrims);
		HTTPSamplerProxy selectStore = new HTTPSamplerProxy();
		String[] store = {"site", "${storeID}"};
		creator.makeHTTPRequest(selectStore, "5-1_Trims_Store", "GET", "", store);
		HTTPSamplerProxy selectYears2 = new HTTPSamplerProxy();
		creator.makeHTTPRequest(selectYears2, "5-2_Trims_Year", "GET", "", argsMakeModel);
		HTTPSamplerProxy selectMakeModel2 = new HTTPSamplerProxy();
		creator.makeHTTPRequest(selectMakeModel2, "5-3_Trims_MakeModel", "GET", "", argsTrims);
		HTTPSamplerProxy selectFont = new HTTPSamplerProxy();
		creator.makeHTTPRequest(selectFont, "5-4_Trims_Font", "GET", "", null);
		HTTPSamplerProxy selectTrims = new HTTPSamplerProxy();
		String[] tireSizesArgs = {"vehicleID", "${vehicleID}", "trimID", "${trimID}", "assemblyID", "${assemblyID}", "chassisID", "${chassisID}", "isStaggered", "${isStaggered}"};
		creator.makeHTTPRequest(selectTrims, "5-5_Select_Trims", "GET", "", tireSizesArgs);
		HTTPSamplerProxy selectTires = new HTTPSamplerProxy();
		String[] tiresArgs = {"vehicleID", "${vehicleID}", "trimID", "${trimID}", "assemblyID", "${assemblyID}", "chassisID", "${chassisID}", 
				"f_cross_section", "${crossSection}", "f_aspect_ratio", "${aspectRatio}", "f_rim_size", "${diameter}", "r_cross_section", "${crossSection}",
				"r_aspect_ratio", "${aspectRatio}", "r_rim_size", "${diameter}", "isStaggered", "${isStaggered}"};
		creator.makeHTTPRequest(selectTires, "5-6_Trims_Tires", "GET", "", tiresArgs);
		
		TestAction yearsPause = new TestAction();
		creator.makeTestAction(yearsPause, "Years Pause", "2000");
		TestAction makeModelPause = new TestAction();
		creator.makeTestAction(makeModelPause, "Make Model Pause", "1000");
		TestAction trimsPause = new TestAction();
		creator.makeTestAction(trimsPause, "Trims Pause", "1500");
		
		ResponseAssertion selectMakeModelAssertion = new ResponseAssertion();
		creator.makeResponseAssertion(selectMakeModelAssertion, "Select MakeModel Assertion", false, "\"serviceFailure\":false,\"message\":\"success\"");
		ResponseAssertion selectTrimsAssertion = new ResponseAssertion();
		creator.makeResponseAssertion(selectTrimsAssertion, "Select Trims Assertion", false, "\"trims\"");
		creator.makeResponseAssertion(selectTrimsAssertion, "Select Trims Assertion 2", false, "{\"serviceFailure\":false,\"message\":\"success\"}");
		ResponseAssertion selectStoreAssertion = new ResponseAssertion();
		creator.makeResponseAssertion(selectStoreAssertion, "Select Store Assertion", false, "siteNumber\":${siteNumber}");
		ResponseAssertion selectTireSizesAssertion = new ResponseAssertion();
		creator.makeResponseAssertion(selectTireSizesAssertion, "Select Tire Sizes Assertion", false, "\"message\":\"success\"");
		creator.makeResponseAssertion(selectTireSizesAssertion, "Select Tire Sizes Assertion", false, "\"vehicleSegment\"");
		creator.makeResponseAssertion(selectTireSizesAssertion, "Select Tire Sizes Assertion", false, "\"tireSizes\"");
		ResponseAssertion selectTiresAssertion = new ResponseAssertion();
		creator.makeResponseAssertion(selectTiresAssertion, "Select Tires Assertion", false, "\"message\":\"success\"");
		creator.makeResponseAssertion(selectTiresAssertion, "Select Tires Assertion", false, "\"tires\"");
		ResponseAssertion selectTiresNotAssertion = new ResponseAssertion();
		creator.makeResponseAssertion(selectTiresNotAssertion, "Select Tires Not Assertion", true, "Unable to find tires that match the current size");
		SizeAssertion selectFontSizeAssertion = new SizeAssertion();
		creator.makeSizeAssertion(selectFontSizeAssertion, "Select Font Size Assertion", 17114);
		ResponseAssertion vehicleFoundAssertion = new ResponseAssertion();
		creator.makeResponseAssertion(vehicleFoundAssertion, "does the vehicle have an ID", true, "vehicleID\":null");
		
		HeaderManager selectMakeModelHM = new HeaderManager();
		String[] makeModelHMNames = {"Referer", "Accept-Language", "Accept-Encoding", "User-Agent", "Accept"};
		String[] makeModelHMValues = {"", "en-US,en;q=0.5", "gzip, deflate", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0", "application/json, text/plain, */*"};
		creator.makeHeaderManager(selectMakeModelHM, "Select Make Models Header Manager", makeModelHMNames, makeModelHMValues);
		HeaderManager selectStoreHM = new HeaderManager();
		creator.makeHeaderManager(selectStoreHM, "Select Store Header Manager", makeModelHMNames, makeModelHMValues);
		HeaderManager selectTrimsHM = new HeaderManager();
		creator.makeHeaderManager(selectTrimsHM, "Select Trims Header Manager", makeModelHMNames, makeModelHMValues);
		HeaderManager selectTireSizesHM = new HeaderManager();
		creator.makeHeaderManager(selectTireSizesHM, "Select Tire Sizes Header Manager", makeModelHMNames, makeModelHMValues);
		HeaderManager selectTiresHM = new HeaderManager();
		creator.makeHeaderManager(selectTiresHM, "Select Tires Header Manager", makeModelHMNames, makeModelHMValues);
		HeaderManager selectFontHM = new HeaderManager();
		String[] selectFontValues = {"", "en-US,en;q=0.5", "identity", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0", "application/font-woff2;q=1.0,application/font-woff;q=0.9,*/*;q=0.8"};
		creator.makeHeaderManager(selectFontHM, "Select Font Header Manager", makeModelHMNames, selectFontValues);
		String[] yearsdynatraceName = {"x-dynaTrace"};
		String[] yearsdynatraceVal = {"NA=03_SelectYear_Jmeter"};
		HeaderManager yearsdynatraceHM = new HeaderManager();
		creator.makeHeaderManager(yearsdynatraceHM, "Year Dynatrace HM", yearsdynatraceName, yearsdynatraceVal);
		String[] mmdynatraceName = {"x-dynaTrace"};
		String[] mmdynatraceVal = {"NA=04_SelectMakeModel_Jmeter"};
		HeaderManager mmdynatraceHM = new HeaderManager();
		creator.makeHeaderManager(mmdynatraceHM, "MakeModel Dynatrace HM", mmdynatraceName, mmdynatraceVal);
		String[] trimdynatraceName = {"x-dynaTrace"};
		String[] trimdynatraceVal = {"NA=05_SelectTrims_Jmeter"};
		HeaderManager trimdynatraceHM = new HeaderManager();
		creator.makeHeaderManager(trimdynatraceHM, "Trims Dynatrace HM", trimdynatraceName, trimdynatraceVal);
		
		RegexExtractor vehicleIDRegex = new RegexExtractor();
		creator.makeRegExExtractor(vehicleIDRegex, "vehicleIDExtractor", "vehicleID\":\"([0-9]*)", "1", "$1$", "vehicleID");
		RegexExtractor trimsIDRegex = new RegexExtractor();
		creator.makeRegExExtractor(trimsIDRegex, "TrimsID Extractor", "trimID\":\"([0-9]*)", "1", "$1$", "trimID");
		trimsIDRegex.setScopeVariable("assemblyString");
		RegexExtractor yearRegex = new RegexExtractor();
		creator.makeRegExExtractor(yearRegex, "Year Extractor", "([a-zA-Z0-9\\s.]*)", "1", "$1$", "year");
		yearRegex.setScopeVariable("carString");
		RegexExtractor modelRegex = new RegexExtractor();
		creator.makeRegExExtractor(modelRegex, "Model Extractor", "([a-zA-Z0-9\\s.\\-&]*)", "3", "$1$", "model");
		modelRegex.setScopeVariable("carString");
		RegexExtractor makeRegex = new RegexExtractor();
		creator.makeRegExExtractor(makeRegex, "Make Extractor", "([a-zA-Z0-9\\s.\\-&]*)", "2", "$1$", "make");
		makeRegex.setScopeVariable("carString");
		RegexExtractor trimsRegex = new RegexExtractor();
		creator.makeRegExExtractor(trimsRegex, "Trims Extractor", "([a-zA-Z0-9\\s.\\-&]*)", "4", "$1$", "trims");
		trimsRegex.setScopeVariable("carString");
		RegexExtractor assemblyStringRegex = new RegexExtractor();
		creator.makeRegExExtractor(assemblyStringRegex, "Full Assembly String Extractor", "trimName\":\"${trims}\"([0-9a-zA-Z\\s\".:,\\-+%{}[]*)", "1", "$1$", "assemblyString");
		RegexExtractor chassisStringRegex = new RegexExtractor();
		creator.makeRegExExtractor(chassisStringRegex, "Chassis String Extractor", "chassisID\":\"([0-9a-zA-Z,.:\"\\-{}[]*)", "0", "$1$", "chassisString");
		chassisStringRegex.setScopeVariable("assemblyString");
		RegexExtractor chassisIDRegex = new RegexExtractor();
		creator.makeRegExExtractor(chassisIDRegex, "chassisID Extractor", "([0-9])", "1", "$1$", "chassisID");
		chassisIDRegex.setScopeVariable("chassisString");
		RegexExtractor assemblyIDRegex = new RegexExtractor();
		creator.makeRegExExtractor(assemblyIDRegex, "assemblyID Extractor", "assemblyID\":\"([A-Z])", "1", "$1$", "assemblyID");
		assemblyIDRegex.setScopeVariable("chassisString");
		RegexExtractor isStaggeredRegex = new RegexExtractor();
		creator.makeRegExExtractor(isStaggeredRegex, "isStaggered Extractor", "isStaggered\":\"([a-z]*)\"", "1", "$1$", "isStaggered");
		isStaggeredRegex.setScopeVariable("chassisString");
		RegexExtractor crossSectionRegex = new RegexExtractor();
		creator.makeRegExExtractor(crossSectionRegex, "Cross Section Extractor", "crossSection\":\"([0-9]*)", "1", "$1$", "crossSection");
		crossSectionRegex.setScopeVariable("chassisString");
		RegexExtractor aspectRatioRegex = new RegexExtractor();
		creator.makeRegExExtractor(aspectRatioRegex, "Aspect Ratio Extractor", "aspectRatio\":\"([0-9]*)", "1", "$1$", "aspectRatio");
		aspectRatioRegex.setScopeVariable("chassisString");
		RegexExtractor diameterRegex = new RegexExtractor();
		creator.makeRegExExtractor(diameterRegex, "Diameter Extractor", "diameter\":\"([0-9]*)", "1", "$1$", "diameter");
		diameterRegex.setScopeVariable("chassisString");
		RegexExtractor axleTypeRegex = new RegexExtractor();
		creator.makeRegExExtractor(axleTypeRegex, "AxleType Extractor", "axleType\":\"([A-Z])", "1", "$1$", "axleType");
		axleTypeRegex.setScopeVariable("chassisString");
		RegexExtractor tiresStringRegex = new RegexExtractor();
		creator.makeRegExExtractor(tiresStringRegex, "Full Tires String Extractor", "product_id\":([a-zA-Z0-9\",{_:]*)", "1", "$1$", "tireString");
		RegexExtractor tiresProdIDFullRegex = new RegexExtractor();
		creator.makeRegExExtractor(tiresProdIDFullRegex, "Full ProductID Extractor", "product_id\":\"([0-9]*)", "-1", "$1$", "prodID");
		RegexExtractor tiresSpeedRatingRegex = new RegexExtractor();
		creator.makeRegExExtractor(tiresSpeedRatingRegex, "Speed Rating Extractor", "speedRating\":\"([A-Z])", "1", "$1$", "speedRating");
		tiresSpeedRatingRegex.setScopeVariable("chassisString");
		RegexExtractor tiresVehicleSegmentRegex = new RegexExtractor();
		creator.makeRegExExtractor(tiresVehicleSegmentRegex, "Vehicle Segment Extractor", "vehicleSegment\":\"([A-Z\\s]*)\"", "1", "$1$", "vehicleSegment");
		
		

		BeanShellSampler tiresPassInBeanShell = new BeanShellSampler();//Constructs the POST body for recommendation using the variables retrieved.
		creator.makeBeanShellSampler(tiresPassInBeanShell, "try{"
				+	"String loops =  vars.get(\"prodID_matchNr\");\n"
				+	"int loopNum = Integer.parseInt(loops);\n"
				+	"String varString = \"\";\n"
				+	"for( int i = 1; i <= loopNum; i++) {\n"
				+		"	if(i == 1) {\n"
				+		"		varString = \"{\\\"frontOrBoth\\\":\\\"\" + vars.get(\"prodID_\" + String.valueOf(i)) + \"\\\",\\\"rear\\\":\\\"\\\"}\";"
				+		"	}\n"
				+		"	else{\n"
				+		"		varString = varString + \",{\\\"frontOrBoth\\\":\\\"\" + vars.get(\"prodID_\" + String.valueOf(i)) + \"\\\",\\\"rear\\\":\\\"\\\"}\";"
				+		"	}\n"
				+		"}\n"
				+		"vars.put(\"varString\", varString);\n"
				+		"}\n"
				+		"	catch(Throwable ex) {"
				+		"log.error(\"Error in Beanshell\", ex);\n"
				+		"		throw ex;\n"
				+		"	}", "Post_BeanShell_TiresPassIn");
		
		BeanShellSampler carFileReader = new BeanShellSampler();
		creator.makeBeanShellSampler(carFileReader, "import java.text.*;\n"
				+ "import java.io.*;\n"
				+ "import java.util.*;\n"
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
				+ "String fileName = \"C:/JMeter/apache-jmeter-3.2/bin/LoadTesting/carList.txt\";\n" //Change This Path!!!!++++++++++++++++++++++++++++++++++++++++++++
				+ "ArrayList strList = new ArrayList();\n"
				+ "try {\n"
				+	"	File file = new File(fileName);\n"
				+	"	BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));\n"
				+	"	int counter = 1;\n"
				+	"	Random rand = new Random();\n"
				+	"	int pick = rand.nextInt(48) + 1;\n"
				+	"	for ( int i = 0; i < pick; i++) {\n"
				+	"		reader.readLine();\n"
				+	"	}\n"
				+	"	String line = reader.readLine();\n"
				+	"	vars.put(\"carString\", line);\n"		
				+"}\n"
				+"catch(Throwable ex) {\n"
				+	"	log.error(\"error in Beanshell\", ex);\n"
				+	"	throw ex;\n"
				+"}", "Post_BeanShell_CarFileReader");
		
		
		
		LoopController selectLoop = new LoopController();
		creator.makeLoopController(selectLoop, "Select Loops", selectionPageLoops);
		
		TransactionController selectGroupController = new TransactionController();
		creator.makeTransactionController(selectGroupController, "Selection Group");
		TransactionController selectYearsController = new TransactionController();
		creator.makeTransactionController(selectYearsController, "03_Select_Year");
		TransactionController selectMakeModelController = new TransactionController();
		creator.makeTransactionController(selectMakeModelController, "04_Select_MakeModel");
		TransactionController selectTrimsController = new TransactionController();
		creator.makeTransactionController(selectTrimsController, "05_Select_Trims");
		
		ListedHashTree selectGroup = new ListedHashTree();
		HashTree selectLoopTree = selectGroup.add(selectLoop);
		HashTree selectCarFileTree = selectLoopTree.add(carFileReader);
		selectCarFileTree.add(makeRegex);
		selectCarFileTree.add(modelRegex);
		selectCarFileTree.add(trimsRegex);
		selectCarFileTree.add(yearRegex);
		HashTree selectYearsTimerTree = selectLoopTree.add(selectYearsController);
		HashTree selectYearsTree = selectYearsTimerTree.add(selectYears);
		selectYearsTree.add(selectMakeModelHM);
		selectYearsTree.add(selectMakeModelAssertion);
		selectYearsTimerTree.add(yearsPause);
		selectYearsTimerTree.add(yearsdynatraceHM);
		HashTree selectMakeModelTimerTree = selectLoopTree.add(selectMakeModelController);
		HashTree selectMakeModelTree = selectMakeModelTimerTree.add(selectMakeModel);
		selectMakeModelTree.add(selectTrimsHM);
		selectMakeModelTree.add(selectTrimsAssertion);
		selectMakeModelTree.add(vehicleFoundAssertion);
		selectMakeModelTree.add(vehicleIDRegex);
		selectMakeModelTree.add(assemblyStringRegex);
		selectMakeModelTree.add(trimsIDRegex);
		selectMakeModelTree.add(chassisStringRegex);
		selectMakeModelTree.add(chassisIDRegex);
		selectMakeModelTree.add(assemblyIDRegex);
		selectMakeModelTree.add(isStaggeredRegex);
		selectMakeModelTree.add(crossSectionRegex);
		selectMakeModelTree.add(aspectRatioRegex);
		selectMakeModelTree.add(diameterRegex);
		selectMakeModelTree.add(axleTypeRegex);
		selectMakeModelTree.add(tiresSpeedRatingRegex);
		selectMakeModelTimerTree.add(makeModelPause);
		selectMakeModelTimerTree.add(mmdynatraceHM);
		HashTree selectTrimsTimerTree = selectLoopTree.add(selectTrimsController);
		HashTree selectStoreTree = selectTrimsTimerTree.add(selectStore);
		selectStoreTree.add(selectStoreHM);
		selectStoreTree.add(selectStoreAssertion);
		HashTree selectMakeModel2Tree = selectTrimsTimerTree.add(selectYears2);
		selectMakeModel2Tree.add(selectMakeModelHM);
		selectMakeModel2Tree.add(selectMakeModelAssertion);
		HashTree selectTrims2Tree = selectTrimsTimerTree.add(selectMakeModel2);
		selectTrims2Tree.add(selectTrimsHM);
		selectTrims2Tree.add(selectTrimsAssertion);
		HashTree selectFontTree = selectTrimsTimerTree.add(selectFont);
		selectFontTree.add(selectFontHM);
		selectFontTree.add(selectFontSizeAssertion);
		HashTree selectTrimsTree = selectTrimsTimerTree.add(selectTrims);
		selectTrimsTree.add(selectTireSizesHM);
		selectTrimsTree.add(selectTireSizesAssertion);
		selectTrimsTree.add(tiresStringRegex);
		selectTrimsTree.add(tiresVehicleSegmentRegex);
		HashTree  selectTiresTree = selectTrimsTimerTree.add(selectTires);
		selectTiresTree.add(selectTiresHM);
		selectTiresTree.add(selectTiresAssertion);
		selectTiresTree.add(selectTiresNotAssertion);
		selectTiresTree.add(tiresProdIDFullRegex);
		selectTrimsTimerTree.add(trimsPause);
		selectTrimsTimerTree.add(trimdynatraceHM);
		selectLoopTree.add(tiresPassInBeanShell);
		
		
		
		return selectGroup;
	}
	
	public ListedHashTree constructRecommendationPage(){
		
		TestPlanCreator creator = new TestPlanCreator();
		
		HTTPSamplerProxy recommendProcess = new HTTPSamplerProxy();
		String[] body = {"", "{\"sessionId\":\"\",\"sourceSystem\":\"PDL\",\"applicationVersion\":\"1.0\",\"userAgent\":\"agent\",\"storeCode\":\"${storeID}\",\""
				+ "siteNumber\":${siteNumber},\"associate\":\"\",\"vehicle\":"
				+ "{\"vehicleId\":\"${vehicleID}\",\"trimId\":\"${trimID}\",\"assemblyId\":\"${assemblyID}\",\"vehicleSegment\":\"${vehicleSegment}\","
				+ "\"oeSpeedRating\":\"${speedRating}\"},\"drivingPriorities\":{\"frontOrBoth\":{\"oeSize\":\"Y\",\"crossSection\":\"${crossSection}\",\"aspectRatio\":\"${aspectRatio}\",\"rimSize\":\"${diameter}\",\"quantity\":\"4\"},\"rear\""
				+ ":{},\"milesDriven\":15000,\"drivingStyle\":\"typical\",\"zipCode\":\"${zipCode}\",\"priority1\":3,\"priority2\":1,\"priority3\":2,\"priority4\":4},\"articles\":[${varString}]}"};
		creator.makeHTTPRequest(recommendProcess, "6-1_Recommend_Process", "POST", "", body);
		recommendProcess.setPostBodyRaw(true);
		HTTPSamplerProxy recommendFont = new HTTPSamplerProxy();
		creator.makeHTTPRequest(recommendFont, "6-2_Recommend_Font", "GET", "", null);
		HTTPSamplerProxy recommendArrowPng = new HTTPSamplerProxy();
		creator.makeHTTPRequest(recommendArrowPng, "6-3_Recommend_ArrowPng", "GET", "", null);
		HTTPSamplerProxy recommendTopRecommendationPng = new HTTPSamplerProxy();
		creator.makeHTTPRequest(recommendTopRecommendationPng, "6-4_Recommend_TopPng", "GET", "", null);
		
		TestAction recommendPause = new TestAction();
		creator.makeTestAction(recommendPause, "Recommend Pause", "1500");
		
		ResponseAssertion recommendProcessAssertion = new ResponseAssertion();
		creator.makeResponseAssertion(recommendProcessAssertion, "Recommendation Process Assertion", false, "\"sessionId\"");
		creator.makeResponseAssertion(recommendProcessAssertion, "Recommendation Process Assertion 2", false, "\"products\"");
		SizeAssertion recommendFontAssertion = new SizeAssertion();
		creator.makeSizeAssertion(recommendFontAssertion, "Recommend Page Font Size Assertion", 18094);
		SizeAssertion recommendArrowAssertion = new SizeAssertion();
		creator.makeSizeAssertion(recommendArrowAssertion, "Recommend Arrow Png Size Assertion", 760);
		SizeAssertion recommendTopRecommendationAssertion = new SizeAssertion();
		creator.makeSizeAssertion(recommendTopRecommendationAssertion, "Top Recommendation Png Size Assertion", 3077);
		
		HeaderManager recommendProcessHM = new HeaderManager();
		String[] recommendHMDefaultNames = {"Referer", "Accept-Language", "Content-Type", "Accept-Encoding", "User-Agent", "Accept"};
		String[] recommendHMDefaultValues = {"", "en-US,en;q=0.5", "application/json;charset=utf-8", "gzip, deflate", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0", "application/json, text/plain, */*"};
		creator.makeHeaderManager(recommendProcessHM, "Recommendation Process Header Manager", recommendHMDefaultNames, recommendHMDefaultValues);
		HeaderManager recommendFontHM = new HeaderManager();
		String[] recommendFontNames = {"Referer", "Accept-Language", "Accept-Encoding", "User-Agent", "Accept"};
		String[] recommendFontValues = {"", "en-US,en;q=0.5", "identity", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0", "application/font-woff2;q=1.0,application/font-woff;q=0.9,*/*;q=0.8"};
		creator.makeHeaderManager(recommendFontHM, "Recommendation Font Header Manager", recommendFontNames, recommendFontValues);
		String[] dynatraceName = {"x-dynaTrace"};
		String[] dynatraceVal = {"NA=06_RecommendationPage_Jmeter"};
		HeaderManager dynatraceHM = new HeaderManager();
		creator.makeHeaderManager(dynatraceHM, "Recommend Dynatrace HM", dynatraceName, dynatraceVal);
		
		LoopController recommendLoopControl = new LoopController();
		creator.makeLoopController(recommendLoopControl, "Recommendation Loop Controller", recommendationPageLoops);
		
		TransactionController recommendController = new TransactionController();
		creator.makeTransactionController(recommendController, "06_Recommendation_Page");
		
		ListedHashTree recommendTree = new ListedHashTree();
		HashTree recommendLoopTree = recommendTree.add(recommendLoopControl);
		HashTree recommendGroupTree = recommendLoopTree.add(recommendController);
		HashTree recommendProcessTree = recommendGroupTree.add(recommendProcess);
		recommendProcessTree.add(recommendProcessHM);
		recommendProcessTree.add(recommendProcessAssertion);
		HashTree recommendArrowTree = recommendGroupTree.add(recommendArrowPng);
		recommendArrowTree.add(recommendArrowAssertion);
		HashTree recommendTopRecommendTree = recommendGroupTree.add(recommendTopRecommendationPng);
		recommendTopRecommendTree.add(recommendTopRecommendationAssertion);
		HashTree recommendFontTree = recommendGroupTree.add(recommendFont);
		recommendFontTree.add(recommendFontHM);
		recommendFontTree.add(recommendFontAssertion);
		recommendGroupTree.add(recommendPause);
		recommendGroupTree.add(dynatraceHM);
		
		return recommendTree;
	}
	
	public ListedHashTree constructTireDetails(){
		
		TestPlanCreator creator = new TestPlanCreator();
		
		HTTPSamplerProxy detailsBlueCar = new HTTPSamplerProxy();
		creator.makeHTTPRequest(detailsBlueCar, "7-1_BCar", "GET", "", null);
		HTTPSamplerProxy detailsStarRed = new HTTPSamplerProxy();
		creator.makeHTTPRequest(detailsStarRed, "7-2_RStar", "GET", "", null);
		HTTPSamplerProxy detailsStarGray = new HTTPSamplerProxy();
		creator.makeHTTPRequest(detailsStarGray, "7-3_GStar", "GET", "", null);
		HTTPSamplerProxy detailsYellowCar = new HTTPSamplerProxy();
		creator.makeHTTPRequest(detailsYellowCar, "7-4_YCar", "GET", "", null);
		HTTPSamplerProxy detailsBlueTrail = new HTTPSamplerProxy();
		creator.makeHTTPRequest(detailsBlueTrail, "7-5_BTrail", "GET", "", null);
		HTTPSamplerProxy detailsYellowTrail = new HTTPSamplerProxy();
		creator.makeHTTPRequest(detailsYellowTrail, "7-6_YTrail", "GET", "", null);
		HTTPSamplerProxy detailsTealTire = new HTTPSamplerProxy();
		creator.makeHTTPRequest(detailsTealTire, "7-7_TTire", "GET", "", null);
		HTTPSamplerProxy detailsGreenTire = new HTTPSamplerProxy();
		creator.makeHTTPRequest(detailsGreenTire, "7-8_GTire", "GET", "", null);
		HTTPSamplerProxy detailsAquaTire = new HTTPSamplerProxy();
		creator.makeHTTPRequest(detailsAquaTire, "7-9_ATire", "GET", "", null);
		
		SizeAssertion blueCarAssertion = new SizeAssertion();
		creator.makeSizeAssertion(blueCarAssertion, "Blue Car Size Assertion", 949);
		SizeAssertion starRedAssertion = new SizeAssertion();
		creator.makeSizeAssertion(starRedAssertion, "Star Red Size Assertion", 591);
		SizeAssertion starGrayAssertion = new SizeAssertion();
		creator.makeSizeAssertion(starGrayAssertion, "Star Gray Size Assertion", 595);
		SizeAssertion yellowCarAssertion = new SizeAssertion();
		creator.makeSizeAssertion(yellowCarAssertion, "Yellow Car Size Assertion", 970);
		SizeAssertion blueTrailAssertion = new SizeAssertion();
		creator.makeSizeAssertion(blueTrailAssertion, "Blue Trail Size Assertion", 7904);
		SizeAssertion yellowTrailAssertion = new SizeAssertion();
		creator.makeSizeAssertion(yellowTrailAssertion, "Yellow Trail Size Assertion", 7872);
		SizeAssertion tealTireAssertion = new SizeAssertion();
		creator.makeSizeAssertion(tealTireAssertion, "Teal Tire Size Assertion", 2201);
		SizeAssertion greenTireAssertion = new SizeAssertion();
		creator.makeSizeAssertion(greenTireAssertion, "Green Tire Size Assertion", 2279);
		SizeAssertion aquaTireAssertion = new SizeAssertion();
		creator.makeSizeAssertion(aquaTireAssertion, "Aqua Tire Size Assertion", 2262);
		
		LoopController detailsLoopController = new LoopController();
		creator.makeLoopController(detailsLoopController, "Details Loop Controller", detailsPageLoops);
		
		TransactionController detailsGroupController = new TransactionController();
		creator.makeTransactionController(detailsGroupController, "07_Details_Page");
		detailsGroupController.setGenerateParentSample(false);
		
		ListedHashTree detailsTree = new ListedHashTree();
		HashTree detailsLoopTree = detailsTree.add(detailsLoopController);
		HashTree detailsGroupTree = detailsLoopTree.add(detailsGroupController);
		HashTree blueCarTree = detailsGroupTree.add(detailsBlueCar);
		blueCarTree.add(blueCarAssertion);
		HashTree starRedTree = detailsGroupTree.add(detailsStarRed);
		starRedTree.add(starRedAssertion);
		HashTree starGrayTree = detailsGroupTree.add(detailsStarGray);
		starGrayTree.add(starGrayAssertion);
		HashTree yellowCarTree = detailsGroupTree.add(detailsYellowCar);
		yellowCarTree.add(yellowCarAssertion);
		HashTree blueTrailTree = detailsGroupTree.add(detailsBlueTrail);
		blueTrailTree.add(blueTrailAssertion);
		HashTree yellowTrailTree = detailsGroupTree.add(detailsYellowTrail);
		yellowTrailTree.add(yellowTrailAssertion);
		HashTree tealTireTree = detailsGroupTree.add(detailsTealTire);
		tealTireTree.add(tealTireAssertion);
		HashTree greenTireTree = detailsGroupTree.add(detailsGreenTire);
		greenTireTree.add(greenTireAssertion);
		HashTree aquaTireTree = detailsGroupTree.add(detailsAquaTire);
		aquaTireTree.add(aquaTireAssertion);
		
		
		return detailsTree;
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException{
		
		//Generating a random list of stores.
		String[] storesString = {};
				
		Random rand = new Random();
		//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		PrintWriter writer = new PrintWriter(jmeterPath + "\\LoadTesting\\randStoreID.txt", "UTF-8");//Change This Path!!!++++++++++++++++++++++++++++++++++++++++++++++
		writer.println("StoreID,ZipCodeLaunch");
		for(int i=0; i < storesString.length-1; i++){
			int j = rand.nextInt(storesString.length);
			writer.println(storesString[j]);
		}
		writer.close();
		
		//User Input
		Scanner reader = new Scanner(System.in);
		System.out.println("Home Loops: ");
		homePageLoops = reader.nextInt();
		System.out.println("Login Loops: ");
		loginPageLoops = reader.nextInt();
		System.out.println("Selection Loops: ");
		selectionPageLoops = reader.nextInt();
		System.out.println("Recommendation Loops: ");
		recommendationPageLoops = reader.nextInt();
		System.out.println("Details Loops: ");
		detailsPageLoops = reader.nextInt();
		System.out.println("User Loops: ");
		userLoops = reader.nextInt();
		System.out.println("Number of Users: ");
		numUsers = reader.nextInt();
		System.out.println("Ramp-up Time(in seconds): ");
		rampUpTime = reader.nextInt();
		System.out.println("Iteration Time(in seconds): ");
		targetRunTime = reader.nextInt();
		reader.close();
		
		
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		JMeterUtils.setJMeterHome("C:\\JMeter\\apache-jmeter-3.2");//Change This Path!!! Do Not Include /bin in the path!++++++++++++++++++++++++++++++++++++++++++++++
		JMeterUtils.loadJMeterProperties(jmeterPath + "\\jmeter.properties");
		JMeterUtils.initLocale();
		
		
		TestPlanCreator creator = new TestPlanCreator();
		
		//Create the Page Trees
		HashTree homeTree = creator.constructHomePage();
		HashTree loginTree = creator.constructLoginPage();
		HashTree selectTree = creator.constructSelectPage();
		HashTree recommendTree = creator.constructRecommendationPage();
		HashTree detailsTree = creator.constructTireDetails();
		
		//create the listener for grafana
		BackendListener grafana = new BackendListener();
		creator.makeBackendListener(grafana, "Grafana Backend","localhost", "2003", "[0-9]{2}.*");
		
		
		//create the timers
		BeanShellSampler startTime = new BeanShellSampler();
		creator.makeBeanShellSampler(startTime, "long startTime = System.nanoTime();\n"
				+ "String startTimeString = String.valueOf(startTime);\n"
				+ "vars.put(\"startTime\", startTimeString);", "Post_BeanShell_StartTimer");
		BeanShellSampler runTime = new BeanShellSampler();
		creator.makeBeanShellSampler(runTime, "import java.util.concurrent.TimeUnit;\n"
				+ "long endTime = System.nanoTime();\n"
				+ "endTime = endTime/1000000000;\n"
				+ "String startTimeString = vars.get(\"startTime\");\n"
				+ "long startTime = Long.parseLong(startTimeString);\n"
				+ "startTime = startTime/1000000000;\n"
				+ "long runTime = endTime - startTime;\n"
				+ "int runTimeInt = (int) runTime;\n"
				+ "int targetTime = " + targetRunTime + ";\n"
				+ "while (runTimeInt <= targetTime) {\n"
				+ "TimeUnit.SECONDS.sleep(1);\n"
				+ "runTimeInt += 1;\n"
				+ "}", "Post_BeanShell_EndTimer");
		
		//create the Hash tree to store everything
		ListedHashTree testPlanTree = new ListedHashTree();
		
		
		TestPlan testPlan = new TestPlan();
		creator.makeTestPlan(testPlan, "Test Plan");
		testPlan.setTearDownOnShutdown(true);
		LoopController threadLoops = new LoopController();
		creator.makeLoopController(threadLoops, "Thread Loops", userLoops);
		ThreadGroup users = new ThreadGroup();
		creator.makeThreadGroup(users, "Full Users", numUsers, rampUpTime, threadLoops);
		
		
		
		testPlanTree.add(testPlan);
		CSVDataSet storeVars = new CSVDataSet();
		//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		creator.makeCSVDataSet(storeVars, "StoreID Getter", "C:/JMeter/apache-jmeter-3.2/bin/LoadTesting/randStoreID.txt", true);//Change This Path!!!++++++++++++++++++
		testPlanTree.add(testPlan, storeVars);
		HashTree userGroup = testPlanTree.add(testPlan, users);
		
		
		TransactionController userGroup1Timer = new TransactionController();
		creator.makeTransactionController(userGroup1Timer, "00_Full_Run");
		HashTree userGroup1TimerTree = userGroup.add(userGroup1Timer);
		userGroup1TimerTree.add(startTime);
		userGroup1TimerTree.add(homeTree);	
		userGroup1TimerTree.add(loginTree);
		userGroup1TimerTree.add(selectTree);
		userGroup1TimerTree.add(recommendTree);
		userGroup1TimerTree.add(detailsTree);
		userGroup1TimerTree.add(runTime);
		userGroup.add(grafana);
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		SaveService.saveTree(testPlanTree, new FileOutputStream(jmeterPath + "\\LoadTesting\\LoadTest.jmx"));//Change This Path!!!+++++++++++++++++++++++++++++++++
		System.exit(0);
		
	}
}