package validator.bl

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

import nextflow.config.ConfigBuilder

class BLConfigBuilder extends ConfigBuilder {

    def get_launchDir() {
        return Paths.get('.').toRealPath()
    }

    def resolve_config(config_text) {
        def param_file = Files.createTempFile(null, ".config")
        Files.write(param_file, config_text.getBytes(StandardCharsets.UTF_8))

        return this.setShowClosures(false)
            .showMissingVariables(true)
            .setUserConfigFiles([param_file])
            .buildConfigObject()
    }
}
