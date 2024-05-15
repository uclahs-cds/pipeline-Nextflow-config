package resource_handler

import groovy.json.JsonSlurper

class PipelineAllocation {
    private SystemResources system_resources
    private File resource_json
    private String allocation_profile
    private Map processed_resources = [:]
    private Map retry_configurations = [:]
    private Map raw_process_resources = [:]

    PipelineAllocation(Object resource_json, Object params) {
        this.system_resources = new SystemResources(params)
        this.resource_json = new File(resource_json)

        def json_slurper = new JsonSlurper()

        // TO-DO: Dump original for logging, keeping in class for now
        this.raw_process_resources = json_slurper.parse(this.resource_json)

        this.select_allocation_profile(params)

        assert this.raw_process_resources instanceof Map

        // TO-DO: Validate JSON is in expected format

        // Separate the retry strategies from the base allocations
        this.processed_resources.each { process, allocations ->
            if (allocations.containsKey("retry_strategy")) {
                this.retry_configurations[process] = allocations["retry_strategy"]
                allocations.remove("retry_strategy")
            }
        }

        // Convert string memory units to memory unit
        this.processed_resources.each { process, allocations ->
            for (resource_type in ["cpus", "memory", "time"]) {
                if (allocations.containsKey(resource_type)) {
                    allocations[resource_type] = this.system_resources.resolve_resource_allocation(allocations[resource_type], resource_type)
                }
            }
        }

        // TO-DO: Singularize processes that may be separated with `|`
    }

    SystemResources get_system_resources() {
        return this.system_resources
    }

    private void load_resource_profile(String profile_tag) {
        this.processed_resources = this.raw_process_resources[profile_tag]

        if (!this.processed_resources) {
            throw new Exception("   ### ERROR ###   Failed to find requested resource profile: `${profile_tag}`")
        }
    }

    private void select_allocation_profile(Map params) {
        String profile_tag = null

        // Try for user-given profile
        if (params.containsKey('resource_allocation_profile_tag') && params.resource_allocation_profile_tag) {
            profile_tag = params.resource_allocation_profile_tag
            this.load_resource_profile(profile_tag)
            return
        }

        // Try loading detected tag based on system resources
        profile_tag = this.system_resources.get_resource_profile_tag()

        if (profile_tag) {
            try {
                this.load_resource_profile(profile_tag)
            } catch (all) {
                // Continue to try loading `default` profile
            }
        }

        // Resort to loading `default` profile
        this.load_resource_profile('default')
    }

    // TO-DO: functionality to dump original loaded JSON to file for logging

    void update_base_allocation(String resource, String process, Object multiplier) {
        if (this.processed_resources.containsKey(process) && this.processed_resources[process].containsKey(resource)) {
            this.processed_resources[process][resource] = this.system_resources.check_limits(this.processed_resources[process][resource] * multiplier, resource)
        } else {
            System.out.println("   ### WARNING ### No base value found for resource `${resource}` for process `${process}`. Update will be skipped.")
        }
    }

    // Apply base resource updates
    void apply_base_updates(Map resource_updates) {
        resource_updates.each { resource, updates ->
            updates.each { processes, multiplier ->
                List processes_to_update = (processes instanceof String || processes instanceof GString) ? [processes] : processes

                if (processes_to_update == []) {
                    processes_to_update = this.processed_resources.keySet() as List
                }

                processes_to_update.each { process ->
                    this.update_base_allocation(resource, process, multiplier)
                }
            }
        }
    }

    Map get_base_resource_allocations() {
        return this.processed_resources
    }

    Map get_retry_configuration() {
        return this.retry_configurations
    }

    void print_resources() {
        System.out.println(this.processed_resources)
    }
}
