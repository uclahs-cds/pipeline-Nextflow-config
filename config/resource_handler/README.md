# Resource handler

This module handles resource allocations including granular per-process allocations, base allocation modifications, and retry setup. It makes use of the [retry module](../retry) module for retry setup.

The resource handler requires a JSON file containing allocation information, in the following format:

```JSON
{
    "allocation_group": {
        "process1": {
            "cpus": {
                "min": 1,
                "fraction": 0.5,
                "max": 1
            },
            "memory": {
                "min": "150 MB",
                "fraction": 0.23,
                "max": "1 GB"
            },
            "retry_strategy": {
                "memory": {
                    "strategy": "exponential",
                    "operand": 2
                }
            }
        },
        "process2": {
            "cpus": {
                "min": 2,
                "fraction": 0.5,
                "max": 2
            },
            "memory": {
                "min": "1500 MB",
                "fraction": 0.23,
                "max": "10 GB"
            }
        }
    },
    "default": {
        "process1": {
            "cpus": {
                "min": 1,
                "fraction": 0.5,
                "max": 1
            },
            "memory": {
                "min": "150 MB",
                "fraction": 0.23,
                "max": "1 GB"
            }
        },
        "process2": {
            "cpus": {
                "min": 2,
                "fraction": 0.5,
                "max": 2
            },
            "memory": {
                "min": "1500 MB",
                "fraction": 0.23,
                "max": "10 GB"
            }
        }
    }
}
```

A resource group with the label `default` is required in the JSON. Additionally, groups labeled as F-series and M-series allocations can be provided and will be automatically detected. For example, an F16 node will be recognized and the corresponding group label `f16` will automatically be loaded if defined in the resources JSON. A specific group can also be selected through configuration with the parameter `resource_allocation_profile_tag`.

## Example
An example of integrating the handler and setting up allocations:
```Nextflow
methods {
    ...
    setup = {
        ...
        resource_handler.handle_resources("${projectDir}/config/resources.json")
    }
}
```