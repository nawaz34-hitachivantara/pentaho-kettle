package org.pentaho.di.trans.step;

import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

@Step(id="Test",name = "Test.Step.Name",
        i18nPackageName = "org.pentaho.di.trans.step",image = "analyzer.svg",
        description = "Test.Step.Description",categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Flow")
public class TestMeta extends BaseStepMeta implements StepMetaInterface {

    private int noOfRows;

    public TestMeta() {
        super();
    }



    @Override
    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
        return new Test( stepMeta, stepDataInterface, copyNr, transMeta, trans );
    }

    @Override
    public StepDataInterface getStepData() {
        return new TestData();
    }


    public int getNoOfRows() {
        return noOfRows;
    }

    public void setNoOfRows(int noOfRows) {
        this.noOfRows = noOfRows;
    }


    @Override
    public void setDefault() {
        noOfRows = 0;
    }
}
