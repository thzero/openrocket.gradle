package net.sf.openrocket.file.rocksim.export;

// thZero

import net.sf.openrocket.rocketcomponent.EngineBlock;
// thZero - Begin
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
// thZero - End

/**
 * Models a Rocksim XML Element for an EngineBlock.  EngineBlocks in Rocksim are treated as rings with a special
 * usage code.
 */
@XmlRootElement(name = "Ring")
@XmlAccessorType(XmlAccessType.FIELD)
public class EngineBlockDTO extends CenteringRingDTO{

    /**
     * Copy constructor.
     *
     * @param theOREngineBlock
     */
    public EngineBlockDTO(EngineBlock theOREngineBlock) {
        super(theOREngineBlock);
        setUsageCode(UsageCode.EngineBlock);
    }
}
