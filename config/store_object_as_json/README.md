# Storage of map-like objects into JSON 

This module takes an arbritrary map-like object and a `File`, and converts the object into JSON and saves it into
the path-specified by `File`.

## Example

Shown is an example to store the `params` object into as a JSON file into the log folder.

Example:
```Nextflow
includeConfig "/path/to/store_object_as_json.config"
...
methods {
    ...
    setup = {
        ...
        json_extractor.store_object_as_json(
            params, // object to be stored
            new File("${params.log_output_dir}/params.json")
        )
    }
}
```
