# Methods

To use a common `methods` function:
- In the pipeline-specific `methods.config`, include the `common_methods.config` file **before** the `methods` namespace
- Use any of the available functions as needed

> **WARNING**: If the pipeline-specific `methods` namespace contains a function of the same name as one found in `common_methods.config`, one will override the other based on order of inclusion:
> - If `common_methods.config` is included *before* the pipeline-specific `methods` namespace, the pipeline-specific function will override the common function
> - If `common_methods.config` is included *after* the pipeline-specific `methods` namespace, the common function will override the pipeline-specific function

## Available functions
- `set_resources_allocation` - Function to load base allocations, detect node type, and node-specific allocations; generally should be called in the pipeline's `methods.set_up()` function
- `generate_registered_output_directory` - Function to generate properly formatted output paths for registered output; requires the following variables to be defined in the `params` namespace: `dataset_id`, `patient_id`, `sample_id`, `analyte`, `technology`, `reference_genome_version`.
    - Positional args:
        |position|name|type|required|default|description|
        |:--:|:--:|:--:|:--:|:--:|:--:|
        |1|`data_dir`|String|No|`"/hot/data"`|Path to registered data directory|

## Example

### Resource allocation
```Nextflow
includeConfig "/path/to/common_methods.config"
...
methods {
    ...
    set_up = {
        ...
        methods.set_resources_allocation()
    }
}
```

### Registered output
```Nextflow
includeConfig "/path/to/common_methods.config"
...
methods {
    ...
    set_output_dir = {
        ...
        params.base_output_dir = methods.generate_registered_output_directory()
    }
}
```
