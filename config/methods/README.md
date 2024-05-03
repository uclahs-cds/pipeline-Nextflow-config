# Methods

To use a common `methods` function:
- In the pipeline-specific `methods.config`, include the `common_methods.config` file **before** the `methods` namespace
- Use any of the available functions as needed

> **WARNING**: If the pipeline-specific `methods` namespace contains a function of the same name as one found in `common_methods.config`, one will override the other based on order of inclusion:
> - If `common_methods.config` is included *before* the pipeline-specific `methods` namespace, the pipeline-specific function will override the common function
> - If `common_methods.config` is included *after* the pipeline-specific `methods` namespace, the common function will override the pipeline-specific function

## Available functions
- `set_resources_allocation` - Function to load base allocations, detect node type, and node-specific allocations; generally should be called in the pipeline's `methods.set_up()` function
- `generate_uuid` - Function to generate a UUID
- `generate_registered_output_directory` - Function to generate properly formatted output paths for registered output; requires the following variables to be defined in the `params` namespace: `dataset_id`, `patient_id`, `sample_id`, `ucla_cds_analyte`, `ucla_cds_technology`, `ucla_cds_reference_genome_version`.
    - Positional args:
        |position|name|type|required|default|description|
        |:--:|:--:|:--:|:--:|:--:|:--:|
        |1|`data_dir`|String|No|`"/hot/data"`|Path to registered data directory|
- `get_genome_version` - Function to extract the reference genome version from a UCLA CDS reference path
    - Positional args:
        |position|name|type|required|default|description|
        |:--:|:--:|:--:|:--:|:--:|:--:|
        |1|`genome_path`|String|No|-|Path from which to extract the reference genome version|
- `load_publish_dirs` - Function to load a config file containing publishDir rules for processes
    - Positional args:
        |position|name|type|required|default|description|
        |:--:|:--:|:--:|:--:|:--:|:--:|
        |1|`file_path`|String|No|`${projectDir}/config/module.config`|Path to config file containing publishDir rules|
- `merge_publish_dirs` - Function to merge the publishDir rules between process-level and process-specific rules
    > For the `publishDir` rules, use the `disable_common_rules` option within each process where the common process-level rules *should not* be used. Ex. use this option to disable the common log file rule and use a custom one when needed by a process.
- `update_base_resource_allocation` - Function to update the base resource allocation for any number of processes
    - Positional args:
        |position|name|type|required|default|description|
        |:--:|:--:|:--:|:--:|:--:|:--:|
        |1|`resource`|String|Yes|-|Resource (`cpus`, `memory`) to update|
        |2|`multiplier`|Float|Yes|-|Float to multiply existing allocation value by for update|
        |3|`processes`|List|No|`[]`|Optional list of processes to apply update to. With default or empty list, all processes will be update|
- `set_env` - Function to set the workDir depending on parameters and on Slurm job ID
- `setup_docker_cpus` - Function to use allocated CPUs for a process to specify number of CPUs rather than CPU shares with Docker
- `get_absolute_path` - Function to resolve a path relative to the currently-loading configuration file into an absolute path
- `sanitize_uclahs_cds_id` - Pass input string to sanitize, i.e. keeping only alphanumeric, `-`, `/`, and `.` characters and replacing `_` with `-`

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

### UUID
```Nextflow
methods {
    ...
    params.bwa_mem2_uuid = methods.generate_uuid()
    ...
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
        params.output_dir_base = methods.generate_registered_output_directory()
    }
}
```

### Get genome version
```Nextflow
includeConfig "/path/to/common_methods.config"
...
methods {
    ...
    genome_version = methods.get_genome_version("/hot/ref/reference/GRCh38-BI-20160721/Homo_sapiens_assembly38.fasta")
}
```

### Load and merge `publishDir` rules
```Nextflow
includeConfig "/path/to/common_methods.config"
...
methods {
    ...
    methods.load_publish_dirs()
    methods.merge_publish_dirs()
}
```

### Update base resource allocation
```Nextflow
includeConfig "/path/to/common_methods.config"
...
methods {
    ...
    methods.update_base_resource_allocation('memory', params.memory_multiplier) // Updates memory for all processes
    methods.update_base_resource_allocation('memory', params.memory_multiplier, ['process_1', 'process_3']) // Updates memory for the given list of processes
}
```

### Set env
```Nextflow
includeConfig "/path/to/common_methods.config"
...
methods {
    ...
    methods.set_env()
}
```

### Set Docker to use CPU number instead of CPU share
```Nextflow
includeConfig "/path/to/common_methods.config"
...
methods {
    ...
    methods.setup_docker_cpus()
}
```

### Resolve a path relative to the currently-loading configuration file into an absolute path

This is not just a replacement for `${projectDir}`, which is set to the directory containing the entrypoint NextFlow script. This function allows you to use paths relative to the config file, which could be located in an entirely different area.

```Nextflow
includeConfig "/path/to/common_methods.config"
...
methods {
    ...
    output_dir = methods.get_absolute_path('../local_outputs')
}
```
### Sanitize uclahs-cds ID
```Nextflow
includeConfig "/path/to/common_methods.config"
...
methods {
    ...
    new_sm_tag = methods.sanitize_uclahs_cds_id(sm_tags[0])
}
```

### Capture process outputs, even after failures
`methods.setup_process_afterscript` is a drop-in replacement for this method of capturing process log files:

```Nextflow
// Old technique
process xxx {
    publishDir path: "${params.log_output_dir}/process-log",
        pattern: ".command.*",
        mode: "copy",
        saveAs: { "${task.process.replace(':', '/')}-${sample_id}/log${file(it).getName()}" }

    output:
    path(".command.*")
```

The above method does not produce any output files if the process fails. In order to always capture the log files, use the following:

```Nextflow
includeConfig "/path/to/common_methods.config"
...
methods {
    ...
    methods.setup_process_afterscript()
}
```

> [!NOTE]
> `methods.setup_process_afterscript()` duplicates the effect of defining `publishDir` and `output: path(".command.*")` for every process. Although there is no harm in using both techniques simultaneously, for clarity it is recommended to only use one.

The output path is controlled by the following two [custom process directives](https://www.nextflow.io/docs/latest/process.html#ext):

| Name | Default Value |
| --- | --- |
| `process.ext.log_dir` | `task.process.replace(':', '/')` |
| `process.ext.log_dir_suffix` | `''` |

Each of the above can be overridden by individual processes.

The final directory path is `${params.log_output_dir}/process-log/${task.ext.log_dir}${task.ext.log_dir_suffix}`.

#### Disabling for individual processes

```Nextflow
process xxx {

    ext capture_logs: false
```

#### Combining with `afterScript`

> [!IMPORTANT]
> This method sets `process.afterScript`. If you define another `afterScript` (either globally or for a specific process), you must use this technique to combine both scripts.

The `afterScript` closure established by `methods.setup_process_afterscript()` is also stored as `process.ext.commonAfterScript`. If a process requires another `afterScript`, it should be combined with the one defined here like so:

```Nextflow
process xxx {
    afterScript {
        [
            "echo 'Before the common script'",
            task.ext.commonAfterScript ?: "",
            "echo 'After the common script'"
        ].join("\n")
    }
```

Due to the [Elvis operator](https://groovy-lang.org/operators.html#_elvis_operator), the above snippet is safe to use even if `methods.setup_process_afterscript()` is not used.


## References
1. `nf-core` - https://nf-co.re/
2. `nf-code modules` - https://github.com/nf-core/sarek/blob/ad2b34f39fead34d7a09051e67506229e827e892/conf/modules.config
