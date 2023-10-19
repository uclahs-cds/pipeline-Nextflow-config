package validator.bl

import org.junit.Before
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.junit.rules.TemporaryFolder

import groovy.util.ConfigObject
import java.nio.file.Path

import validator.bl.BLConfigBuilder


abstract class NextflowConfigTests {
    protected BLConfigBuilder builder
    protected ConfigObject inconfig, expected

    @Rule
    public final EnvironmentVariables envvars = new EnvironmentVariables()

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder()

    @Before
    public void setup() {
        this.builder = new BLConfigBuilder()
        this.builder.setBaseDir(get_projectDir())

        this.inconfig = new ConfigObject()
        this.expected = new ConfigObject()
    }

    abstract protected Path get_projectDir()

    abstract protected def generate_config_text(configobj)

    protected void compare() {
        def actual = builder.resolve_config(generate_config_text(inconfig))

        // Copy the `params` space out into separate objects - without doing
        // this first, Groovy's power assert will print _everything_ (`schema`,
        // `methods`, etc.) on failure
        def actual_params = actual.params
        def expected_params = expected.params

        // Get rid of the `ucla_cds` parameter from the actual parameters, if
        // it is empty.
        if (!actual_params.isSet("ucla_cds")) {
            actual_params.remove("ucla_cds")
        }

        assert actual_params == expected_params
    }
}
