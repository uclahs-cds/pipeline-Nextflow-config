import java.nio.file.Path
import java.nio.file.Paths

import static groovy.test.GroovyAssert.shouldFail

import nextflow.util.ConfigHelper
import org.junit.Test

import validator.bl.NextflowConfigTests

class BamParserTests extends NextflowConfigTests {
    protected Path get_projectDir() {
        return Paths.get(
            getClass().protectionDomain.codeSource.location.path
        ).getParent().getParent()
    }

    @Override
    protected def generate_config_text(configobj) {
        return """
        includeConfig "\${projectDir}/config/bam/bam_parser.config"

        ${ConfigHelper.toCanonicalString(configobj)}

        params.header = bam_parser.parse_bam_header(params.bam_file)
        """
    }

    @Test
    void parse_missing_read_group() {
        def bam_path = builder.get_launchDir().resolve("data/toy.bam").toString()
        inconfig.params.bam_file = bam_path
        expected.params.bam_file = bam_path
        expected.params.header = [read_group: []]

        compare()
    }

    @Test
    void parse_read_group() {
        def bam_path = builder.get_launchDir().resolve("data/toy-rg.bam").toString()
        inconfig.params.bam_file = bam_path
        expected.params.bam_file = bam_path
        expected.params.header = [
            read_group: [
                [LB: 'lib1', ID: '4', PL: 'illumina', SM: '20', PU: 'unit1'],
            ]
        ]

        compare()
    }
}
