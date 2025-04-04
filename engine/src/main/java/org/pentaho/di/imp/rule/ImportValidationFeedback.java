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


package org.pentaho.di.imp.rule;

import java.util.ArrayList;
import java.util.List;

public class ImportValidationFeedback {
  private ImportRuleInterface importRule;
  private ImportValidationResultType resultType;
  private String comment;

  /**
   * @param resultType
   * @param comment
   */
  public ImportValidationFeedback( ImportRuleInterface importRule, ImportValidationResultType resultType,
    String comment ) {
    this.importRule = importRule;
    this.resultType = resultType;
    this.comment = comment;
  }

  public static List<ImportValidationFeedback> getErrors( List<ImportValidationFeedback> feedback ) {
    List<ImportValidationFeedback> errors = new ArrayList<ImportValidationFeedback>();

    for ( ImportValidationFeedback error : feedback ) {
      if ( error.isError() ) {
        errors.add( error );
      }
    }

    return errors;
  }

  @Override
  public String toString() {
    StringBuilder string = new StringBuilder();

    string.append( resultType.name() ).append( " : " );
    string.append( comment ).append( " - " );
    string.append( importRule.toString() );

    return string.toString();
  }

  /**
   * @return the resultType
   */
  public ImportValidationResultType getResultType() {
    return resultType;
  }

  public boolean isError() {
    return resultType == ImportValidationResultType.ERROR;
  }

  public boolean isApproval() {
    return resultType == ImportValidationResultType.APPROVAL;
  }

  /**
   * @param resultType
   *          the resultType to set
   */
  public void setResultType( ImportValidationResultType resultType ) {
    this.resultType = resultType;
  }

  /**
   * @return the comment
   */
  public String getComment() {
    return comment;
  }

  /**
   * @param comment
   *          the comment to set
   */
  public void setComment( String comment ) {
    this.comment = comment;
  }

  /**
   * @return the importRule
   */
  public ImportRuleInterface getImportRule() {
    return importRule;
  }

  /**
   * @param importRule
   *          the importRule to set
   */
  public void setImportRule( ImportRuleInterface importRule ) {
    this.importRule = importRule;
  }

}
