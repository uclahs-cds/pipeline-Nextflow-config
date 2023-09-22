# Changelog
All notable changes to pipeline-Nextflow-config.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]
### Added
- Generic resource allocations for outside ucla_cds
- `get_absolute_path` function to resolve a path relative to the current configu file
- Function for merging `publishDir` rules
- Check for empty lists
- Function for specific schema validation
- BAM parsing module
- Funtion to update base resource allocation values
- Env setting function
- Function to use CPUs with Docker to circumvent behavior introduced by Nextflow starting in [v22.11.0-edge](https://github.com/nextflow-io/nextflow/releases/tag/v22.11.0-edge)
- Function for resolving pipelines in metapipelines
- Common functions for align-DNA and -RNA
- Option for user to overwrite previously loaded custum types by calling `schema.load_custom_types()` with argument `purge_existing_custom_types=true`

### Fixed
- BAM parser failing if multiple lines needed to be skipped
- Schema config function `load_custom_types()` overwriting previously loaded custom types if called multiple times
- workDir parity to handle `NXF_WORK` setting

### Changed
- Modify exceptions to be more specific
---

## [1.0.0] - 2022-08-24
### Added
- Retry module
- Schema validation module
- Check for empty strings
- CSV parsing module
- Common methods module
- Registered output directory generation function
- Error handling for node-specific configs
- Reference genome version extraction function
### Changed
- Initial repo set up
- Generalize error message
- Clarify retry README
