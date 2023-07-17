package org.pentaho.di.trans.step;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

public class Test extends BaseStep implements StepInterface {

    private TestData testData;


    private TestMeta testMeta;

    public Test(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans) {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }

    @Override
    public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
        testMeta = (TestMeta) smi;
        testData = (TestData) sdi;
        return super.init(smi, sdi);
    }

    @Override
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
        testMeta = (TestMeta) smi;
        testData = (TestData) sdi;

        Object[] row = getRow();

        if(null == row){
            setOutputDone();
            return false;
        }

        if(first){

            testMeta.setNoOfRows(0);
            first = false;

        }

        testMeta.setNoOfRows(testMeta.getNoOfRows()+1);
        putRow( getInputRowMeta(),row);

        return true;
    }

    @Override
    public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
        System.out.println("In Dispose");
        testMeta = (TestMeta)smi;
        testData = (TestData)sdi;

        //testMeta.setNoOfRows(this.rowsetInputSize());


        super.dispose(smi, sdi);
    }
}
