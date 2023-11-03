import java.nio.file.Path
import java.nio.file.Paths
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail

import nextflow.util.ConfigHelper

import validator.bl.NextflowConfigTests

// Test must begin with a capital letter (ExampleTests, not exampletests) and
// reside in a file of the same name (ExampleTests.groovy).
// New test classes must also be added to the list in the suite.groovy file.
class ExampleTests extends NextflowConfigTests {

    // This function should return the Path to the repository's root. This
    // implementation will work for any file located in <root>/tests/
    protected Path get_projectDir() {
        return Paths.get(
            getClass().protectionDomain.codeSource.location.path
        ).getParent().getParent()
    }

    // This function should return the text of a config file that will include
    // everything in the given ConfigObject and execute the config function to be
    // tested
    @Override
    protected def generate_config_text(configobj) {
        return """
        includeConfig "\${projectDir}/config/methods/common_methods.config"

        ${ConfigHelper.toCanonicalString(configobj)}

        methods.set_env()
        """
    }

    // All functions decorated with @Test are considered tests. A class can
    // contain multiple test functions.
    @Test
    void arbitrary_name() {
        // A test function can contain one or more `assert` statements.
        assert true

        // Statements expected to fail should be wrapped with `shouldFail`
        shouldFail {
            assert false
        }

        // NextflowConfigTests provides a few useful helpers:
        // testFolder is a TemporaryFolder that will be cleaned up after the
        // test.
        def csvfile = testFolder.newFile("data.csv")
        csvfile.withWriter('UTF-8') {
            it.writeLine "column_one,column_two,three"
            it.writeLine "alpha,beta,gamma"
            it.writeLine "delta,epsilon,eta"
        }
        inconfig.params.input_csv = csvfile.toString()
        // We also have to set the parameter on expected
        expected.params.input_csv = csvfile.toString()

        // This will set an environment variable, exactly as if you called
        // "MY_ENVVAR=value nextflow ..."
        envvars.set("MY_ENVVAR", "value")

        // NextflowConfigTests.compare() is a convenience function that calls
        // generate_config_text() on `inconfig` and then asserts the equality
        // of the resolved parameters and `expected`.

        // WARNING: compare() can only be called ONCE within a test function.
        // Nextflow internally caches the results of any calls to
        // includeConfig, so repeated calls to compare() will not produce the
        // expected results.

        def value = testFolder.getRoot().toString()
        inconfig.params.work_dir = value
        expected.params.work_dir = value
        compare()
    }
}
