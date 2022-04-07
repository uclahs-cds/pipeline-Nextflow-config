/*
*   Nextflow module for validating files and directories
*
*   @input  mode    string  Type of validation
*   @input  file_to_validate    path    File to validate
*
*   @params validation_log_output_dir   path    Directory for validation log output
*   @params docker_image_pipeval    string  Docker image for running process
*/
process run_validate_PipeVal {
    container params.docker_image_pipeval // Docker image reference

    publishDir path: "${params.validation_log_output_dir}",
        pattern: ".command.*",
        mode: "copy",
        saveAs: { "${task.process.replace(':','/')}-${task.index}/log${file(it).getName()}" }

    input:
        tuple val(mode), path(file_to_validate)

    output:
        path(".command.*")
        path("input_validation.txt"), emit: val_file

    script:
    """
    set -euo pipefail
    python3 -m validate -t ${mode} ${file_to_validate} > 'input_validation.txt'
    """
}