import java.nio.file.Path
import java.nio.file.Paths

import static groovy.test.GroovyAssert.shouldFail

import nextflow.util.ConfigHelper
import org.junit.Test

import validator.bl.NextflowConfigTests

class AlignMethodsTests extends NextflowConfigTests {
    protected Path get_projectDir() {
        return Paths.get(
            getClass().protectionDomain.codeSource.location.path
        ).getParent().getParent()
    }

    @Override
    protected def generate_config_text(configobj) {
        return """
        includeConfig "\${projectDir}/config/align_methods/align_methods.config"
        includeConfig "\${projectDir}/config/csv/csv_parser.config"

        ${ConfigHelper.toCanonicalString(configobj)}

        align_methods.set_params_from_input()
        """
    }

    @Test
    void empty_throws_error() {
        // Without `params.input_csv`, we expect an exception
        shouldFail {
            compare()
        }
    }

    @Test
    void input_does_nothing() {
        // Nothing is done if `params.input` exists
        inconfig.params.input = null
        expected.params.input = null
        compare()
    }

    @Test
    void fake_csv() {
        def csvfile = testFolder.newFile("data.csv")
        csvfile.withWriter('UTF-8') {
            it.writeLine "column_one,column_two,three"
            it.writeLine "alpha,beta,gamma"
            it.writeLine "delta,epsilon,eta"
        }

        inconfig.params.input_csv = csvfile.toString()

        expected.params.input_csv = csvfile.toString()
        expected.params.input = [
            FASTQ: [
                [column_one: 'alpha', column_two: 'beta', three: 'gamma'],
                [column_one: 'delta', column_two: 'epsilon', three: 'eta'],
            ] as Set
        ]

        compare()
    }

}
