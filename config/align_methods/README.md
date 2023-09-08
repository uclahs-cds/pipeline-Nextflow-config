# Methods

To use a common `align_methods` function:
- In the pipeline-specific `methods.config`, include the `align_methods.config`
- Use any of the available functions as needed

## Available functions
<<<<<<< HEAD
- `set_params_from_input` - Function to extract parameters from a CSV input and format it into a YAML-like list of maps with each map corresponding to one FASTQ file/pair; the inputs are loaded under `params.input.FASTQ`; generally should be called in the pipeline's `methods.set_up()` function; should be called before any parameter validation
=======
- `set_params_from_input` - Function to extract parameters from a CSV input and format it into a YAML-like map; generally should be called in the pipeline's `methods.set_up()` function; should be called before any parameter validation
>>>>>>> 96ae992 (add a readme for align methods)

## Example

### Set Params from Input
```Nextflow
includeConfig "/path/to/align_methods.config"
...
methods {
    ...
    set_up = {
        ...
        align_methods.set_params_from_input()
<<<<<<< HEAD
        schema.load_custom_types("/path/to/custom_schema_types.config")
=======
        schema.load_custom_types("${projectDir}/external/nextflow-config/config/align_methods/custom_schema_types.config")
>>>>>>> 96ae992 (add a readme for align methods)
        schema.validate()
    }
}
```

<<<<<<< HEAD
## Custom schema types
- `InputNamespace`: Used for align-DNA and -RNA inputs; Check that input is namespace of expected types
- `ListFASTQPairs`: Used for input FASTQ list; Check that input is list of namespace of expected types

=======
>>>>>>> 96ae992 (add a readme for align methods)
## References
