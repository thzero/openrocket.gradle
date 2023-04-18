package net.sf.openrocket.file.rasaero.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;

/**
 * Applies the RASAero surface finish to all components.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public abstract class SurfaceFinishHandler {
    public static void setSurfaceFinishes(Rocket rocket, String finish, WarningSet warnings) {
        ExternalComponent.Finish surfaceFinish = RASAeroCommonConstants.getSurfaceFinishFromRASAero(finish, warnings);
        for (RocketComponent component : rocket) {
            if (component instanceof ExternalComponent) {
                ((ExternalComponent) component).setFinish(surfaceFinish);
            }
        }
    }
}
