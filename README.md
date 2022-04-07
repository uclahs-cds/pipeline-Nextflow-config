# Nextflow Configs


- [Overview](#overview)
- [How to use](#how-to-use)
- [Available Configs](#available-configs)
    - [Retry](#retry)
- [License](#License)


## Overview

A set of Nextflow configs commonly used across pipelines.

## How to use

1. Add this repository as a submodule in the pipeline of interest
2. Include the required config files and call necessary functions

## Available Configs

### Retry

[retry.config](./config/retry/retry.config)

To enable Nextflow's retry mechanism, set up the configuration for process-specific resource allocation by adding a `retry_strategy` namespace for each process that requires a retry mechanism. In these namespaces, a retry strategy for `memory` and `cpus` can be specified (both are not required, include only the resource(s) that require an update with each retry) as separate namespaces, where each must define a `strategy` (the mathematical operation for updating the resource) and an `operand` (the value used to perform the `strategy`).

Example:
```
process {
    withName: 'proc1' {
        cpus = 4
        memory = 8.GB
        retry_strategy {
            memory {
                strategy = 'exponential'
                operand = 2
            }
            cpus {
                strategy = 'add'
                operand = 6
            }
        }
    }
}
```

Available strategies:
- `add` - add `operand` with each retry
- `subtract` - subtract `operand` with each retry
- `exponential` - multiply by `operand` with each retry, results in exponential growth with `operand` > 1 and exponential decay with 0 < `operand` < 1.

To convert these configurations into Nextflow retry settings, call the `setup_retry` function from the `retry` namespace in `methods.config`.

Example:
```
includeConfig "/path/to/retry.config"
...
methods {
    ...
    setup = {
        ...
        retry.setup_retry()
    }
}
```

## License

Author: Yash Patel (YashPatel@mednet.ucla.edu)

pipeline-Nextflow-config is licensed under the GNU General Public License version 2. See the file LICENSE for the terms of the GNU GPL license.

pipeline-Nextflow-config comprises a set of commonly used Nextflow configs.

Copyright (C) 2022 University of California Los Angeles ("Boutros Lab") All rights reserved.

This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
