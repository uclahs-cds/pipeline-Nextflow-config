#!/usr/bin/env nextflow

nextflow.enable.dsl=2

// Include processes and workflows here
include { run_validate_PipeVal } from './module/validation'
include { tool_name_command_name } from './module/module-name'

// Log info here
log.info """\    
        ======================================
        T E M P L A T E - N F  P I P E L I N E
        ======================================
        Boutros Lab

        Current Configuration:
        - pipeline:
            name: ${workflow.manifest.name}
            version: ${workflow.manifest.version}

        - input:
            input a: ${params.variable_name}
            ...

        - output: 
            output a: ${params.output_path}
            ...

        - options:
            option a: ${params.option_name}
            ...

        Tools Used:
            tool a: ${params.docker_image_name}

        ------------------------------------
        Starting workflow...
        ------------------------------------
        """
        .stripIndent()

// Channels here
// Decription of input channel
Channel
    .fromPath(params.input_csv)
    .ifEmpty { error "Cannot find input csv: ${params.input_csv}" }
    .splitCsv(header:true)
    .map { row -> 
        return tuple(row.row_1_name,
            row.row_2_name_file_extension
            )
        }
    .into { input_ch_input_csv } // copy into two channels, one is for validation

// Decription of input channel
Channel
    .fromPath(params.variable_name)
    .ifEmpty { error "Cannot find: ${params.variable_name}" }
    .into { input_ch_variable_name } // copy into two channels, one is for validation

// Pre-validation steps
input_ch_input_csv // flatten csv channel to only file paths
    .flatMap { library, lane, read_group_name, read1_fastq, read2_fastq ->
        [read1_fastq, read2_fastq]
    }
    .map { ['file-input', it] } // Add the validation type as a tuple
    .set { input_ch_input_csv_validate_tuple } // new tuple validation channel


// Main workflow here
workflow {
    // Validation process
    run_validate_PipeVal(
        input_ch_input_csv_validate_tuple
        )

    // Workflow or process
    tool_name_command_name(
        input_ch_input_csv,
        input_ch_variable_name
        )
}
