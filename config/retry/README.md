# Retry mechanism

To enable Nextflow's process retry mechanism, set up the configuration for process-specific resource allocation by adding a `retry_strategy` namespace for each process that requires a retry mechanism. In these namespaces, a retry strategy for `memory` and `cpus` can be specified (both are not required, include only the resource(s) that require an update with each retry) as separate namespaces, where each must define a `strategy` (the mathematical operation for updating the resource) and an `operand` (the value used to perform the `strategy`).

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
