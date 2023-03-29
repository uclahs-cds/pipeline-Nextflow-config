# BAM Parser

To parser a BAM header:
- In `methods.config`, include the `bam_parser.config` file and call `bam_parser.parse_bam_header(</path/to/bam>)`

The function returns a `Map`, with the following information:
- `read_group`: A list of `Maps`, where each `Map` contains one parsed read group from the BAM header.

## Available functions
- `parse_bam_header` - Function for parsing a BAM header
    - Positional args:
        |position|name|type|required|default|description|
        |:--:|:--:|:--:|:--:|:--:|:--:|
        |1|`bam_path`|String|Yes|none|String representing path to BAM file for parsing|

## Example

To load a BAM header from a file defined at `params.bam_file_path`:
```Nextflow
includeConfig "/path/to/bam_parser.config"
...
methods {
    ...
    bam_header = bam_parser.parse_bam_header(params.bam_file_path)
}
```
