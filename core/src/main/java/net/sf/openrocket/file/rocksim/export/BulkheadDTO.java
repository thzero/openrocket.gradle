package net.sf.openrocket.file.rocksim.export;

// thZero

import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.rocketcomponent.Bulkhead;

// thZero - Begin
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
// thZero - End

/**
 * Conversion of an OR Bulkhead to an Rocksim Bulkhead.  Bulkheads are represented as Rings in Rocksim.
 */
@XmlRootElement(name = RocksimCommonConstants.RING)
@XmlAccessorType(XmlAccessType.FIELD)
public class BulkheadDTO extends CenteringRingDTO {

    /**
     * Constructor.
     *
     * @param theORBulkhead the OR bulkhead
     */
    public BulkheadDTO(Bulkhead theORBulkhead) {
        super(theORBulkhead);
        setUsageCode(CenteringRingDTO.UsageCode.Bulkhead);
    }
}
