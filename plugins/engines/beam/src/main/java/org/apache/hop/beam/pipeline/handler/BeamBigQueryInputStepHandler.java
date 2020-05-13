package org.apache.hop.beam.pipeline.handler;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.values.PCollection;
import org.apache.hop.beam.core.HopRow;
import org.apache.hop.beam.core.transform.BeamBQInputTransform;
import org.apache.hop.beam.core.util.JsonRowMeta;
import org.apache.hop.beam.engines.IBeamPipelineEngineRunConfiguration;
import org.apache.hop.beam.transforms.bq.BeamBQInputMeta;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.logging.ILogChannel;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.metastore.api.IMetaStore;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.TransformMeta;

import java.util.List;
import java.util.Map;

public class BeamBigQueryInputStepHandler extends BeamBaseStepHandler implements BeamStepHandler {

  public BeamBigQueryInputStepHandler( IBeamPipelineEngineRunConfiguration runConfiguration, IMetaStore metaStore, PipelineMeta pipelineMeta, List<String> transformPluginClasses, List<String> xpPluginClasses ) {
    super( runConfiguration, true, false, metaStore, pipelineMeta, transformPluginClasses, xpPluginClasses );
  }

  @Override public void handleStep( ILogChannel log, TransformMeta transformMeta, Map<String, PCollection<HopRow>> stepCollectionMap,
                                    Pipeline pipeline, IRowMeta rowMeta, List<TransformMeta> previousSteps,
                                    PCollection<HopRow> input ) throws HopException {

    // Input handling
    //
    BeamBQInputMeta beamInputMeta = (BeamBQInputMeta) transformMeta.getTransform();

    // Output rows (fields selection)
    //
    IRowMeta outputRowMeta = new RowMeta();
    beamInputMeta.getFields( outputRowMeta, transformMeta.getName(), null, null, pipelineMeta, null );

    BeamBQInputTransform beamInputTransform = new BeamBQInputTransform(
      transformMeta.getName(),
      transformMeta.getName(),
      pipelineMeta.environmentSubstitute( beamInputMeta.getProjectId() ),
      pipelineMeta.environmentSubstitute( beamInputMeta.getDatasetId() ),
      pipelineMeta.environmentSubstitute( beamInputMeta.getTableId() ),
      pipelineMeta.environmentSubstitute( beamInputMeta.getQuery() ),
      JsonRowMeta.toJson( outputRowMeta ),
      transformPluginClasses,
      xpPluginClasses
    );
    PCollection<HopRow> afterInput = pipeline.apply( beamInputTransform );
    stepCollectionMap.put( transformMeta.getName(), afterInput );
    log.logBasic( "Handled transform (BQ INPUT) : " + transformMeta.getName() );

  }
}
