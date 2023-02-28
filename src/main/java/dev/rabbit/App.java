package dev.rabbit;

import org.apache.beam.sdk.options.PipelineOptionsFactory;

public class App {

    public static void main(String[] args) {
        TreeBuilderPipelineOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(TreeBuilderPipelineOptions.class);
        TreeBuilderPipeline.run(options);
    }
}
