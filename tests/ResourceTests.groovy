import java.nio.file.Path
import java.nio.file.Paths

import static groovy.test.GroovyAssert.shouldFail
import groovy.json.JsonOutput
import groovy.util.ConfigObject

import nextflow.util.ConfigHelper
import nextflow.util.MemoryUnit
import nextflow.util.SysHelper
import org.junit.Test

import validator.bl.NextflowConfigTests

class ResourceTests extends NextflowConfigTests {
    protected Path get_projectDir() {
        // Return the path to the "resource_test/" subfolder
        return Paths.get(
            getClass().protectionDomain.codeSource.location.path
        ).getParent().resolve("resource_test")
    }

    protected int override_cpus = 10
    protected MemoryUnit override_memory = 12.GB

    @Override
    protected def generate_config_text(configobj) {
        return """
        import nextflow.util.SysHelper
        import nextflow.util.MemoryUnit
        import static org.mockito.Mockito.*
        import org.mockito.MockedStatic

        // Mock out the SysHelper::getAvailCpus() and
        // SysHelper::getAvailMemory() methods

        try (MockedStatic dummyhelper = mockStatic(
                SysHelper.class,
                CALLS_REAL_METHODS)) {
            dummyhelper
                .when(SysHelper::getAvailCpus)
                .thenReturn(${override_cpus});
            dummyhelper
                .when(SysHelper::getAvailMemory)
                .thenReturn(new MemoryUnit(${override_memory.getBytes()}));

            includeConfig "\${projectDir}/../../config/resource_handler/resource_handler.config"

            ${ConfigHelper.toCanonicalString(configobj)}
            resource_handler.handle_resources(params.resource_file)
        }
        """
    }

    protected Map get_baseline_resource_allocations() {
        // These are modified from the resource allocation README
        return [
            default: [
                process1: [
                    cpus: [ min: 1, fraction: 0.51, max: 100 ],
                    memory: [ min: "1 MB", fraction: 0.5, max: "100 GB" ]
                ],
                process2: [
                    cpus: [ min: 1, fraction: 0.75, max: 100 ],
                    memory: [ min: "1 MB", fraction: 0.25, max: "100 GB" ]
                ],
                process3: [
                    cpus: [ min: 1, fraction: 0.75, max: 2 ],
                    memory: [ min: "1 MB", fraction: 0.5, max: "12 MB" ]
                ]
            ],
            custom_profile: [
                process1: [
                    cpus: [ min: 12, fraction: 0.25, max: 100 ],
                    memory: [ min: "5 GB", fraction: 1.0, max: "100 GB" ]
                ],
                process2: [
                    cpus: [ min: 12, fraction: 0.25, max: 20],
                    memory: [ min: "230 MB", fraction: 0.25, max: "250 MB" ]
                ],
                process3: [
                    cpus: [ min: 7, fraction: 0.75, max: 1000 ],
                    memory: [ min: "12 GB", fraction: 0.6, max: "120 GB" ]
                ]
            ]
        ]
    }

    protected String write_resource_json(Map resources) {
        File tempfile = testFolder.newFile("resources.json")
        tempfile.write(JsonOutput.prettyPrint(JsonOutput.toJson(resources)))
        return tempfile.toString()
    }

    protected def set_common_parameters(Map resources) {
        File tempfile = testFolder.newFile("resources.json")
        tempfile.write(JsonOutput.prettyPrint(JsonOutput.toJson(resources)))

        inconfig.params.resource_file = tempfile.toString()
        expected.params.resource_file = tempfile.toString()

        // These parameters are scraped from the current system
        expected.params.min_cpus = 1
        expected.params.max_cpus = override_cpus

        expected.params.min_memory = 1.MB
        expected.params.max_memory = override_memory

        expected.params.min_time = 1.s
        expected.params.max_time = 1000.d

        // Re-implement the logic from the resource handler to predict the values
        expected.params.base_allocations = [:]

        def profile = resources.get(
            inconfig.params.containsKey("resource_allocation_profile_tag")
            ? inconfig.params.resource_allocation_profile_tag
            : "default"
        )

        profile.each { process_name, process_info ->
            def allocations = [:]

            allocations.cpus = Math.min(
                Math.min(
                    Math.max(
                        (override_cpus * process_info.cpus.fraction).asType(Integer),
                        process_info.cpus.min
                    ),
                    process_info.cpus.max
                ),
                override_cpus
            )

            allocations.memory = MemoryUnit.of(Math.min(
                Math.min(
                    Math.max(
                        (override_memory.getBytes() * process_info.memory.fraction).asType(long),
                        MemoryUnit.of(process_info.memory.min).getBytes()
                    ),
                    MemoryUnit.of(process_info.memory.max).getBytes()
                ),
                override_memory.getBytes()
            ))

            expected.params.base_allocations[process_name] = allocations
        }

        expected.params.retry_information = [:]
    }

    // A helper method to compare that the values of any common keys between
    // the two maps are equal.
    def compare_common_keys(Map left, Map right) {
        left.keySet().intersect(right.keySet()).each { key ->
            if (left[key] instanceof Map) {
                assert right[key] instanceof Map
                compare_common_keys(left[key], right[key])
            } else {
                assert left[key] == right[key]
            }
        }
    }

    @Test
    void test_defaults() {
        def resources = get_baseline_resource_allocations()
        set_common_parameters(resources)

        // Sanity check - should get 50% of the memory in the default profile
        assert expected.params.base_allocations.process1.memory == 0.5 * override_memory

        compare()
    }

    @Test
    void test_custom_profile() {
        def resources = get_baseline_resource_allocations()
        inconfig.params.resource_allocation_profile_tag = "custom_profile"
        expected.params.resource_allocation_profile_tag = "custom_profile"
        set_common_parameters(resources)

        // Sanity check - should get 100% of the memory in the custom profile
        assert expected.params.base_allocations.process1.memory == override_memory

        compare()
    }

    @Test
    void test_modified_parameters() {
        def resources = get_baseline_resource_allocations()

        def meta_expected = new ConfigObject()

        // Tweak the system on which this is being evaluated
        override_cpus = 1000
        override_memory *= 2

        // Sanity check - process1 should get 50% of the memory
        meta_expected.process1.memory = 0.5 * override_memory

        // If we pin the min and max CPUs, that should determine exactly the number we get
        resources.default.process1.cpus.min = 34
        resources.default.process1.cpus.max = 34
        meta_expected.process1.cpus = 34

        // If the max CPUs are limiting, that determines the CPU limit
        resources.default.process2.cpus.min = 1
        resources.default.process2.cpus.max = 72
        meta_expected.process2.cpus = 72

        set_common_parameters(resources)

        compare_common_keys(expected.params.base_allocations, meta_expected)

        // Sanity-check the compare_common_keys function
        // Extraneous keys don't cause problems
        meta_expected.process7 = [:]
        meta_expected.process2.fakekey = 12
        compare_common_keys(expected.params.base_allocations, meta_expected)

        // Mismatched keys _must_ cause problems
        meta_expected.process2.cpus = 71
        shouldFail {
            compare_common_keys(expected.params.base_allocations, meta_expected)
        }

        compare()
    }
}
