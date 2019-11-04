package com.openxsl.config.test;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

public class MyTestExecutionLister implements TestExecutionListener {

	@Override
	public void beforeTestClass(TestContext testContext) throws Exception {
		testContext.getApplicationContext();  //applicationContext
		testContext.getTestClass();     //class
		testContext.getTestInstance();  //object
		testContext.getTestMethod();    //Method
	}

	@Override
	public void prepareTestInstance(TestContext testContext) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeTestMethod(TestContext testContext) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterTestMethod(TestContext testContext) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterTestClass(TestContext testContext) throws Exception {
		// TODO Auto-generated method stub

	}

}
