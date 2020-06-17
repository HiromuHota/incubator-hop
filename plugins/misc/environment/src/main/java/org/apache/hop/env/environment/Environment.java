package org.apache.hop.env.environment;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang.StringUtils;
import org.apache.hop.core.Const;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.core.variables.Variables;
import org.apache.hop.env.util.Defaults;
import org.apache.hop.env.util.EnvironmentUtil;
import org.apache.hop.metadata.api.HopMetadataProperty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Environment {

  private String description;

  private String version;

  // Environment metadata (nice to know)
  //
  private String company;

  private String department;

  private String project;

  private String metadataBaseFolder;


  // Data Sets , Unit tests
  //
  private String unitTestsBasePath;

  private String dataSetsCsvFolder;

  private boolean enforcingExecutionInHome;

  // Variables
  //
  @HopMetadataProperty
  private List<EnvironmentVariable> variables;


  public Environment() {
    variables = new ArrayList<>();
    metadataBaseFolder = "${" + EnvironmentUtil.VARIABLE_ENVIRONMENT_HOME + "}/metadata";
    dataSetsCsvFolder = "${" + EnvironmentUtil.VARIABLE_ENVIRONMENT_HOME + "}/datasets";
    unitTestsBasePath = "${" + EnvironmentUtil.VARIABLE_ENVIRONMENT_HOME + "}";
    enforcingExecutionInHome = true;
  }

  public String toJsonString() throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setSerializationInclusion( JsonInclude.Include.NON_DEFAULT );
    objectMapper.enable( SerializationFeature.INDENT_OUTPUT );
    return objectMapper.writeValueAsString( this );
  }

  public static Environment fromJsonString( String jsonString ) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue( jsonString, Environment.class );
  }

  public void modifyVariables( IVariables variables, String environmentName, String environmentHomeFolder ) {

    if ( variables == null ) {
      variables = Variables.getADefaultVariableSpace();
    }

    // Set the name of the active environment
    //
    variables.setVariable( Defaults.VARIABLE_ACTIVE_ENVIRONMENT, Const.NVL( environmentName, "" ) );

    if ( StringUtils.isNotEmpty( environmentHomeFolder ) ) {
      String realValue = variables.environmentSubstitute( environmentHomeFolder );
      variables.setVariable( EnvironmentUtil.VARIABLE_ENVIRONMENT_HOME, realValue );
    }
    if ( StringUtils.isNotEmpty( metadataBaseFolder ) ) {
      String realValue = variables.environmentSubstitute( metadataBaseFolder );
      variables.setVariable( Const.HOP_METADATA_FOLDER, realValue );
    }
    if ( StringUtils.isNotEmpty( unitTestsBasePath ) ) {
      String realValue = variables.environmentSubstitute( unitTestsBasePath );
      variables.setVariable( EnvironmentUtil.VARIABLE_UNIT_TESTS_BASE_PATH, realValue );
    }
    if ( StringUtils.isNotEmpty( dataSetsCsvFolder ) ) {
      String realValue = variables.environmentSubstitute( dataSetsCsvFolder );
      variables.setVariable( EnvironmentUtil.VARIABLE_DATASETS_BASE_PATH, realValue );
    }

    for ( EnvironmentVariable variable : this.variables ) {
      if ( variable.getName() != null ) {
        variables.setVariable( variable.getName(), variable.getValue() );
      }
    }
  }

  /**
   * Gets description
   *
   * @return value of description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description The description to set
   */
  public void setDescription( String description ) {
    this.description = description;
  }

  /**
   * Gets company
   *
   * @return value of company
   */
  public String getCompany() {
    return company;
  }

  /**
   * @param company The company to set
   */
  public void setCompany( String company ) {
    this.company = company;
  }

  /**
   * Gets department
   *
   * @return value of department
   */
  public String getDepartment() {
    return department;
  }

  /**
   * @param department The department to set
   */
  public void setDepartment( String department ) {
    this.department = department;
  }

  /**
   * Gets metadataBaseFolder
   *
   * @return value of metadataBaseFolder
   */
  public String getMetadataBaseFolder() {
    return metadataBaseFolder;
  }

  /**
   * @param metadataBaseFolder The metadataBaseFolder to set
   */
  public void setMetadataBaseFolder( String metadataBaseFolder ) {
    this.metadataBaseFolder = metadataBaseFolder;
  }

  /**
   * Gets project
   *
   * @return value of project
   */
  public String getProject() {
    return project;
  }

  /**
   * @param project The project to set
   */
  public void setProject( String project ) {
    this.project = project;
  }

  /**
   * Gets version
   *
   * @return value of version
   */
  public String getVersion() {
    return version;
  }

  /**
   * @param version The version to set
   */
  public void setVersion( String version ) {
    this.version = version;
  }

  /**
   * Gets unitTestsBasePath
   *
   * @return value of unitTestsBasePath
   */
  public String getUnitTestsBasePath() {
    return unitTestsBasePath;
  }

  /**
   * @param unitTestsBasePath The unitTestsBasePath to set
   */
  public void setUnitTestsBasePath( String unitTestsBasePath ) {
    this.unitTestsBasePath = unitTestsBasePath;
  }

  /**
   * Gets dataSetsCsvFolder
   *
   * @return value of dataSetsCsvFolder
   */
  public String getDataSetsCsvFolder() {
    return dataSetsCsvFolder;
  }

  /**
   * @param dataSetsCsvFolder The dataSetsCsvFolder to set
   */
  public void setDataSetsCsvFolder( String dataSetsCsvFolder ) {
    this.dataSetsCsvFolder = dataSetsCsvFolder;
  }

  /**
   * Gets variables
   *
   * @return value of variables
   */
  public List<EnvironmentVariable> getVariables() {
    return variables;
  }

  /**
   * @param variables The variables to set
   */
  public void setVariables( List<EnvironmentVariable> variables ) {
    this.variables = variables;
  }

  /**
   * Gets enforcingExecutionInHome
   *
   * @return value of enforcingExecutionInHome
   */
  public boolean isEnforcingExecutionInHome() {
    return enforcingExecutionInHome;
  }

  /**
   * @param enforcingExecutionInHome The enforcingExecutionInHome to set
   */
  public void setEnforcingExecutionInHome( boolean enforcingExecutionInHome ) {
    this.enforcingExecutionInHome = enforcingExecutionInHome;
  }
}
