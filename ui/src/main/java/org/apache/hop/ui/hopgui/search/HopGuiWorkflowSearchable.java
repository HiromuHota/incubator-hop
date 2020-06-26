package org.apache.hop.ui.hopgui.search;

import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.search.ISearchResult;
import org.apache.hop.core.search.ISearchable;
import org.apache.hop.core.search.ISearchableCallback;
import org.apache.hop.ui.hopgui.HopGui;
import org.apache.hop.ui.hopgui.file.workflow.HopWorkflowFileType;
import org.apache.hop.ui.hopgui.perspective.dataorch.HopDataOrchestrationPerspective;
import org.apache.hop.workflow.WorkflowMeta;

public class HopGuiWorkflowSearchable implements ISearchable<WorkflowMeta> {

  private String location;
  private WorkflowMeta workflowMeta;

  public HopGuiWorkflowSearchable( String location, WorkflowMeta workflowMeta ) {
    this.location = location;
    this.workflowMeta = workflowMeta;
  }

  @Override public String getLocation() {
    return location;
  }

  @Override public String getName() {
    return workflowMeta.getName();
  }

  @Override public String getType() {
    return HopWorkflowFileType.WORKFLOW_FILE_TYPE_DESCRIPTION;
  }

  @Override public String getFilename() {
    return workflowMeta.getFilename();
  }

  @Override public WorkflowMeta getSearchableObject() {
    return workflowMeta;
  }

  @Override public ISearchableCallback getSearchCallback() {
    return new ISearchableCallback() {
      @Override public void callback( ISearchable searchable, ISearchResult searchResult ) throws HopException {
        HopDataOrchestrationPerspective perspective = HopGui.getDataOrchestrationPerspective();
        perspective.addWorkflow( perspective.getComposite(), HopGui.getInstance(), workflowMeta, perspective.getWorkflowFileType() );
      }
    };
  }
}
