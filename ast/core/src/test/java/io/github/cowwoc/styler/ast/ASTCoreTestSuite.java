package io.github.cowwoc.styler.ast;

import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.Arrays;
import java.util.List;

/**
 * Comprehensive test suite for AST Core module.
 * Organizes and executes all test categories for complete validation.
 */
public class ASTCoreTestSuite {
	/**
	 * Main entry point for running the complete test suite.
	 */
	public static void main(String[] args) {
		ASTCoreTestSuite suite = new ASTCoreTestSuite();

		if (args.length > 0 && "quick".equals(args[0])) {
			suite.runQuickValidation();
		} else {
			suite.runFullTestSuite();
		}
	}

	/**
	 * Runs the complete test suite with all categories.
	 * @throws SecurityException if security manager prevents exit
	 */
	public void runFullTestSuite() {
		System.out.println("=== Running AST Core Module Complete Test Suite ===");

		XmlSuite suite = new XmlSuite();
		suite.setName("AST Core Complete Test Suite");
		suite.setParallel(XmlSuite.ParallelMode.METHODS);
		suite.setThreadCount(4);

		// Core Infrastructure Tests
		XmlTest coreTest = createTest(suite, "Core Infrastructure Tests", Arrays.asList(
			BasicFunctionalityTest.class,
			VisitorPatternComplianceTest.class
		));

		// Run the suite
		TestNG testng = new TestNG();
		testng.setXmlSuites(Arrays.asList(suite));
		testng.run();

		// Print summary
		if (testng.hasFailure()) {
			System.err.println("❌ Test suite completed with failures");
			System.exit(1);
		} else {
			System.out.println("✅ All tests passed successfully");
		}
	}

	/**
	 * Runs a quick validation subset for development feedback.
	 * @throws SecurityException if security manager prevents exit
	 */
	public void runQuickValidation() {
		System.out.println("=== Running AST Core Module Quick Validation ===");

		XmlSuite suite = new XmlSuite();
		suite.setName("AST Core Quick Validation");
		suite.setParallel(XmlSuite.ParallelMode.CLASSES);
		suite.setThreadCount(2);

		// Quick validation focuses on core functionality
		XmlTest quickTest = createTest(suite, "Quick Validation", Arrays.asList(
			BasicFunctionalityTest.class,
			VisitorPatternComplianceTest.class
		));

		// Run the suite
		TestNG testng = new TestNG();
		testng.setXmlSuites(Arrays.asList(suite));
		testng.run();

		// Print summary
		if (testng.hasFailure()) {
			System.err.println("❌ Quick validation failed");
			System.exit(1);
		} else {
			System.out.println("✅ Quick validation passed");
		}
	}

	/**
	 * Creates a TestNG XML test configuration.
	 */
	private XmlTest createTest(XmlSuite suite, String testName, List<Class<?>> testClasses) {
		XmlTest test = new XmlTest(suite);
		test.setName(testName);

		List<XmlClass> xmlClasses = testClasses.stream()
			.map(clazz -> new XmlClass(clazz.getName()))
			.toList();

		test.setXmlClasses(xmlClasses);
		return test;
	}

	/**
	 * Programmatic test execution for CI/CD integration.
	 */
	public static class ProgrammaticRunner {
		/**
		 * Runs tests programmatically and returns success status.
		 * @return true if all tests pass, false otherwise
		 */
		public static boolean runAllTests() {
			TestNG testng = new TestNG();

			// Configure test classes
			testng.setTestClasses(new Class<?>[] {
			    BasicFunctionalityTest.class,
			    VisitorPatternComplianceTest.class
			});

			// Configure for CI environment
			testng.setVerbose(1);
			testng.setUseDefaultListeners(false);

			// Run tests
			testng.run();

			return !testng.hasFailure();
		}

		/**
		 * Runs core tests only for quick feedback.
		 * @return true if core tests pass, false otherwise
		 */
		public static boolean runCoreTests() {
			TestNG testng = new TestNG();

			testng.setTestClasses(new Class<?>[] {
			    BasicFunctionalityTest.class,
			    VisitorPatternComplianceTest.class
			});

			testng.setVerbose(0);
			testng.setUseDefaultListeners(false);

			testng.run();

			return !testng.hasFailure();
		}
	}
}
