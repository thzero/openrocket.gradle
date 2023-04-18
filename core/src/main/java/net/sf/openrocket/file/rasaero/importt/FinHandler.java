package net.sf.openrocket.file.rasaero.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.file.simplesax.AbstractElementHandler;
import net.sf.openrocket.file.simplesax.ElementHandler;
import net.sf.openrocket.file.simplesax.PlainTextHandler;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import org.xml.sax.SAXException;

import java.util.HashMap;

/**
 * A SAX handler for the RASAero Fin element. This is just an OpenRocket TrapezoidFinSet.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class FinHandler extends AbstractElementHandler {
    private final TrapezoidFinSet finSet = new TrapezoidFinSet();
    private final RocketComponent parent;

    public FinHandler(RocketComponent parent, WarningSet warnings) {
        if (parent == null) {
            throw new IllegalArgumentException("The parent component of a body tube may not be null.");
        }
        this.parent = parent;
        finSet.setName("Fin");
        ColorHandler.applyRASAeroColor(finSet, null);   // Use default RASAero color
    }

    @Override
    public ElementHandler openElement(String element, HashMap<String, String> attributes, WarningSet warnings) throws SAXException {
        return PlainTextHandler.INSTANCE;
    }

    @Override
    public void closeElement(String element, HashMap<String, String> attributes, String content, WarningSet warnings)
            throws SAXException {
        try {
            // There is also a PartType element, but we don't need it

            if (RASAeroCommonConstants.FIN_COUNT.equals(element)) {
                finSet.setFinCount(Integer.parseInt(content));
            } else if (RASAeroCommonConstants.FIN_CHORD.equals(element)) {
                finSet.setRootChord(Double.parseDouble(content) / RASAeroCommonConstants.RASAERO_TO_OPENROCKET_LENGTH);
            } else if (RASAeroCommonConstants.FIN_SPAN.equals(element)) {
                finSet.setHeight(Double.parseDouble(content) / RASAeroCommonConstants.RASAERO_TO_OPENROCKET_LENGTH);
            } else if (RASAeroCommonConstants.FIN_SWEEP_DISTANCE.equals(element)) {
                finSet.setSweep(Double.parseDouble(content) / RASAeroCommonConstants.RASAERO_TO_OPENROCKET_LENGTH);
            } else if (RASAeroCommonConstants.FIN_TIP_CHORD.equals(element)) {
                finSet.setTipChord(Double.parseDouble(content) / RASAeroCommonConstants.RASAERO_TO_OPENROCKET_LENGTH);
            } else if (RASAeroCommonConstants.FIN_THICKNESS.equals(element)) {
                finSet.setThickness(Double.parseDouble(content) / RASAeroCommonConstants.RASAERO_TO_OPENROCKET_LENGTH);
            } else if (RASAeroCommonConstants.AIRFOIL_SECTION.equals(element)) {
                finSet.setCrossSection(RASAeroCommonConstants.getFinCrossSectionFromRASAero(content, warnings));
            } else if (RASAeroCommonConstants.LOCATION.equals(element)) {
                // Location is the location of the front of the fin relative to the bottom of the body tube
                finSet.setAxialMethod(AxialMethod.BOTTOM);
                double location = Double.parseDouble(content) / RASAeroCommonConstants.RASAERO_TO_OPENROCKET_LENGTH;
                location = -location + finSet.getLength();
                finSet.setAxialOffset(location);
            }
        } catch (NumberFormatException nfe) {
            warnings.add("Could not convert " + element + " value of " + content + ".  It is expected to be a number.");
        }
    }

    @Override
    public void endHandler(String element, HashMap<String, String> attributes, String content, WarningSet warnings) throws SAXException {
        super.endHandler(element, attributes, content, warnings);
        if (parent instanceof BodyTube) {
            addFinToBodyTube();
        } else if (parent instanceof Transition) {
            addFinToTransition();
        } else {
            throw new IllegalArgumentException("Cannot add fin set to parent of type " + parent.getClass().getName());
        }
    }

    private void addFinToBodyTube() {
        parent.addChild(this.finSet);
    }

    private void addFinToTransition() {
        // You can only add freeform fins to a transition, that's why we need the conversion
        FreeformFinSet fins = FreeformFinSet.convertFinSet(finSet);
        parent.addChild(fins);
    }
}
