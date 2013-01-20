
package tx;

import javax.transaction.xa.Xid;

public class XidImpl implements Xid {

    private int id;
    
    public XidImpl(int id) {
	this.id = id;
    }
    
    public int getFormatId() {
	return 0;
    }

    @Override
    public byte[] getGlobalTransactionId() {
	return ("xid"+id).getBytes();
    }

    @Override
    public byte[] getBranchQualifier() {
	return new byte[0];
    }
    
}
