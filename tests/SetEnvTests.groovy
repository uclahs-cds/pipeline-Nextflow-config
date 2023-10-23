import java.nio.file.Path
import java.nio.file.Paths

import nextflow.util.ConfigHelper
import org.junit.Test

import validator.bl.NextflowConfigTests

class SetEnvTests extends NextflowConfigTests {
    protected Path get_projectDir() {
        return Paths.get(
            getClass().protectionDomain.codeSource.location.path
        ).getParent().getParent()
    }

    @Override
    protected def generate_config_text(configobj) {
        return """
        includeConfig "\${projectDir}/config/methods/common_methods.config"

        ${ConfigHelper.toCanonicalString(configobj)}

        methods.set_env()
        """
    }

    @Test
    void default_set_env() {
        // With no further parameters, set_env() should set the result to $launchDir/work
        expected.params.work_dir = builder.get_launchDir().resolve("work")
        compare()
    }

   @Test
   void envvar_set_env() {
        def value = testFolder.getRoot().toString()
        envvars.set("NXF_WORK", value)
        expected.params.work_dir = value
        compare()
    }

    @Test
    void params_set_env() {
        def value = testFolder.getRoot().toString()
        inconfig.params.work_dir = value
        expected.params.work_dir = value
        compare()
    }

    @Test
    void standardized_set_env() {
        def value = testFolder.getRoot().toString()

        inconfig.params.ucla_cds = true
        inconfig.params.work_dir = value

        expected.params.ucla_cds = true
        expected.params.work_dir = value

        compare()
    }

    @Test
    void standard_default_set_env() {
        def job_id = "1234321"
        envvars.set("SLURM_JOB_ID", job_id)

        inconfig.params.ucla_cds = true
        expected.params.ucla_cds = true
        expected.params.work_dir = "/scratch/${job_id}"

        compare()
    }
}
