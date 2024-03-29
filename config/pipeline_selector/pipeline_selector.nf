/**
*   This namespace contains the function needed for selecting pipelines to run for metapipelines
*/
pipeline_selector {
    /**
    *   Ensure requested pipelines are valid
    */
    validate_pipelines = { Map dependencies, List pipelines ->
        def unexpected_pipelines = []
        pipelines.each{ pipeline ->
            if (! dependencies.containsKey(pipeline)) {
                unexpected_pipelines.add(pipeline)
            }
        }

        if (unexpected_pipelines) {
            throw new Exception("### ERROR ### Found unexpected pipelines: ${unexpected_pipelines}. Expected selection from ${dependencies.keySet()}.")
        }
    }

    resolve_dependencies = { List requested_pipelines, Map pipeline_dependencies ->
        def dependencies_to_check = [] as Queue
        def pipelines_to_run = [] as Set

        requested_pipelines.each{ pipeline ->
            dependencies_to_check.offer(pipeline)
        }

        def curr_pipeline = ''
        while (curr_pipeline = dependencies_to_check.poll()) {
            pipelines_to_run.add(curr_pipeline)
            pipeline_dependencies[curr_pipeline].each{ dependency ->
                dependencies_to_check.offer(dependency)
            }
        }

        return pipelines_to_run as List
    }

    /**
    *   Entry point for selecting pipelines and identifying all pre-requisite pipelines for selection
    */
    get_pipelines = { Map pipeline_dependencies, List requested_pipelines=[] ->
        if (!requested_pipelines) {
            return pipeline_dependencies.keySet() as List
        }

        pipeline_selector.validate_pipelines(pipeline_dependencies, requested_pipelines)

        return pipeline_selector.resolve_dependencies(requested_pipelines, pipeline_dependencies)
    }
}
