/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.jsonoutput;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Converts input rows to one or more XML files.
 *
 * @author Matt
 * @since 14-jan-2006
 */
public class JsonOutput extends BaseStep implements StepInterface {
  private static Class<?> PKG = JsonOutput.class; // for i18n purposes, needed by Translator2!!

  private JsonOutputMeta meta;
  private JsonOutputData data;
  private Date startProcessingDate;

  private interface CompatibilityFactory {
    public void execute( Object[] row ) throws KettleException;
  }

  @SuppressWarnings( "unchecked" )
  private class CompatibilityMode implements CompatibilityFactory {
    public void execute( Object[] row ) throws KettleException {

      for ( int i = 0; i < data.nrFields; i++ ) {
        JsonOutputField outputField = meta.getOutputFields()[i];

        ValueMetaInterface v = data.inputRowMeta.getValueMeta( data.fieldIndexes[i] );

        // Create a new object with specified fields
        JSONObject jo = new JSONObject();

        switch ( v.getType() ) {
          case ValueMetaInterface.TYPE_BOOLEAN:
            jo.put( outputField.getElementName(), data.inputRowMeta.getBoolean( row, data.fieldIndexes[i] ) );
            break;
          case ValueMetaInterface.TYPE_INTEGER:
            jo.put( outputField.getElementName(), data.inputRowMeta.getInteger( row, data.fieldIndexes[i] ) );
            break;
          case ValueMetaInterface.TYPE_NUMBER:
            jo.put( outputField.getElementName(), data.inputRowMeta.getNumber( row, data.fieldIndexes[i] ) );
            break;
          case ValueMetaInterface.TYPE_BIGNUMBER:
            jo.put( outputField.getElementName(), data.inputRowMeta.getBigNumber( row, data.fieldIndexes[i] ) );
            break;
          default:
            jo.put( outputField.getElementName(), data.inputRowMeta.getString( row, data.fieldIndexes[i] ) );
            break;
        }
        data.ja.add( jo );
      }

      data.nrRow++;

      if ( data.nrRowsInBloc > 0 ) {
        // System.out.println("data.nrRow%data.nrRowsInBloc = "+ data.nrRow%data.nrRowsInBloc);
        if ( data.nrRow % data.nrRowsInBloc == 0 ) {
          // We can now output an object
          // System.out.println("outputting the row.");
          outPutRow( row );
        }
      }
    }
  }

  @SuppressWarnings( "unchecked" )
  private class FixedMode implements CompatibilityFactory {
    public void execute( Object[] row ) throws KettleException {

      // Create a new object with specified fields
      JSONObject jo = new JSONObject();

      for ( int i = 0; i < data.nrFields; i++ ) {
        JsonOutputField outputField = meta.getOutputFields()[i];

        ValueMetaInterface v = data.inputRowMeta.getValueMeta( data.fieldIndexes[i] );

        switch ( v.getType() ) {
          case ValueMetaInterface.TYPE_BOOLEAN:
            jo.put( outputField.getElementName(), data.inputRowMeta.getBoolean( row, data.fieldIndexes[i] ) );
            break;
          case ValueMetaInterface.TYPE_INTEGER:
            jo.put( outputField.getElementName(), data.inputRowMeta.getInteger( row, data.fieldIndexes[i] ) );
            break;
          case ValueMetaInterface.TYPE_NUMBER:
            jo.put( outputField.getElementName(), data.inputRowMeta.getNumber( row, data.fieldIndexes[i] ) );
            break;
          case ValueMetaInterface.TYPE_BIGNUMBER:
            jo.put( outputField.getElementName(), data.inputRowMeta.getBigNumber( row, data.fieldIndexes[i] ) );
            break;
          default:
            jo.put( outputField.getElementName(), data.inputRowMeta.getString( row, data.fieldIndexes[i] ) );
            break;
        }
      }
      data.ja.add( jo );

      data.nrRow++;

      if ( data.nrRowsInBloc > 0 ) {
        // System.out.println("data.nrRow%data.nrRowsInBloc = "+ data.nrRow%data.nrRowsInBloc);
        if ( data.nrRow % data.nrRowsInBloc == 0 ) {
          // We can now output an object
          // System.out.println("outputting the row.");
          outPutRow( row );
        }
      }
    }
  }

  private CompatibilityFactory compatibilityFactory;

  public JsonOutput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
                     Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );

    // Here we decide whether or not to build the structure in
    // compatible mode or fixed mode
    JsonOutputMeta jsonOutputMeta = (JsonOutputMeta) ( stepMeta.getStepMetaInterface() );
    if ( jsonOutputMeta.isCompatibilityMode() ) {
      compatibilityFactory = new CompatibilityMode();
    } else {
      compatibilityFactory = new FixedMode();
    }
  }

  @SuppressWarnings( "java:S1144" )
  public JSONObject getOperationTypesAction( Map<String, String> queryParamToValues ) {
    JSONObject response = new JSONObject();
    JSONArray operationTypes = new JSONArray();

    for ( int i = 0; i < JsonOutputMeta.operationTypeCode.length; i++ ) {
      JSONObject operationType = new JSONObject();
      operationType.put( "id", JsonOutputMeta.operationTypeCode[i] );
      operationType.put( "name", JsonOutputMeta.operationTypeDesc[i] );
      operationTypes.add( operationType );
    }

    response.put( "operationTypes", operationTypes );
    response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    return response;
  }

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  public  JSONObject getEncodingTypesAction( Map<String, String> queryParamToValues) {
    JSONObject response = new JSONObject();
    JSONArray encodingsArray = new JSONArray();

    List<Charset> availableCharsets = new ArrayList<>( Charset.availableCharsets().values() );
    for ( Charset charset : availableCharsets ) {
      encodingsArray.add( charset.displayName() );
    }

    response.put( "encoding", encodingsArray );
    response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    return response;
  }

  @SuppressWarnings( "java:S1144" ) // Using reflection this method is being invoked
  public JSONObject showFileNameAction( Map<String, String> queryParams ) {
    JSONObject response = new JSONObject();
    JsonOutputMeta jsonOutputMeta = ( JsonOutputMeta ) getStepMetaInterface();

    JSONArray fileList = new JSONArray();
    startProcessingDate = new Date();

    if ( jsonOutputMeta.getFileName() != null && !jsonOutputMeta.getFileName().isEmpty() ) {
      String fileName = jsonOutputMeta.buildFilename( jsonOutputMeta.getFileName(), startProcessingDate );
      fileList.add( fileName );
    }

    if ( fileList.isEmpty() ) {
      response.put( "message", BaseMessages.getString( PKG, "JsonOutputDialog.NoFilesFound.DialogMessage" ) );
    } else {
      response.put( StepInterface.ACTION_STATUS, StepInterface.SUCCESS_RESPONSE );
    }

    response.put( "files", fileList );
    return response;
  }

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {

    meta = (JsonOutputMeta) smi;
    data = (JsonOutputData) sdi;

    Object[] r = getRow(); // This also waits for a row to be finished.
    if ( r == null ) {
      // no more input to be expected...
      if ( !data.rowsAreSafe ) {
        // Let's output the remaining unsafe data
        outPutRow( r );
      }

      setOutputDone();
      return false;
    }

    if ( first ) {
      first = false;
      startProcessingDate = new Date();
      data.inputRowMeta = getInputRowMeta();
      data.inputRowMetaSize = data.inputRowMeta.size();
      if ( data.outputValue ) {
        data.outputRowMeta = data.inputRowMeta.clone();
        meta.getFields( getTransMeta().getBowl(), data.outputRowMeta, getStepname(), null, null, this, repository,
                        metaStore );
      }

      // Cache the field name indexes
      //
      data.nrFields = meta.getOutputFields().length;
      data.fieldIndexes = new int[data.nrFields];
      for ( int i = 0; i < data.nrFields; i++ ) {
        data.fieldIndexes[i] = data.inputRowMeta.indexOfValue( meta.getOutputFields()[i].getFieldName() );
        if ( data.fieldIndexes[i] < 0 ) {
          throw new KettleException( BaseMessages.getString( PKG, "JsonOutput.Exception.FieldNotFound" ) );
        }
        JsonOutputField field = meta.getOutputFields()[i];
        field.setElementName( environmentSubstitute( field.getElementName() ) );
      }
    }

    data.rowsAreSafe = false;
    compatibilityFactory.execute( r );

    if ( data.writeToFile && !data.outputValue ) {
      putRow( data.inputRowMeta, r ); // in case we want it go further...
      incrementLinesOutput();
    }
    return true;
  }

  @SuppressWarnings( "unchecked" )
  private void outPutRow( Object[] rowData ) throws KettleStepException {
    // We can now output an object
    data.jg = new JSONObject();
    String value;
    if ( data.realBlocName == "" ) {
      value = data.ja.toJSONString();
    } else {
      data.jg.put( data.realBlocName, data.ja );
      value = data.jg.toJSONString();
    }


    if ( data.outputValue && data.outputRowMeta != null ) {
      Object[] outputRowData = RowDataUtil.addValueData( rowData, data.inputRowMetaSize, value );
      incrementLinesOutput();
      putRow( data.outputRowMeta, outputRowData );
    }

    if ( data.writeToFile && !data.ja.isEmpty() ) {
      // Open a file
      if ( !openNewFile() ) {
        throw new KettleStepException( BaseMessages.getString(
          PKG, "JsonOutput.Error.OpenNewFile", buildFilename() ) );
      }
      // Write data to file
      try {
        data.writer.write( value );
      } catch ( Exception e ) {
        throw new KettleStepException( BaseMessages.getString( PKG, "JsonOutput.Error.Writing" ), e );
      }
      // Close file
      closeFile();
    }
    // Data are safe
    data.rowsAreSafe = true;
    data.ja = new JSONArray();
  }

  public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (JsonOutputMeta) smi;
    data = (JsonOutputData) sdi;
    if ( super.init( smi, sdi ) ) {

      data.writeToFile = ( meta.getOperationType() != JsonOutputMeta.OPERATION_TYPE_OUTPUT_VALUE );
      data.outputValue = ( meta.getOperationType() != JsonOutputMeta.OPERATION_TYPE_WRITE_TO_FILE );

      if ( data.outputValue ) {
        // We need to have output field name
        if ( Utils.isEmpty( environmentSubstitute( meta.getOutputValue() ) ) ) {
          logError( BaseMessages.getString( PKG, "JsonOutput.Error.MissingOutputFieldName" ) );
          stopAll();
          setErrors( 1 );
          return false;
        }
      }
      if ( data.writeToFile ) {
        // We need to have output field name
        if ( !meta.isServletOutput() && Utils.isEmpty( meta.getFileName() ) ) {
          logError( BaseMessages.getString( PKG, "JsonOutput.Error.MissingTargetFilename" ) );
          stopAll();
          setErrors( 1 );
          return false;
        }
        if ( !meta.isDoNotOpenNewFileInit() ) {
          if ( !openNewFile() ) {
            logError( BaseMessages.getString( PKG, "JsonOutput.Error.OpenNewFile", buildFilename() ) );
            stopAll();
            setErrors( 1 );
            return false;
          }
        }

      }
      data.realBlocName = Const.NVL( environmentSubstitute( meta.getJsonBloc() ), "" );
      data.nrRowsInBloc = Const.toInt( environmentSubstitute( meta.getNrRowsInBloc() ), 0 );
      return true;
    }

    return false;
  }

  public void dispose( StepMetaInterface smi, StepDataInterface sdi ) {
    meta = (JsonOutputMeta) smi;
    data = (JsonOutputData) sdi;
    if ( data.ja != null ) {
      data.ja = null;
    }
    if ( data.jg != null ) {
      data.jg = null;
    }
    closeFile();
    super.dispose( smi, sdi );

  }

  private void createParentFolder( String filename ) throws KettleStepException {
    if ( !meta.isCreateParentFolder() ) {
      return;
    }
    // Check for parent folder
    FileObject parentfolder = null;
    try {
      // Get parent folder
      parentfolder = KettleVFS.getInstance( getTransMeta().getBowl() )
        .getFileObject( filename, getTransMeta() ).getParent();
      if ( !parentfolder.exists() ) {
        if ( log.isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "JsonOutput.Error.ParentFolderNotExist", parentfolder.getName() ) );
        }
        parentfolder.createFolder();
        if ( log.isDebug() ) {
          logDebug( BaseMessages.getString( PKG, "JsonOutput.Log.ParentFolderCreated" ) );
        }
      }
    } catch ( Exception e ) {
      throw new KettleStepException( BaseMessages.getString(
        PKG, "JsonOutput.Error.ErrorCreatingParentFolder", parentfolder.getName() ) );
    } finally {
      if ( parentfolder != null ) {
        try {
          parentfolder.close();
        } catch ( Exception ex ) { /* Ignore */
        }
      }
    }
  }

  public boolean openNewFile() {
    if ( data.writer != null ) {
      return true;
    }
    boolean retval = false;

    try {

      if ( meta.isServletOutput() ) {
        data.writer = getTrans().getServletPrintWriter();
      } else {
        String filename = buildFilename();
        createParentFolder( filename );
        if ( meta.AddToResult() ) {
          // Add this to the result file names...
          ResultFile resultFile =
            new ResultFile(
              ResultFile.FILE_TYPE_GENERAL, KettleVFS.getInstance( getTransMeta().getBowl() )
                .getFileObject( filename, getTransMeta() ),
              getTransMeta().getName(), getStepname() );
          resultFile.setComment( BaseMessages.getString( PKG, "JsonOutput.ResultFilenames.Comment" ) );
          addResultFile( resultFile );
        }

        OutputStream outputStream;
        OutputStream fos = KettleVFS.getInstance( getTransMeta().getBowl() )
          .getOutputStream( filename, getTransMeta(), meta.isFileAppended() );
        outputStream = fos;

        if ( !Utils.isEmpty( meta.getEncoding() ) ) {
          data.writer =
            new OutputStreamWriter( new BufferedOutputStream( outputStream, 5000 ), environmentSubstitute( meta
              .getEncoding() ) );
        } else {
          data.writer = new OutputStreamWriter( new BufferedOutputStream( outputStream, 5000 ) );
        }

        if ( log.isDetailed() ) {
          logDetailed( BaseMessages.getString( PKG, "JsonOutput.FileOpened", filename ) );
        }

        data.splitnr++;
      }

      retval = true;

    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "JsonOutput.Error.OpeningFile", e.toString() ) );
    }

    return retval;
  }

  public String buildFilename() {

    boolean forceSameOutputFile = "Y".equalsIgnoreCase(
      System.getProperty( Const.KETTLE_JSON_OUTPUT_FORCE_SAME_OUTPUT_FILE, "N" ) );

    if ( forceSameOutputFile ) {
      return meta.buildFilename( environmentSubstitute( meta.getFileName() ), startProcessingDate );
    }

    return meta.buildFilename( meta.getParentStepMeta().getParentTransMeta(), getCopy() + "", null, data.splitnr + "",
      false );
  }

  protected boolean closeFile() {
    if ( data.writer == null ) {
      return true;
    }
    boolean retval = false;

    try {
      data.writer.close();
      data.writer = null;
      retval = true;
    } catch ( Exception e ) {
      logError( BaseMessages.getString( PKG, "JsonOutput.Error.ClosingFile", e.toString() ) );
      setErrors( 1 );
      retval = false;
    }

    return retval;
  }
}
