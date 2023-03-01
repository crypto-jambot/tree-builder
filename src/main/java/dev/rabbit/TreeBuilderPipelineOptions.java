package dev.rabbit;

import org.apache.beam.sdk.extensions.gcp.options.GcpOptions;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.ValueProvider;

import static org.apache.beam.sdk.options.Validation.Required;

public interface TreeBuilderPipelineOptions extends GcpOptions {
    @Required
    @Default.String("crypto-351521")
    String getGcpProject();

    void setGcpProject(String value);

    @Required
    @Default.String("SELECT * FROM `crypto-351521.ethereum_v2.refined_pools_with_paths`")
    ValueProvider<String> getQueryString();

    void setQueryString(ValueProvider<String> value);

    @Required
    @Default.String("ethereum")
    String getDatabase();

    void setDatabase(String value);

    @Required
    @Default.String("backfill-trees")
    String getCollection();

    void setCollection(String value);

    @Required
    @Default.Integer(500)
    Integer getMongoBatchSize();

    void setMongoBatchSize(Integer value);
}
