package net.sf.openrocket.file.rasaero.importt;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.database.motor.ThrustCurveMotorSet;
import net.sf.openrocket.file.motor.AbstractMotorLoader;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.startup.Application;

import java.util.ArrayList;
import java.util.List;

public abstract class RASAeroMotorsLoader {
    private static List<ThrustCurveMotor> allMotors = null;

    /**
     * Returns a RASAero motor from the motor string of its RASAero file.
     * @param motorString The motor string of the RASAero file, e.g. "1/4A2  (AP)".
     * @param warnings   The warning set to add import warnings to.
     * @return The motor, or null if not found.
     */
    public static ThrustCurveMotor getMotorFromRASAero(String motorString, WarningSet warnings) {
        if (motorString == null) {
            return null;
        }
        if (allMotors == null) {
            loadAllMotors();
        }
        /*
            RASAero file motor strings are formatted as "<motorName>  (<manufacturer>)"
         */
        String[] split = motorString.split("\\s{2}");
        if (split.length != 2) {
            return null;
        }
        String motorName = AbstractMotorLoader.removeDelay(split[0]);
        String manufacturer = split[1].replaceAll("^\\(|\\)$", "");     // Remove beginning and ending parenthesis
        for (ThrustCurveMotor motor : allMotors) {
            if (motorName.equals(motor.getDesignation()) && motor.getManufacturer().matches(manufacturer)) {
                return motor;
            }
        }
        warnings.add("Could not find motor '" + motorString + "' in the OpenRocket motors database. Please add it manually.");
        return null;
    }

    /**
     * Call this method when you don't need the RASAero motors anymore to free memory.
     */
    public static void clearAllMotors() {
        if (allMotors != null) {
            allMotors.clear();
            allMotors = null;
        }
    }

    // Not currently used, because it causes some compatibility issues when e.g. wanting to open the RASAero motor
    // in the motor selection table (because it is not present there).
    // It's probably also better to load OR-native motors.
    // But I'll leave this in, in case it's needed in the future.
    /*
     * Loads all original RASAero motors.
     * @param warnings The warning set to add import warnings to.
     * @throws RuntimeException If the RASAero motors file could not be found.
     *
     private static void loadAllRASAeroMotors(WarningSet warnings) throws RuntimeException {
        allMotors = new ArrayList<>();

        GeneralMotorLoader loader = new GeneralMotorLoader();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        String fileName = "RASAero_Motors.eng";
        InputStream is = classloader.getResourceAsStream("datafiles/thrustcurves/RASAero/" + fileName);
        if (is == null) {
            throw new RuntimeException("Could not find " + fileName);
        }
        try {
            List<ThrustCurveMotor.Builder> motors = loader.load(is, fileName);
            for (ThrustCurveMotor.Builder builder : motors) {
                allMotors.add(builder.build());
            }
        } catch (IOException e) {
            warnings.add("Error during motor loading: " + e.getMessage());
        }
    }*/

    /**
     * Loads the OpenRocket motors database.
     */
    private static void loadAllMotors() {
        allMotors = new ArrayList<>();
        List<ThrustCurveMotorSet> database = Application.getThrustCurveMotorSetDatabase().getMotorSets();
        for (ThrustCurveMotorSet set : database) {
            allMotors.addAll(set.getMotors());
        }
    }

}
