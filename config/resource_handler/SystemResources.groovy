package resource_handler

import nextflow.util.SysHelper

class SystemResources {
    private Map resource_limits = [
        'cpus': [
            'type': java.lang.Integer,
            'min': 1,
            'max': SysHelper.getAvailCpus()
        ],
        'memory': [
            'type': nextflow.util.MemoryUnit,
            'min': 1.MB,
            'max': SysHelper.getAvailMemory()
        ],
        'time': [
            'type': nextflow.util.Duration,
            'min': 1.s,
            'max': 1000.d
        ]
    ]
    private String resource_profile = null

    SystemResources(Map params) {
        this.resource_limits.each { resource, resource_info ->
            ['min', 'max'].each { limit_end ->
                try {
                    if (params.containsKey("${limit_end}_${resource}" as String)) {
                        this.resource_limits[resource][limit_end] = params["${limit_end}_${resource}" as String].asType(his.resource_limits[resource]['type'])
                    }
                } catch (all) {
                    // Do nothing, let default value defined above take effect
                }
            }
        }

        this.identify_resource_profile()
    }

    Map get_limits(String type) {
        return [
            ("min_${type}" as String): this.resource_limits[type]['min'],
            ("max_${type}" as String): this.resource_limits[type]['max']
        ]
    }

    Object check_limits(Object obj, String type) {
        return SystemResources.check_limits(obj, type, this.resource_limits[type]['min'], this.resource_limits[type]['max'])
    }

    static Object check_limits(Object obj, String type, Object min, Object max) {
        if (obj.compareTo(max) == 1) {
            return max
        } else if (obj.compareTo(min) == -1) {
            return min
        } else {
            return obj
        }
    }

    private void identify_resource_profile() {
         // Identify if available resources match F- or M-series nodes
         def cpus = this.resource_limits.cpus.max
         def memory = this.resource_limits.memory.max.toGiga()

         if (cpus >= (cpus * 2 * 0.9 - 1) && (cpus <= (cpus * 2))) {
             this.resource_profile = "f${cpus}"
         }

         if (cpus >= (cpus * 16 * 0.9 - 1) && (cpus <= (cpus * 16))) {
             this.resource_profile = "m${cpus}"
         }
    }

    Object resolve_resource_allocation(Map allocation, String type) {
        def min_raw = allocation['min'].asType(this.resource_limits[type]['type'])
        def max_raw = allocation['max'].asType(this.resource_limits[type]['type'])

        def min_allocation = this.check_limits(min_raw, type)
        def max_allocation = this.check_limits(max_raw, type)

        def requested_allocation = allocation.fraction * (this.resource_limits[type]['max'])

        return SystemResources.check_limits(requested_allocation, type, min_allocation, max_allocation)
    }

    String get_resource_profile_tag() {
        return this.resource_profile
    }
}
