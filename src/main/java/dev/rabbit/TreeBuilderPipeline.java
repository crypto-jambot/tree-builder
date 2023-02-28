package dev.rabbit;

import dev.rabbit.doFn.BuildTrees;
import dev.rabbit.util.Mapper;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.TypeDescriptors;

public class TreeBuilderPipeline {

    public static void run(TreeBuilderPipelineOptions options) {

        Pipeline pipeline = Pipeline.create(options);

        pipeline.apply("Query for path data", BigQueryIO.readTableRows()
                        .fromQuery(options.getQueryString())
                        .withoutValidation()
                        .withTemplateCompatibility()
                        .usingStandardSql())
                .apply("Build Trees", ParDo.of(BuildTrees.of()))
                .apply("Map to JSON", MapElements.into(TypeDescriptors.strings()).via(Mapper.INSTANCE::toJsonString))
                .apply("Write to GCS", TextIO.write().to(options.getGcsBucket()).withNumShards(options.getShardNumber()).withSuffix(".jsonl"));

        pipeline.run();
    }
}
