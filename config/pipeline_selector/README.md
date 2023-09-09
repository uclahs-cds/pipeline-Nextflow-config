# Pipeline Selector

To use the pipeline selector:
- Define a `Map` of dependencies with each key being a pipeline and the value being a list of pipelines the key depends on
- Define a list of requested pipelines
- Include the `pipeline_selector.config` file and call `pipeline_selector.get_pipelines(<dependencies>, <requested-pipelines>)`

The function returns a `List` of all pipelines that need to run to satisfy each requested pipeline's dependencies.

## Available functions
- `get_pipelines` - Function for resolving dependencies and identifying all pipelines that need to be run
    - Positional args:
        |position|name|type|required|default|description|
        |:--:|:--:|:--:|:--:|:--:|:--:|
        |1|`pipeline_dependencies`|Map|Yes|none|`Map` defining the dependencies for each pipeline|
        |2|`requested_pipelines`|List|No|`[]`|List of requested pipelines for which to resolve dependencies|

## Example

To resolve dependencies for a list of pipelines `params.requested_pipelines` using dependencies `pipeline_dependencies`:
```Nextflow
includConfig "/path/to/pipeline_selector.config"
...
methods {
    ...
    Map pipeline_dependencies = [
        'pipeline-1': ['pipeline-2'],
        'pipeline-2': ['pipeline-3', 'pipeline-4']
    ]
    List pipelines_to_run = pipeline_selector.get_pipelines(pipeline_dependencies, params.requested_pipelines)
}
```
