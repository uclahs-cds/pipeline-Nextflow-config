# Methods

To use a common `align_methods` function:
- In the pipeline-specific `methods.config`, include the `align_methods.config`
- Use any of the available functions as needed

## Available functions
- `set_params_from_input` - Function to extract parameters from a CSV input and format it into a YAML-like map; generally should be called in the pipeline's `methods.set_up()` function; should be called before any parameter validation

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
        schema.load_custom_types("${projectDir}/external/nextflow-config/config/align_methods/custom_schema_types.config")
        schema.validate()
    }
}
```

## References
